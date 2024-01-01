// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.ArmConstants;

/** A robot arm subsystem that moves with a motion profile. */
public class ArmSubsystem extends SubsystemBase implements AutoCloseable {
  private final CANSparkMax motor = new CANSparkMax(ArmConstants.MOTOR_PORT, MotorType.kBrushless);
  private final RelativeEncoder encoder = motor.getEncoder();

  private ProfiledPIDController armController =
      new ProfiledPIDController(
          Constants.ArmConstants.DEFAULT_ARM_KP,
          0,
          0,
          new TrapezoidProfile.Constraints(
              ArmConstants.DEFAULT_MAX_VELOCITY_RAD_PER_SEC,
              ArmConstants.DEFAULT_MAX_ACCELERATION_RAD_PER_SEC));

  private ArmFeedforward feedforward =
      new ArmFeedforward(
          ArmConstants.DEFAULT_KS_VOLTS,
          ArmConstants.DEFAULT_KG_VOLTS,
          ArmConstants.DEFAULT_KV_VOLTS_PER_SEC_PER_RAD,
          0.0); // Acceleration is not used in this implementation

  private double output = 0.0;
  private TrapezoidProfile.State setpoint = new State();
  private double newFeedforward = 0;
  private boolean armEnabled;
  private double voltageCommand = 0.0;

  /** Create a new ArmSubsystem controlled by a Profiled PID COntroller . */
  public ArmSubsystem() {

    // Setup the encoder scale factors and reset encoder to 0. Since this is a relation encoder,
    // arm position will only be correct if the arm is in the starting rest position when the
    // subsystem is constructed.
    encoder.setPositionConversionFactor(ArmConstants.ARM_RAD_PER_ENCODER_ROTATION);
    encoder.setVelocityConversionFactor(ArmConstants.RPM_TO_RAD_PER_SEC);
    encoder.setPosition(0);

    // Configure the motor to use EMF braking when idle and set voltage to 0.
    motor.setIdleMode(IdleMode.kBrake);
    motor.setVoltage(0.0);

    // Set tolerances that will be used to determine when the arm is at the goal position.
    armController.setTolerance(
        Constants.ArmConstants.POSITION_TOLERANCE, Constants.ArmConstants.VELOCITY_TOLERANCE);

    disable();

    initPreferences();

    SmartDashboard.putData(this);
  }

  @Override
  public void periodic() {

    SmartDashboard.putBoolean("Arm Enabled", armEnabled);
    SmartDashboard.putNumber("Arm Goal", Units.radiansToDegrees(armController.getGoal().position));
    SmartDashboard.putNumber("Arm Angle", Units.radiansToDegrees(getMeasurement()));
    SmartDashboard.putNumber("Arm Velocity", Units.radiansToDegrees(encoder.getVelocity()));
    SmartDashboard.putNumber("Arm Voltage", voltageCommand);
    SmartDashboard.putNumber("Arm Current", motor.getOutputCurrent());
    SmartDashboard.putNumber("Arm Feedforward", newFeedforward);
    SmartDashboard.putNumber("Arm PID output", output);
    SmartDashboard.putNumber("Arm SetPt Pos", Units.radiansToDegrees(setpoint.position));
    SmartDashboard.putNumber("Arm SetPt Vel", Units.radiansToDegrees(setpoint.velocity));
  }

  /** Generate the motor command using the PID controller and feedforward. */
  public void useOutput() {
    // Calculate the next set point along the profile to the goal and the next PID output based
    // on the set point and current position.
    output = armController.calculate(getMeasurement());
    setpoint = armController.getSetpoint();

    if (armEnabled) {
      // Calculate the feedforward to move the arm at the desired velocity and offset
      // the effect of gravity at the desired position. Voltage for acceleration is not
      // used.
      newFeedforward = feedforward.calculate(setpoint.position, setpoint.velocity);

      // Add the feedforward to the PID output to get the motor output
      voltageCommand = output + newFeedforward;

    } else {
      // If the arm isn't enabled, set the motor command to 0. In this state the arm
      // will move down until it hits the rest position. Motor EMF braking will slow movement
      // if that mode is used.
      voltageCommand = 0;
    }
    motor.setVoltage(voltageCommand);
  }

  /** Returns a Command that moves the arm to a new position. */
  public Command moveToPosition(double goal) {
    return runOnce(() -> setGoalPosition(goal))
        .andThen(run(this::useOutput))
        .until(this::atGoalPosition);
  }

  /**
   * Returns a Command that holds the arm at the last goal position using the PID Controller driving
   * the motor.
   */
  public Command holdPosition() {
    return run(this::useOutput).withName("Arm: Hold Position");
  }

  /** Returns a Command that shifts arm position up by a fixed increment. */
  public Command shiftUp() {
    return runOnce(
            () ->
                setGoalPosition(
                    armController.getGoal().position + Constants.ArmConstants.POS_INCREMENT))
        .andThen(run(this::useOutput))
        .until(this::atGoalPosition)
        .withName("Arm: Shift Position Up");
  }

  /** Returns a Command that shifts arm position down by a fixed increment. */
  public Command shiftDown() {
    return runOnce(
            () ->
                setGoalPosition(
                    armController.getGoal().position - Constants.ArmConstants.POS_INCREMENT))
        .andThen(run(this::useOutput))
        .until(this::atGoalPosition)
        .withName("Arm: Shift Position Down");
  }

