package frc.robot;

// Forked from FRC Team 2832 "The Livonia Warriors"

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.util.datalog.StringLogEntry;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.EventImportance;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import java.util.Map;

public class DataLogging {

  private DoubleLogEntry loopTime;
  private double startTime;
  private ShuffleboardTab sbCommandsTab;
  private ShuffleboardLayout pdpWidget;
  private boolean everBrownout = false;
  private boolean prevDsConnectState;

  private DataLogging() {
    // Starts recording to data log
    DataLogManager.start();
    final DataLog log = DataLogManager.getLog();

    // Record both DS control and joystick data. To
    DriverStation.startDataLog(DataLogManager.getLog(), Constants.LOG_JOYSTICK_DATA);

    if (Constants.LW_TELEMETRY_ENABLE) {
      // In 2022 code this is the default, in 2023 the default changes
      // and they add the enable call.
      // LiveWindow.enableAllTelemetry()
    } else {
      LiveWindow.disableAllTelemetry();
    }

    ShuffleboardTab sbRobotTab = Shuffleboard.getTab("Robot");
    pdpWidget = sbRobotTab.getLayout("PDP", BuiltInLayouts.kGrid).withSize(3, 3);
    ShuffleboardLayout rcWidget =
        sbRobotTab.getLayout("RobotController", BuiltInLayouts.kGrid).withSize(3, 3);

    sbCommandsTab = Shuffleboard.getTab("Commands");

    /* sbRobotTab */
    rcWidget
        .addNumber("Batt Volt", RobotController::getBatteryVoltage)
        .withWidget(BuiltInWidgets.kVoltageView)
        .withProperties(Map.of("min", 0, "max", 13));
    rcWidget
        .addBoolean("Brown Out", RobotController::isBrownedOut)
        .withWidget(BuiltInWidgets.kBooleanBox)
        .withProperties(Map.of("Color when true", "Red", "Color when false", "Green"));
    rcWidget
        .addBoolean("Ever Browned Out", this::getEverBrownOut)
        .withWidget(BuiltInWidgets.kBooleanBox)
        .withProperties(Map.of("Color when true", "Red", "Color when false", "Green"));

    prevDsConnectState = DriverStation.isDSAttached();
    DataLogManager.log(String.format("Brownout Voltage: %f", RobotController.getBrownoutVoltage()));

    // Set the scheduler to log Shuffleboard events for command initialize,
    // interrupt, finish

    StringLogEntry commandLog = new StringLogEntry(log, "/command/event");
    CommandScheduler.getInstance()
        .onCommandInitialize(
            command -> commandLog.append("Command initialized:" + command.getName()));
    CommandScheduler.getInstance()
        .onCommandExecute(command -> commandLog.append("Command execute:" + command.getName()));
    CommandScheduler.getInstance()
        .onCommandInterrupt(
            command -> commandLog.append("Command interrupted" + command.getName()));
    CommandScheduler.getInstance()
        .onCommandFinish(command -> commandLog.append("Command finished" + command.getName()));
    commandLog.append("Opened commandlog");

    loopTime = new DoubleLogEntry(log, "/robot/LoopTime");
  }

  private static class InstanceHolder {
    private static final DataLogging instance = new DataLogging();
  }

  /**
   * Gets the dataloggins Singleton object.
   *
   * @return DataLogging
   */
  public static DataLogging getInstance() {
    return InstanceHolder.instance;
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

    if (RobotController.isBrownedOut()) {
      everBrownout = true;
    }

    boolean newDsConnectState = DriverStation.isDSAttached();
    if (prevDsConnectState != newDsConnectState) {
      Shuffleboard.addEventMarker(
          "Driver Station is %s" + (newDsConnectState ? "Connected" : "Disconnected"),
          EventImportance.kHigh);
      prevDsConnectState = newDsConnectState;
    }
    if (Constants.LOOP_TIMING_LOG) {
      loopTime.append(Timer.getFPGATimestamp() - startTime);
    }
  }

  /**
   * Called from robot.java immedately after the robotContainer is created.
   *
   * @param robotContainer The robotContainer just constructed.
   */
  public void dataLogRobotContainerInit(RobotContainer robotContainer) {

    PowerDistribution pdp;
    pdp = robotContainer.getPdp();

    // Add hardware sendables here
    // sbRobotTab.add("PDP", pdp).withWidget(BuiltInWidgets.kPowerDistribution)
    pdpWidget.add("PDP", pdp);

    // Log configuration info here
    DataLogManager.log(String.format("PDP Can ID: %d", pdp.getModule()));

    // Add values with supplier functions here.
    pdpWidget
        .addNumber("PDP Temp", pdp::getTemperature)
        .withWidget(BuiltInWidgets.kDial)
        .withProperties(Map.of("min", 15, "max", 50));
    pdpWidget.addNumber("PDP Current", pdp::getTotalCurrent);
    pdpWidget.addNumber("PDP Energy", pdp::getTotalEnergy);
    pdpWidget.addNumber("PDP Power", pdp::getTotalPower);
  }

  /**
   * Add a button for the command onto the Shuffleboard Commands tab.
   *
   * @param comName The name of the command.
   * @param com The command object.
   */
  public void logCommand(String comName, Sendable com) {
    sbCommandsTab.add(comName, com).withSize(2, 1);
  }

  /**
   * Add a button for the command in the subsystem group. Usually called in the command constructor.
   *
   * <pre>{@code
   * DataLogging.getInstance().logCommand(this.subsystem.getName(),
   * this.getName(), this);
   * }</pre>
   *
   * @param ssName The name of subsystem.
   * @param comName The name of the command.
   * @param com The command object.
   */
  public final void logCommand(String ssName, String comName, Sendable com) {
    sbCommandsTab.getLayout(ssName, BuiltInLayouts.kList).withSize(2, 0).add(comName, com);
    // ISSUE #2 Hide the command name label.
    // Add property to layout to set label position to HIDDEN.
    // See "Adding widgets to layouts" in Shuffleboard docs.
  }

  public void startLoopTime() {
    startTime = Timer.getFPGATimestamp();
  }

  public final boolean getEverBrownOut() {
    return this.everBrownout;
  }
}
