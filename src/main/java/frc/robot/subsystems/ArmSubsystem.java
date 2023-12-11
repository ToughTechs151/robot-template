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
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;
import frc.robot.Constants;
import frc.robot.Constants.ArmConstants;

/** A robot arm subsystem that moves with a motion profile. */
public class ArmSubsystem extends ProfiledPIDSubsystem implements AutoCloseable {
  private final CANSparkMax motor = new CANSparkMax(ArmConstants.MOTOR_PORT, MotorType.kBrushless);
  private final RelativeEncoder encoder = motor.getEncoder();

  private ArmFeedforward feedforward =
      new ArmFeedforward(
          ArmConstants.DEFAULT_KS_VOLTS,
          ArmConstants.DEFAULT_KG_VOLTS,
          ArmConstants.DEFAULT_KV_VOLTS_PER_SEC_PER_RAD,
          ArmConstants.DEFAULT_KA_VOLTS_PER_SEC_SQUARED_PER_RAD);

  private double voltageCommand = 0.0;
  private double goalPosition;

  /** Create a new ArmSubsystem controlled by a Profiled PID COntroller . */
  public ArmSubsystem() {
    super(
        new ProfiledPIDController(
            Constants.ArmConstants.DEFAULT_ARM_KP,
            0,
            0,
            new TrapezoidProfile.Constraints(
                ArmConstants.DEFAULT_MAX_VELOCITY_RAD_PER_SEC,
                ArmConstants.DEFAULT_MAX_ACCELERATION_RAD_PER_SEC)),
        0);

    // Setup the encoder scale factors and reset encoder to 0
    encoder.setPositionConversionFactor(ArmConstants.ARM_RAD_PER_ENCODER_ROTATION);
    encoder.setVelocityConversionFactor(ArmConstants.RPM_TO_RAD_PER_SEC);
    resetPosition();

    // Configure the motor to use EMF braking when idle and set voltage to 0
    motor.setIdleMode(IdleMode.kBrake);
    motor.setVoltage(0.0);

    /* Assume the arm is starting in the back rest position, so initialize goal to this point so no
    movement is needed when enabled */
    setGoal(ArmConstants.ARM_OFFSET_RADS);

    setupShuffleboard();

    initPreferences();
  }

  @Override
  public void periodic() {
    if (m_enabled) {
      useOutput(m_controller.calculate(getMeasurement()), m_controller.getSetpoint());
    }
    updateShuffleboard();
  }

  // Generate the motor command using the PID controller and feedforward
  @Override
  public void useOutput(double output, TrapezoidProfile.State setpoint) {
    double newFeedforward = 0;
    if (m_enabled) {
      // Calculate the feedforward from the setpoint
      newFeedforward = feedforward.calculate(setpoint.position, setpoint.velocity);
      // Add the feedforward to the PID output to get the motor output
      voltageCommand = output + newFeedforward;
    } else {
      voltageCommand = 0;
    }
    motor.setVoltage(voltageCommand);

    SmartDashboard.putNumber("feedforward", newFeedforward);
    SmartDashboard.putNumber("output", output);
    SmartDashboard.putNumber("SetPt Pos", Units.radiansToDegrees(setpoint.position));
    SmartDashboard.putNumber("SetPt Vel", Units.radiansToDegrees(setpoint.velocity));
  }

  @Override
  // Arm position for PID measurement (Radians relative to horizontal)
  public double getMeasurement() {
    // Add offset for starting zero point
    return encoder.getPosition() + ArmConstants.ARM_OFFSET_RADS;
  }

  // Motor Commanded Voltage
  public double getVoltageCommand() {
    return voltageCommand;
  }

  /**
   * Reset the Arm encoder to zero. Should only be used when the arm is in the neutral offset
   * position. This method is only allowed when the arm is disabled.
   */
  public void resetPosition() {

    if (m_enabled) {
      DataLogManager.log("Warning: Arm is enabled - encoder position not reset.");
    } else {
      encoder.setPosition(0);
    }
  }

