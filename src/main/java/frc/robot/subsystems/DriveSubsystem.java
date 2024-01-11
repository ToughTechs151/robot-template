// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.DriveConstants;

/** Drive subsystem using differential drive. */
public class DriveSubsystem extends SubsystemBase {
  private final CANSparkMax frontLeft =
      new CANSparkMax(DriveConstants.FRONT_LEFT_MOTOR_PORT, MotorType.kBrushless);
  private final CANSparkMax rearLeft =
      new CANSparkMax(DriveConstants.REAR_LEFT_MOTOR_PORT, MotorType.kBrushless);
  private final CANSparkMax frontRight =
      new CANSparkMax(DriveConstants.FRONT_RIGHT_MOTOR_PORT, MotorType.kBrushless);
  private final CANSparkMax rearRight =
      new CANSparkMax(DriveConstants.REAR_RIGHT_MOTOR_PORT, MotorType.kBrushless);

  private final DifferentialDrive drive = new DifferentialDrive(frontLeft, frontRight);

  // The front-left-side drive encoder
  private final RelativeEncoder frontLeftEncoder = this.frontLeft.getEncoder();

  // The rear-left-side drive encoder
  private final RelativeEncoder rearLeftEncoder = this.rearLeft.getEncoder();

  // The front-right--side drive encoder
  private final RelativeEncoder frontRightEncoder = this.frontRight.getEncoder();

  // The rear-right-side drive encoder
  private final RelativeEncoder rearRightEncoder = this.rearRight.getEncoder();

  // The gyro sensor
  private final ADXRS450_Gyro gyro = new ADXRS450_Gyro();

  // Odometry class for tracking robot pose
  DifferentialDriveOdometry odometry =
      new DifferentialDriveOdometry(
          this.gyro.getRotation2d(),
          frontLeftEncoder.getPosition(),
          frontRightEncoder.getPosition());

  // drive constants
  /** The scaling factor between the joystick value and the speed controller. */
  private double speedMultiplier = 0.5;

  /** The scale factor for normal mode. */
  private static final double NORMAL = 1.0;

  /** The scale factor for crawl mode. */
  private static final double CRAWL = 0.3;

  /** Creates a new DriveSubsystem. */
  public DriveSubsystem() {

    this.frontLeft.restoreFactoryDefaults();
    this.frontRight.restoreFactoryDefaults();
    this.rearLeft.restoreFactoryDefaults();
    this.rearRight.restoreFactoryDefaults();

    this.frontLeft.setIdleMode(IdleMode.kCoast);
    this.frontRight.setIdleMode(IdleMode.kCoast);
    this.rearLeft.setIdleMode(IdleMode.kCoast);
    this.rearRight.setIdleMode(IdleMode.kCoast);

    rearLeft.follow(frontLeft);
    rearRight.follow(frontRight);

    // Sets the distance per pulse for the encoders
    this.frontLeftEncoder.setPositionConversionFactor(DriveConstants.ENCODER_DISTANCE_PER_PULSE);
    this.rearLeftEncoder.setPositionConversionFactor(DriveConstants.ENCODER_DISTANCE_PER_PULSE);
    this.frontRightEncoder.setPositionConversionFactor(DriveConstants.ENCODER_DISTANCE_PER_PULSE);
    this.rearRightEncoder.setPositionConversionFactor(DriveConstants.ENCODER_DISTANCE_PER_PULSE);
    this.frontLeftEncoder.setVelocityConversionFactor(DriveConstants.ENCODER_VELOCITY_CONVERSION);
    this.rearLeftEncoder.setVelocityConversionFactor(DriveConstants.ENCODER_VELOCITY_CONVERSION);
    this.frontRightEncoder.setVelocityConversionFactor(DriveConstants.ENCODER_VELOCITY_CONVERSION);
    this.rearRightEncoder.setVelocityConversionFactor(DriveConstants.ENCODER_VELOCITY_CONVERSION);

    // We need to invert one side of the drivetrain so that positive voltages
    // result in both sides moving forward. Depending on how your robot's
    // gearbox is constructed, you might have to invert the left side instead.
    frontRight.setInverted(true);

    SmartDashboard.putData(this.drive);
  }

