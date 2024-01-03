// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.util.Units;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {

  private Constants() {
    throw new IllegalStateException("Utility class");
  }

  // Joystick Axes
  public static final int LEFT_X = 0;
  public static final int LEFT_Y = 1;
  public static final int LEFT_TRIGGER = 2;
  public static final int RIGHT_TRIGGER = 3;
  public static final int RIGHT_X = 4;
  public static final int RIGHT_Y = 5;

  // Joystick Buttons
  public static final int JS_A = 1;
  public static final int JS_B = 2;
  public static final int JS_X = 3;
  public static final int JS_Y = 4;
  public static final int JS_LB = 5;
  public static final int JS_RB = 6;
  public static final int JS_BACK = 7;
  public static final int JS_START = 8;
  public static final int JS_L_STICK = 9;
  public static final int JS_R_STICK = 10;

  // Run time options

  // Set to true to log Joystick data. To false otherwise.
  public static final boolean LOG_JOYSTICK_DATA = true;

  // Set to true to send telemetry data to Live Window. To false
  // to disable it.
  public static final boolean LW_TELEMETRY_ENABLE = false;

  // Set to true to log loop timing data. To false to disable.
  public static final boolean LOOP_TIMING_LOG = true;

  /** Constants used for the Drive subsystem. */
  public static final class DriveConstants {

    private DriveConstants() {
      throw new IllegalStateException("DriveConstants Utility class");
    }

    public static final int FRONT_LEFT_MOTOR_PORT = 4;
    public static final int REAR_LEFT_MOTOR_PORT = 1;
    public static final int FRONT_RIGHT_MOTOR_PORT = 2;
    public static final int REAR_RIGHT_MOTOR_PORT = 3;

    // Distance between centers of right and left wheels on robot
    public static final double WHEEL_BASE = 0.7;
    // Distance between centers of front and back wheels on robot

    public static final double TRACK_WIDTH_METERS = Units.inchesToMeters(22);
    public static final DifferentialDriveKinematics kDriveKinematics =
        new DifferentialDriveKinematics(TRACK_WIDTH_METERS);

    public static final double GEAR_RATIO = 10.71;
    public static final double WHEEL_DIAMETER_METERS = 0.15;
    public static final double ENCODER_DISTANCE_PER_PULSE =
        // Assumes the encoders are directly mounted on the wheel shafts
        (WHEEL_DIAMETER_METERS * Math.PI) / GEAR_RATIO;
    public static final double ENCODER_VELOCITY_CONVERSION =
        (WHEEL_DIAMETER_METERS * Math.PI) / (GEAR_RATIO * 60);
  }

  /** Constants used for the Arm subsystem. */
  public static final class ArmConstants {

    private ArmConstants() {
      throw new IllegalStateException("ArmConstants Utility Class");
    }

    public static final int MOTOR_PORT = 6;

    // These are fake gains; in actuality these must be determined individually for each robot

    // Constants tunable through preferences
    public static final String ARM_KP_KEY = "ArmKP"; // The P gain for the PID controller
    public static final double DEFAULT_ARM_KP = 3.0;
    public static final String ARM_KS_KEY = "ArmKS"; // Static motor gain
    public static final double DEFAULT_KS_VOLTS = 0.5;
    public static final String ARM_KG_KEY = "ArmKG"; // Gravity gain
    public static final double DEFAULT_KG_VOLTS = 1.25;
    public static final String ARM_KV_KEY = "ArmKV"; // Velocity gain
    public static final double DEFAULT_KV_VOLTS_PER_SEC_PER_RAD = 0.8;
    public static final String ARM_VELOCITY_MAX_KEY = "ArmVelocityMax";
    public static final double DEFAULT_MAX_VELOCITY_RAD_PER_SEC = Units.degreesToRadians(90);
    public static final String ARM_ACCELERATION_MAX_KEY = "ArmAccelerationMax";
    public static final double DEFAULT_MAX_ACCELERATION_RAD_PER_SEC = Units.degreesToRadians(360);

    public static final double GEAR_RATIO = 1.0d / 200;
    public static final double ARM_RAD_PER_ENCODER_ROTATION = 2.0 * Math.PI * GEAR_RATIO;
    public static final double RPM_TO_RAD_PER_SEC = ARM_RAD_PER_ENCODER_ROTATION / 60;

    // Arm positions.  Horizontal = 0 radians. Assume arm starts at lowest (rest) position
    public static final double MIN_ANGLE_RADS = Units.degreesToRadians(-45);
    public static final double MAX_ANGLE_RADS = Units.degreesToRadians(120);
    public static final double ARM_OFFSET_RADS = MIN_ANGLE_RADS;
    public static final double ARM_HIGH_POSITION = Units.degreesToRadians(45);
    public static final double ARM_LOW_POSITION = Units.degreesToRadians(-40);
    public static final double POS_INCREMENT = Units.degreesToRadians(2); // For small adjustments
    public static final double POSITION_TOLERANCE = Units.degreesToRadians(1);
    public static final double VELOCITY_TOLERANCE = Units.degreesToRadians(1);
  }

  /** Constants used for assigning operator input. */
  public static final class OIConstants {

    private OIConstants() {
      throw new IllegalStateException("OIConstants Utility Class");
    }

    public static final int DRIVER_CONTROLLER_PORT = 0;
  }
}
