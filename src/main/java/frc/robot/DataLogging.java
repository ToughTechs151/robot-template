package frc.robot;

// Forked from FRC Team 2832 "The Livonia Warriors"

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.shuffleboard.EventImportance;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;

public class DataLogging {

  private Robot robot;
  private RobotContainer robotContainer;
  private NetworkTableEntry sbPdpTempEntry;
  private NetworkTableEntry sbPdpCurrentEntry;
  private NetworkTableEntry sbBatVoltEntry;
  private DoubleLogEntry loopTime;
  private PowerDistribution pdp;
  private double startTime;
  private ShuffleboardTab sbRobotTab;
  private boolean prevBrownoutState;
  private boolean prevDsConnectState;

  public DataLogging(Robot robot) {
    this.robot = robot;
  }

  /**
   * Runs when the Robot is initialized. This method should be called in the robotInit method in
   * Robot.java. The code should look like this:
   *
   * <pre>{@code
   * datalog = new DataLogging()
   * datalog.init();
   * }</pre>
   */
  public void init() {
    // Starts recording to data log
    DataLogManager.start();
    final DataLog log = DataLogManager.getLog();

    // Record both DS control and joystick data. To
    DriverStation.startDataLog(DataLogManager.getLog(), Constants.LOG_JS_DATA);

    sbRobotTab = Shuffleboard.getTab("Robot");

    sbPdpTempEntry = sbRobotTab.add("PDP Temp", -1).getEntry();
    sbPdpCurrentEntry = sbRobotTab.add("PDP Current", -1).getEntry();
    sbBatVoltEntry = sbRobotTab.add("Battery Voltage", -1).getEntry();
    prevBrownoutState = RobotController.isBrownedOut();
    prevDsConnectState = DriverStation.isDSAttached();

    loopTime = new DoubleLogEntry(log, "/robot/LoopTime");
  }

  /**
   * Runs at each loop slice.. This method should be called in the robotPeriodic method in
   * Robot.java. the code must be the last thing in the method.
   *
   * <pre>{@code
   * //must be at end
   * datalog.periodic();
   * }</pre>
   */
  public void periodic() {

    // Get the robotcontainer and the rest of the hardware. We do this the first time through the
    // periodic method because we started datalogging before we created a robot container. That way
    // we can log the events of creating the robot.
    if (robotContainer == null || pdp == null) {
      robotContainer = robot.getrobotContainer();
      pdp = robotContainer.getPdp();

      // Add hardware sendables here
      sbRobotTab.add("PDP", pdp);

      // Log configuration info here
      DataLogManager.log(String.format("PDP Can ID: %d", pdp.getModule()));
      DataLogManager.log(
          String.format("Brownout Voltage: %f", RobotController.getBrownoutVoltage()));
    }

    sbPdpTempEntry.setDouble(pdp.getTemperature());
    sbPdpCurrentEntry.setDouble(pdp.getTotalCurrent());
    sbBatVoltEntry.setDouble(RobotController.getBatteryVoltage());

    boolean newBrownoutState = RobotController.isBrownedOut();
    if (prevBrownoutState != newBrownoutState) {
      Shuffleboard.addEventMarker(
          "Brownout %s" + (newBrownoutState ? "Started" : "Ended"), EventImportance.kCritical);
      prevBrownoutState = newBrownoutState;
    }

    boolean newDsConnectState = DriverStation.isDSAttached();
    if (prevDsConnectState != newDsConnectState) {
      Shuffleboard.addEventMarker(
          "Driver Station is %s" + (newDsConnectState ? "Connected" : "Disconnected"),
          EventImportance.kHigh);
      prevDsConnectState = newDsConnectState;
    }
    loopTime.append(Timer.getFPGATimestamp() - startTime);
  }

  public void startLoopTime() {
    startTime = Timer.getFPGATimestamp();
  }
}