  @Override
  public void periodic() {
    // Update the odometry in the periodic block
    this.odometry.update(
        this.gyro.getRotation2d(), frontLeftEncoder.getPosition(), frontRightEncoder.getPosition());

    SmartDashboard.putNumber("temp left pos", frontLeftEncoder.getPosition());
    SmartDashboard.putNumber("temp right pos", frontRightEncoder.getPosition());
    SmartDashboard.putNumber("temp gyro angle", gyro.getAngle());
    SmartDashboard.putNumber("temp gyro rate", gyro.getRate());

    SmartDashboard.putNumber("FL-Voltage", frontLeft.getBusVoltage());
    SmartDashboard.putNumber("FL-Current", frontLeft.getOutputCurrent());
    SmartDashboard.putNumber("FL-Temp", frontLeft.getMotorTemperature());

    SmartDashboard.putNumber("RL-Voltage", rearLeft.getBusVoltage());
    SmartDashboard.putNumber("RL-Current", rearLeft.getOutputCurrent());
    SmartDashboard.putNumber("RL-Temp", rearLeft.getMotorTemperature());

    SmartDashboard.putNumber("FR-Voltage", frontRight.getBusVoltage());
    SmartDashboard.putNumber("FR-Current", frontRight.getOutputCurrent());
    SmartDashboard.putNumber("FR-Temp", frontRight.getMotorTemperature());

    SmartDashboard.putNumber("RR-Voltage", rearRight.getBusVoltage());
    SmartDashboard.putNumber("RR-Current", rearRight.getOutputCurrent());
    SmartDashboard.putNumber("RR-Temp", rearRight.getMotorTemperature());
  }

  /**
   * Drives the robot using arcade controls.
   *
   * @param leftSpeed The left joystick controller speed -1 to 1
   * @param rightSpeed The right joystick controller speed -1 to 1
   */
  public void tankDrive(double leftSpeed, double rightSpeed, boolean isCrawl) {
    speedMultiplier = isCrawl ? CRAWL : NORMAL;
    drive.tankDrive(leftSpeed * speedMultiplier, rightSpeed * speedMultiplier, true);
  }

  /**
   * Returns the currently-estimated pose of the robot.
   *
   * @return The pose.
   */
  public Pose2d getPose() {
    return this.odometry.getPoseMeters();
  }

  /**
   * Returns the current wheel speeds of the robot.
   *
   * @return The current wheel speeds.
   */
  public DifferentialDriveWheelSpeeds getWheelSpeeds() {
    return new DifferentialDriveWheelSpeeds(
        frontLeftEncoder.getPosition(), frontRightEncoder.getPosition());
  }

  /**
   * Controls the left and right sides of the drive directly with voltages.
   *
   * @param leftVolts the commanded left output
   * @param rightVolts the commanded right output
   */
  public void tankDriveVolts(double leftVolts, double rightVolts) {
    frontLeft.setVoltage(leftVolts);
    frontRight.setVoltage(rightVolts);
    drive.feed();
  }

  /**
   * Resets the odometry to the specified pose.
   *
   * @param pose The pose to which to set the odometry.
   */
  public void resetOdometry(Pose2d pose) {
    this.odometry.resetPosition(
        this.gyro.getRotation2d(),
        frontLeftEncoder.getPosition(),
        frontRightEncoder.getPosition(),
        pose);
  }

  /** Resets the drive encoders to currently read a position of 0. */
  public void resetEncoders() {
    this.frontLeftEncoder.setPosition(0);
    this.rearLeftEncoder.setPosition(0);
    this.frontRightEncoder.setPosition(0);
    this.rearRightEncoder.setPosition(0);
  }

  /**
   * Gets the current wheel speeds.
   *
   * @return the current wheel speeds in a DifferentialDriveWheelSpeeds object.
   */
  public DifferentialDriveWheelSpeeds getCurrentWheelSpeeds() {
    return new DifferentialDriveWheelSpeeds(
        frontLeftEncoder.getVelocity(), frontRightEncoder.getVelocity());
  }

  /**
   * Sets the max output of the drive. Useful for scaling the drive to drive more slowly.
   *
   * @param maxOutput the maximum output to which the drive will be constrained
   */
  public void setMaxOutput(double maxOutput) {
    this.drive.setMaxOutput(maxOutput);
  }

  /** Zeroes the heading of the robot. */
  public void zeroHeading() {
    this.gyro.reset();
  }

  /**
   * Returns the heading of the robot.
   *
   * @return the robot's heading in degrees, from -180 to 180
   */
  public double getHeading() {
    return this.gyro.getRotation2d().getDegrees();
  }

  /**
   * Returns the turn rate of the robot.
   *
   * @return The turn rate of the robot, in degrees per second
   */
  public double getTurnRate() {
    return -this.gyro.getRate();
  }

  /* The following fields and methods are used during simulation mode.
   *  Get subsystem outputs to the real hardware to drive the simulation
   */

  /**
   * Get a reference to the gyro for simulation.
   *
   * @return Reference to the gyro device
   */
  public ADXRS450_Gyro getGyro() {
    return gyro;
  }

  /**
   * Get the voltage command to the left motor.
   *
   * @return command to the left motor controller group in volts
   */
  public double getLeftMotorVolts() {
    return frontLeft.get();
  }

  /**
   * Get the voltage command to the right motor.
   *
   * @return command to the right motor controller group in volts
   */
  public double getRightMotorVolts() {
    return frontRight.get();
  }

  /**
   * Disable the drive by setting motor output to zero. Any PID controllers should also be disabled
   * here. NOTE: In this state the drive will roll to a stop if using coast mode. Using EMF braking
   * mode will cause drive to stop quickly.
   */
  public void disable() {
    tankDriveVolts(0, 0);
  }
}
