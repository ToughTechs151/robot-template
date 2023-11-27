package frc.sim;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import frc.sim.Constants.ArmSim;


public class Arm implements AutoCloseable {
  // The P gain for the PID controller that drives this arm.
  private double armKp = ArmSim.DEFAULT_ARM_KP;
  private double armSetPointDegrees = ArmSim.DEFAULT_ARM_SETPOINT_DEGREES;

  // The arm gearbox represents a gearbox containing two Vex 775pro motors.
  private final DCMotor armGearbox = DCMotor.getVex775Pro(2);

  // Standard classes for controlling our arm
  private final PIDController controller = new PIDController(this.armKp, 0, 0);
  private final Encoder encoder = new Encoder(ArmSim.ENCODER_A_CHANNEL, ArmSim.ENCODER_B_CHANNEL);
  private final PWMSparkMax motor = new PWMSparkMax(ArmSim.MOTOR_PORT);

  // Simulation classes help us simulate what's going on, including gravity.
  // This arm sim represents an arm that can travel from -75 degrees (rotated down front)
  // to 255 degrees (rotated down in the back).
  private final SingleJointedArmSim armSim =
      new SingleJointedArmSim(
          this.armGearbox,
          ArmSim.ARM_REDUCTION,
          SingleJointedArmSim.estimateMOI(ArmSim.ARM_LENGTH, ArmSim.ARM_MASS),
          ArmSim.ARM_LENGTH,
          ArmSim.MIN_ANGLE_RADS,
          ArmSim.MAX_ANGLE_RADS,
          true,
          0,
          VecBuilder.fill(ArmSim.ARM_ENCODER_DIST_PER_PULSE) // Add noise with a std-dev of 1 tick
          );
  private final EncoderSim encoderSim = new EncoderSim(this.encoder);

  // Create a Mechanism2d display of an Arm with a fixed ArmTower and moving Arm.
  private final Mechanism2d mech2d = new Mechanism2d(60, 60);
  private final MechanismRoot2d armPivot = this.mech2d.getRoot("ArmPivot", 30, 30);
  private final MechanismLigament2d armTower =
      this.armPivot.append(new MechanismLigament2d("ArmTower", 30, -90));
  private final MechanismLigament2d armLig =
      this.armPivot.append(
          new MechanismLigament2d(
              "Arm",
              30,
              Units.radiansToDegrees(this.armSim.getAngleRads()),
              6,
              new Color8Bit(Color.kYellow)));

  /** Subsystem constructor. */
  public Arm() {
    this.encoder.setDistancePerPulse(ArmSim.ARM_ENCODER_DIST_PER_PULSE);

    // Put Mechanism 2d to SmartDashboard
    SmartDashboard.putData("Arm Sim", this.mech2d);
    this.armTower.setColor(new Color8Bit(Color.kBlue));

    // Set the Arm position setpoint and P constant to Preferences if the keys don't already exist
    Preferences.initDouble(ArmSim.ARM_POSITION_KEY, this.armSetPointDegrees);
    Preferences.initDouble(ArmSim.ARM_P_KEY, this.armKp);
  }

  /** Update the simulation model. */
  public void simulationPeriodic() {
    // In this method, we update our simulation of what our arm is doing
    // First, we set our "inputs" (voltages)
    this.armSim.setInput(this.motor.get() * RobotController.getBatteryVoltage());

    // Next, we update it. The standard loop time is 20ms.
    this.armSim.update(0.020);

    // Finally, we set our simulated encoder's readings and simulated battery voltage
    this.encoderSim.setDistance(this.armSim.getAngleRads());
    // SimBattery estimates loaded battery voltages
    RoboRioSim.setVInVoltage(
        BatterySim.calculateDefaultBatteryLoadedVoltage(this.armSim.getCurrentDrawAmps()));

    // Update the Mechanism Arm angle based on the simulated arm angle
    this.armLig.setAngle(Units.radiansToDegrees(this.armSim.getAngleRads()));
  }

  /** Load setpoint and kP from preferences. */
  public void loadPreferences() {
    // Read Preferences for Arm setpoint and kP on entering Teleop
    this.armSetPointDegrees =
        Preferences.getDouble(ArmSim.ARM_POSITION_KEY, this.armSetPointDegrees);
    if (this.armKp != Preferences.getDouble(ArmSim.ARM_P_KEY, this.armKp)) {
      this.armKp = Preferences.getDouble(ArmSim.ARM_P_KEY, this.armKp);
      this.controller.setP(this.armKp);
    }
  }

  /** Run the control loop to reach and maintain the setpoint from the preferences. */
  public void reachSetpoint() {
    var pidOutput =
        this.controller.calculate(
            this.encoder.getDistance(), Units.degreesToRadians(this.armSetPointDegrees));
    this.motor.setVoltage(pidOutput);
  }

  public void stop() {
    this.motor.set(0.0);
  }

  @Override
  public void close() {
    this.motor.close();
    this.encoder.close();
    this.mech2d.close();
    this.armPivot.close();
    this.controller.close();
    this.armLig.close();
  }
}