  /** Calculate increased goal, limited to allowed range. */
  public double increasedGoal() {
    double newGoal = m_controller.getGoal().position + Constants.ArmConstants.POS_INCREMENT;
    return MathUtil.clamp(
        newGoal, Constants.ArmConstants.MIN_ANGLE_RADS, Constants.ArmConstants.MAX_ANGLE_RADS);
  }

  /** Calculate decreased goal, limited to allowed range. */
  public double decreasedGoal() {
    double newGoal = m_controller.getGoal().position - Constants.ArmConstants.POS_INCREMENT;
    return MathUtil.clamp(
        newGoal, Constants.ArmConstants.MIN_ANGLE_RADS, Constants.ArmConstants.MAX_ANGLE_RADS);
  }

  /** Enables the PID control. Resets the controller. */
  @Override
  public void enable() {

    // Don't enable if already enabled since this may cause control transients
    if (!m_enabled) {
      m_enabled = true;
      loadPreferences();
      m_controller.reset(getMeasurement());
      DataLogManager.log(
          "Arm Enabled - kP="
              + m_controller.getP()
              + " kI="
              + m_controller.getI()
              + " kD="
              + m_controller.getD()
              + " PosGoal="
              + Units.radiansToDegrees(goalPosition)
              + " CurPos="
              + Units.radiansToDegrees(getMeasurement()));
    }
  }

  /** Disables the PID control. Sets output to zero. */
  @Override
  public void disable() {

    // Set goal to current position to minimize movement on re-enable and reset output
    m_enabled = false;
    setGoal(getMeasurement());
    useOutput(0, new State());
    DataLogManager.log("Arm Disabled");
  }

  /** Sets the goal state for the subsystem. Goal velocity assumed to be zero. */
  @Override
  public void setGoal(double goal) {
    setGoal(new TrapezoidProfile.State(goal, 0));
    goalPosition = goal;
  }

  /** Shuffleboard settings that only need to done during initialization. */
  private void setupShuffleboard() {
    // Put shuffleboard initialization here
  }

  /** Update Shuffleboard values (call periodically). */
  public void updateShuffleboard() {

    SmartDashboard.putBoolean("Arm Enabled", m_enabled);
    SmartDashboard.putNumber("Arm Goal", Units.radiansToDegrees(goalPosition));
    SmartDashboard.putNumber("Measured Angle", Units.radiansToDegrees(getMeasurement()));
    SmartDashboard.putNumber("Arm Velocity", Units.radiansToDegrees(encoder.getVelocity()));
    SmartDashboard.putNumber("Motor Voltage", voltageCommand);
    SmartDashboard.putNumber("Battery Voltage", RobotController.getBatteryVoltage()); // sim
    SmartDashboard.putNumber("Motor Current", motor.getOutputCurrent());
  }

  /** Put tunable values in Preferences table if the keys don't already exist. */
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
    Preferences.initDouble(
        Constants.ArmConstants.ARM_KA_KEY,
        Constants.ArmConstants.DEFAULT_KA_VOLTS_PER_SEC_SQUARED_PER_RAD);
  }

  /**
   * Load Preferences for values that can be tuned at runtime. This should only be called when the
   * controller is disabled, for example from enable()
   */
  private void loadPreferences() {

    // Read Preferences for PID controller
    m_controller.setP(
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
    m_controller.setConstraints(new TrapezoidProfile.Constraints(velocityMax, accelerationMax));

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
    double accelerationGain =
        Preferences.getDouble(
            Constants.ArmConstants.ARM_KA_KEY,
            Constants.ArmConstants.DEFAULT_KA_VOLTS_PER_SEC_SQUARED_PER_RAD);
    feedforward = new ArmFeedforward(staticGain, gravityGain, velocityGain, accelerationGain);
  }

  @Override
  public void close() {
    motor.close();
  }
}
