package frc.sim;

import edu.wpi.first.math.util.Units;
import frc.robot.Constants.ArmConstants;

/** Constants utility class for the arm simulation. */
public final class Constants {

  private Constants() {
    throw new IllegalStateException("Utility class");
  }

  /** Arm simulation constants. */
  public static final class ArmSim {
    private ArmSim() {
      throw new IllegalStateException("ArmSim Utility Class");
    }

    public static final double ARM_REDUCTION = 1 / ArmConstants.GEAR_RATIO;
    public static final double ARM_MASS_KG = 8.0;
    public static final double ARM_LENGTH_INCHES = 30;
    public static final double ARM_LENGTH_METERS = Units.inchesToMeters(ARM_LENGTH_INCHES);
    public static final double START_ANGLE_RADS = ArmConstants.MIN_ANGLE_RADS;
    public static final int ENCODER_PRR =
        4096; // Only used to simulate noise in position measurement
    public static final double ENCODER_DISTANCE_PER_PULSE =
        2.0 * Math.PI / ENCODER_PRR * ArmConstants.GEAR_RATIO;
  }
}
