// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
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
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.ArmConstants;

/**
 * The {@code ArmSubsystem} class is a subsystem that controls the movement of an arm using a
 * Profiled PID Controller. It uses a CANSparkMax motor and a RelativeEncoder to measure the arm's
 * position. The class provides methods to move the arm to a specific position, hold the arm at the
 * current position, and shift the arm's position up or down by a fixed increment.
 *
 * <p>Example Usage:
 *
 * <pre>{@code
 * // Create a new instance of ArmSubsystem
 * CANSparkMax motor = new CANSparkMax(1, MotorType.kBrushless);
 * ArmSubsystem armSubsystem = new ArmSubsystem(motor);
 *
 * // Move the arm to a specific position
 * Command moveToPositionCommand = armSubsystem.moveToPosition(90.0);
 * moveToPositionCommand.schedule();
 *
 * // Hold the arm at the current position
 * Command holdPositionCommand = armSubsystem.holdPosition();
 * holdPositionCommand.schedule();
 *
 * // Shift the arm's position up by a fixed increment
 * Command shiftUpCommand = armSubsystem.shiftUp();
 * shiftUpCommand.schedule();
 *
 * // Shift the arm's position down by a fixed increment
 * Command shiftDownCommand = armSubsystem.shiftDown();
 * shiftDownCommand.schedule();
 * }
 *
 * Code Analysis:
 * - Main functionalities:
 *   - Control the movement of an arm using a Profiled PID Controller
 *   - Move the arm to a specific position
 *   - Hold the arm at the current position
 *   - Shift the arm's position up or down by a fixed increment
 * - Methods:
 *   - {@code periodic()}: Updates the SmartDashboard with information about the arm's state.
 *   - {@code useOutput()}: Generates the motor command using the PID controller and feedforward.
 *   - {@code moveToPosition(double goal)}: Returns a Command that moves the arm to a new position.
 *   - {@code holdPosition()}: Returns a Command that holds the arm at the last goal position.
 *   - {@code shiftUp()}: Returns a Command that shifts the arm's position up by a fixed increment.
 *   - {@code shiftDown()}: Returns a Command that shifts the arm's position down by a fixed
 *     increment.
 *   - {@code setGoalPosition(double goal)}: Sets the goal state for the subsystem.
 *   - {@code atGoalPosition()}: Returns whether the arm has reached the goal position.
 *   - {@code enable()}: Enables the PID control of the arm.
 *   - {@code disable()}: Disables the PID control of the arm.
 *   - {@code getMeasurement()}: Returns the arm position for PID control and logging.
 *   - {@code getVoltageCommand()}: Returns the motor commanded voltage.
 *   - {@code initPreferences()}: Initializes the preferences for tuning the controller.
 *   - {@code loadPreferences()}: Loads the preferences for tuning the controller.
 *   - {@code close()}: Closes any objects that support it.
 * - Fields:
 *   - {@code private final CANSparkMax motor}: The motor used to control the arm.
 *   - {@code private final RelativeEncoder encoder}: The encoder used to measure the arm's
 *     position.
 *   - {@code private ProfiledPIDController armController}: The PID controller used to control the
 *     arm's movement.
 *   - {@code private ArmFeedforward feedforward}: The feedforward controller used to calculate the
 *     motor output.
 *   - {@code private double output}: The output of the PID controller.
 *   - {@code private TrapezoidProfile.State setpoint}: The setpoint of the PID controller.
 *   - {@code private double newFeedforward}: The calculated feedforward value.
 *   - {@code private boolean armEnabled}: A flag indicating whether the arm is enabled.
 *   - {@code private double voltageCommand}: The motor commanded voltage.
 * </pre>
 */
public class ArmSubsystem extends SubsystemBase implements AutoCloseable {

  /** Hardware components for the arm subsystem. */
  public static class Hardware {
    CANSparkMax motor;
    RelativeEncoder encoder;

    public Hardware(CANSparkMax motor, RelativeEncoder encoder) {
      this.motor = motor;
      this.encoder = encoder;
    }
  }

  private final CANSparkMax motor;
  private final RelativeEncoder encoder;

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
  public ArmSubsystem(Hardware armHardware) {
    this.motor = armHardware.motor;
    this.encoder = armHardware.encoder;

    initializeArm();
  }

  private void initializeArm() {

    initPreferences();
    initEncoder();
    initMotor();

    // Set tolerances that will be used to determine when the arm is at the goal position.
    armController.setTolerance(
        Constants.ArmConstants.POSITION_TOLERANCE, Constants.ArmConstants.VELOCITY_TOLERANCE);

    disable();
  }

  private void initMotor() {
    motor.restoreFactoryDefaults();
    // Maybe we should print the faults if non-zero before clearing?
    motor.clearFaults();
    // Configure the motor to use EMF braking when idle and set voltage to 0.
    motor.setIdleMode(IdleMode.kBrake);
    DataLogManager.log("Arm motor firmware version:" + motor.getFirmwareString());
  }

  private void initEncoder() {
    // Setup the encoder scale factors and reset encoder to 0. Since this is a relation encoder,
    // arm position will only be correct if the arm is in the starting rest position when the
    // subsystem is constructed.
    encoder.setPositionConversionFactor(ArmConstants.ARM_RAD_PER_ENCODER_ROTATION);
    encoder.setVelocityConversionFactor(ArmConstants.RPM_TO_RAD_PER_SEC);
    encoder.setPosition(0);
  }

  /**
   * Initialize hardware devices for the arm subsystem.
   *
   * @return Hardware object containing all necessary devices for this subsystem
   */
  public static Hardware initializeHardware() {
    CANSparkMax motor = new CANSparkMax(ArmConstants.MOTOR_PORT, MotorType.kBrushless);
    RelativeEncoder encoder = motor.getEncoder();

    return new Hardware(motor, encoder);
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
    if (armEnabled) {
      // Calculate the next set point along the profile to the goal and the next PID output based
      // on the set point and current position.
      output = armController.calculate(getMeasurement());
      setpoint = armController.getSetpoint();

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
      output = 0;
      newFeedforward = 0;
      voltageCommand = 0;
    }
    motor.setVoltage(voltageCommand);
  }

  /** Returns a Command that moves the arm to a new position. */
  public Command moveToPositionOrig(double goal) {
    return runOnce(() -> setGoalPosition(goal))
        .andThen(run(this::useOutput))
        .until(this::atGoalPosition);
  }

  /** Returns a Command that moves the arm to a new position. */
  public Command moveToPosition(double goal) {
    return new FunctionalCommand(
        () -> setGoalPosition(goal),
        this::useOutput,
        interrupted -> {},
        this::atGoalPosition,
        this);
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
    DataLogManager.log(
        "Arm Disabled CurPos="
            + Units.radiansToDegrees(getMeasurement())
            + " CurVel="
            + Units.radiansToDegrees(encoder.getVelocity()));
  }

  /** Returns the Arm position for PID control and logging (Units are Radians from horizontal). */
  public double getMeasurement() {
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
