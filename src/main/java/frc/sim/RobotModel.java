package frc.sim;

/* Code poached from https://github.com/RobotCasserole1736/TheBestSwerve2021 */

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.PDPSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import frc.robot.Robot;
import java.util.Random;

public class RobotModel {

  PDPSim simpdp;

  // Mechanical arm driven by motor with gear reduction for simulation purposes.
  // Works in conjunction with ArmSubsystem
  ArmModel simArm;

  // Differential drive simulation. Works in conjunction with DriveSubsystem
  DrivetrainModel simDrivetrain;

  Random random = new Random();
  private final boolean isReal;
  static final double QUIESCENT_CURRENT_DRAW_A = 2.0; // Misc electronics
  static final double BATTERY_NOMINAL_VOLTAGE = 13.2; // Nicely charged battery
  static final double BATTERY_NOMINAL_RESISTANCE = 0.040; // 40mOhm - average battery + cabling
  double currentDrawA = QUIESCENT_CURRENT_DRAW_A;
  double batteryVoltageV = BATTERY_NOMINAL_VOLTAGE;

  /**
   * Create robot simulation. Does nothing if not running a simulation. Called from Robot.java as a
   * class field.
   *
   * @param robot Robot
   */
  public RobotModel(Robot robot) {
    if (RobotBase.isSimulation()) {
      isReal = false;
    } else {
      isReal = true;
      return;
    }

    simArm = new ArmModel(robot.getRobotContainer().getArmSubsystem());

    simDrivetrain = new DrivetrainModel(robot.getRobotContainer().getDriveSubsystem());

    simpdp = new PDPSim(robot.getRobotContainer().getPdp());
    reset();
  }

  /** Update the simulation model. Call from simulationPeriodic method in robot.java. */
  public void update() {
    if (isReal) {
      return;
    }

    // Update subsystem simulations
    simArm.updateSim();
    simDrivetrain.updateSim();

    // Simulate battery voltage drop based on total simulated current
    double armCurrent = Math.abs(simArm.getSimCurrent());
    double leftDriveCurrent = Math.abs(simDrivetrain.getLeftSimCurrent());
    double rightDriveCurrent = Math.abs(simDrivetrain.getRightSimCurrent());
    double[] simCurrents = {armCurrent, leftDriveCurrent, rightDriveCurrent};

    double unloadedVoltage = batteryVoltageV * 0.98 + ((random.nextDouble() / 10) - 0.05);
    double loadedVoltage =
        BatterySim.calculateLoadedBatteryVoltage(
            unloadedVoltage, BATTERY_NOMINAL_RESISTANCE, simCurrents);
    RoboRioSim.setVInVoltage(loadedVoltage);

    simpdp.setVoltage(loadedVoltage);
    simpdp.setCurrent(0, currentDrawA + random.nextDouble());
    simpdp.setCurrent(7, armCurrent);
    simpdp.setCurrent(10, leftDriveCurrent / 2);
    simpdp.setCurrent(11, leftDriveCurrent / 2);
    simpdp.setCurrent(12, rightDriveCurrent / 2);
    simpdp.setCurrent(13, rightDriveCurrent / 2);
    simpdp.setTemperature(26.5);
  }

  /** Reset the simulation data. */
  public final void reset() {
    if (isReal) {
      return;
    }
    simpdp.resetData();
    RoboRioSim.resetData();
  }
}
