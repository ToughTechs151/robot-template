package frc.sim;
/* Code poached from https://github.com/RobotCasserole1736/TheBestSwerve2021 */

import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.PDPSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import frc.robot.Robot;
import java.util.Random;

public class RobotModel {

  PDPSim simpdp;
  Random random = new Random();

  public RobotModel(Robot robot) {
    simpdp = new PDPSim(robot.getrobotContainer().getPdp());
    reset();
  }

  /** Update the simulation model. Call from simulationperiodic method in robot.java. */
  public void update() {
    RoboRioSim.setVInVoltage(12.1 + random.nextDouble());
    BatterySim.calculateDefaultBatteryLoadedVoltage(1.0);
    simpdp.setVoltage(12.3 + random.nextDouble());
    simpdp.setCurrent(0, 2.0 + random.nextDouble());
    simpdp.setCurrent(7, 2.0 + random.nextDouble());
    simpdp.setTemperature(26.5);
  }

  /** Reset the simulation data. */
  public void reset() {
    simpdp.resetData();
    RoboRioSim.resetData();
  }
}