  /**
   * Set the goal state for the subsystem, limited to allowable range. Goal velocity is set to zero.
   * The ProfiledPIDController drives the arm to this position and holds it there.
   */
  private void setGoalPosition(double goal) {
    armController.setGoal(
        new TrapezoidProfile.State(
            MathUtil.clamp(
                goal, Constants.ArmConstants.MIN_ANGLE_RADS, Constants.ArmConstants.MAX_ANGLE_RADS),
            0));

    // Call enable() to configure and start the controller in case it is not already enabled.
    enable();
  }

  /** Returns whether the arm has reached the goal position and velocity is within limits. */
  public boolean atGoalPosition() {
    return armController.atGoal();
  }

  /**
   * Sets up the PID controller to move the arm to the defined goal position and hold at that
   * position. Preferences for tuning the controller are applied.
   */
  private void enable() {

    // Don't enable if already enabled since this may cause control transients
    if (!armEnabled) {
      loadPreferences();
      setDefaultCommand(holdPosition());

      // Reset the PID controller to clear any previous state
      armController.reset(getMeasurement());
      armEnabled = true;

      DataLogManager.log(
          "Arm Enabled - kP="
              + armController.getP()
              + " kI="
              + armController.getI()
              + " kD="
              + armController.getD()
              + " PosGoal="
              + Units.radiansToDegrees(armController.getGoal().position)
              + " CurPos="
              + Units.radiansToDegrees(getMeasurement()));
    }
  }

  /**
   * Disables the PID control of the arm. Sets motor output to zero. NOTE: In this state the arm
   * will move until it hits the stop. Using EMF braking mode with motor will slow this movement.
   */
  public void disable() {

    // Clear the enabled flag and call useOutput to zero the motor command
    armEnabled = false;
    useOutput();

    // Remove the default command and cancel any command that is active
    removeDefaultCommand();
    Command currentCommand = CommandScheduler.getInstance().requiring(this);
    if (currentCommand != null) {
      CommandScheduler.getInstance().cancel(currentCommand);
    }
    DataLogManager.log("Arm Disabled");
  }

  /** Returns the Arm position for PID control and logging (Units are Radians from horizontal). */
  private double getMeasurement() {
    // Add the offset from the starting point. The arm must be at this position at startup for
    // the relative encoder to provide a correct position.
    return encoder.getPosition() + ArmConstants.ARM_OFFSET_RADS;
  }

  /** Returns the Motor Commanded Voltage. */
  public double getVoltageCommand() {
    return voltageCommand;
  }

  /**
   * Put tunable values in the Preferences table using default values, if the keys don't already
   * exist.
   */
  private void initPreferences() {

    // Preferences for PID controller
    Preferences.initDouble(
        Constants.ArmConstants.ARM_KP_KEY, Constants.ArmConstants.DEFAULT_ARM_KP);

    // Preferences for Trapezoid Profile
    Preferences.initDouble(
        Constants.ArmConstants.ARM_VELOCITY_MAX_KEY,
        Constants.ArmConstants.DEFAULT_MAX_VELOCITY_RAD_PER_SEC);
    Preferences.initDouble(
        Constants.ArmConstants.ARM_ACCELERATION_MAX_KEY,
        Constants.ArmConstants.DEFAULT_MAX_ACCELERATION_RAD_PER_SEC);

    // Preferences for Feedforward
    Preferences.initDouble(
        Constants.ArmConstants.ARM_KS_KEY, Constants.ArmConstants.DEFAULT_KS_VOLTS);
    Preferences.initDouble(
        Constants.ArmConstants.ARM_KG_KEY, Constants.ArmConstants.DEFAULT_KG_VOLTS);
    Preferences.initDouble(
        Constants.ArmConstants.ARM_KV_KEY, Constants.ArmConstants.DEFAULT_KV_VOLTS_PER_SEC_PER_RAD);
  }

  /**
   * Load Preferences for values that can be tuned at runtime. This should only be called when the
   * controller is disabled - for example from enable().
   */
  private void loadPreferences() {

    // Read Preferences for PID controller
    armController.setP(
        Preferences.getDouble(
            Constants.ArmConstants.ARM_KP_KEY, Constants.ArmConstants.DEFAULT_ARM_KP));

    // Read Preferences for Trapezoid Profile and update
    double velocityMax =
        Preferences.getDouble(
            Constants.ArmConstants.ARM_VELOCITY_MAX_KEY,
            Constants.ArmConstants.DEFAULT_MAX_VELOCITY_RAD_PER_SEC);
    double accelerationMax =
        Preferences.getDouble(
            Constants.ArmConstants.ARM_ACCELERATION_MAX_KEY,
            Constants.ArmConstants.DEFAULT_MAX_ACCELERATION_RAD_PER_SEC);
    armController.setConstraints(new TrapezoidProfile.Constraints(velocityMax, accelerationMax));

    // Read Preferences for Feedforward and create a new instance
    double staticGain =
        Preferences.getDouble(
            Constants.ArmConstants.ARM_KS_KEY, Constants.ArmConstants.DEFAULT_KS_VOLTS);
    double gravityGain =
        Preferences.getDouble(
            Constants.ArmConstants.ARM_KG_KEY, Constants.ArmConstants.DEFAULT_KG_VOLTS);
    double velocityGain =
        Preferences.getDouble(
            Constants.ArmConstants.ARM_KV_KEY,
            Constants.ArmConstants.DEFAULT_KV_VOLTS_PER_SEC_PER_RAD);

    feedforward = new ArmFeedforward(staticGain, gravityGain, velocityGain, 0);
  }

  /** Close any objects that support it. */
  @Override
  public void close() {
    motor.close();
  }
}
