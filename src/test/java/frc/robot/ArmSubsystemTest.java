package frc.robot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.ArmConstants;
import frc.robot.subsystems.ArmSubsystem;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArmSubsystemTest {

  private Map<String, Double> telemetryDoubleMap = new HashMap<>();
  private Map<String, Boolean> telemetryBooleanMap = new HashMap<>();

  private ArmSubsystem arm;

  @BeforeEach
  public void initEach() {
    CANSparkMax motor = new CANSparkMax(1, MotorType.kBrushless);
    arm = new ArmSubsystem(motor);
  }

  @AfterEach
  public void closeArm() {
    arm.close(); // motor is closed from the arm close method
  }

  @Test
  @DisplayName("Test constructor and initialization.")
  void testConstructor() {
    /* We haven't enabled it yet, so the motor command should still be zero. */
    // NOTE: this is probably always 0 in test without simulated motor
    assertThat(arm.getVoltageCommand()).isZero();
    /* Position should be set to starting position*/
    assertThat(arm.getMeasurement()).isEqualTo(ArmConstants.ARM_OFFSET_RADS);
  }

  @Test
  @DisplayName("Test move command.")
  void testMoveCommand() {

    // System.out.println("DS Enabled: " + DriverStationSim.getEnabled());
    // DriverStationSim.setEnabled(true);
    // System.out.println("DS Enabled: " + DriverStationSim.getEnabled());

    /* Create a command to move the arm then initialize */
    // Command moveCommandOrig = arm.moveToPositionOrig(Constants.ArmConstants.ARM_LOW_POSITION);
    Command moveCommand = arm.moveToPosition(Constants.ArmConstants.ARM_LOW_POSITION);
    System.out.println("Initialize");
    // moveCommandOrig.initialize(); // ok
    moveCommand.initialize();

    /* Run the periodic method to generate telemetry and verify it was published */
    System.out.println("periodic");
    arm.periodic();
    int numEntries = readTelemetry();
    assertThat(numEntries).isPositive();
    assertEquals(
        Units.radiansToDegrees(ArmConstants.ARM_LOW_POSITION),
        telemetryDoubleMap.get("Arm Goal"),
        0.01);

    System.out.println("move execute");
    // moveCommandOrig.execute(); // Doesn't actually execute
    moveCommand.execute(); // ok
    // CommandScheduler scheduler = CommandScheduler.getInstance();
    // scheduler.enable();
    // scheduler.schedule(moveCommandOrig);
    // scheduler.run();
    // System.out.println("Scheduled: " + scheduler.isScheduled(moveCommandOrig)); // false
    // System.out.println("Composed: " + scheduler.isComposed(moveCommandOrig)); // false

    System.out.println("periodic");
    arm.periodic();
    readTelemetry();

    /*  Motor command should be positive to move arm up. */
    assertThat(telemetryDoubleMap.get("Arm Voltage")).isPositive();
    // System.out.println("Enable: " + telemetryBooleanMap.get("Arm Enabled"));
    assertThat(telemetryBooleanMap.get("Arm Enabled")).isTrue();
  }

  @Test
  @DisplayName("Test range limit and hold.")
  void testLimitAndHoldCommand() {

    /* Try a command to move the arm above the limit */
    Command moveCommand = arm.moveToPosition(Constants.ArmConstants.MAX_ANGLE_RADS + 0.1);
    moveCommand.initialize();
    arm.periodic();
    readTelemetry();
    assertEquals(
        Units.radiansToDegrees(ArmConstants.MAX_ANGLE_RADS),
        telemetryDoubleMap.get("Arm Goal"),
        0.01);

    /* Verify that the hold command runs the controller */
    Command moveCommandHigh = arm.moveToPosition(Constants.ArmConstants.ARM_HIGH_POSITION);
    Command holdCommand = arm.holdPosition();
    // Initialize to set goal but don't execute so hold can be checked
    moveCommandHigh.initialize();
    holdCommand.execute();

    System.out.println("periodic");
    arm.periodic();
    readTelemetry();

    /*  Motor command should be positive to move arm up. */
    assertThat(telemetryDoubleMap.get("Arm Voltage")).isPositive();
    // System.out.println("Enable: " + telemetryBooleanMap.get("Arm Enabled"));
    assertThat(telemetryBooleanMap.get("Arm Enabled")).isTrue();
  }

  @Test
  @DisplayName("Test shift down and up commands.")
  void testShiftDownCommand() {
    Command moveCommand = arm.moveToPositionOrig(Constants.ArmConstants.ARM_LOW_POSITION);
    moveCommand.initialize();
    Command upCommand = arm.shiftUp();

    upCommand.initialize();
    arm.periodic();
    readTelemetry();
    assertEquals(
        Units.radiansToDegrees(ArmConstants.ARM_LOW_POSITION + ArmConstants.POS_INCREMENT),
        telemetryDoubleMap.get("Arm Goal"),
        0.01);

    Command downCommand = arm.shiftDown();
    downCommand.initialize();
    arm.periodic();
    readTelemetry();
    // Currently up and down increments are the same. Update if that changes.
    assertEquals(
        Units.radiansToDegrees(
            ArmConstants.ARM_LOW_POSITION
                + ArmConstants.POS_INCREMENT
                - ArmConstants.POS_INCREMENT),
        telemetryDoubleMap.get("Arm Goal"),
        0.01);
  }

  /* Read in telemetry values from the network table and store in maps */
  private int readTelemetry() {
    NetworkTable telemetryTable = NetworkTableInstance.getDefault().getTable("SmartDashboard");
    Set<String> telemetryKeys = telemetryTable.getKeys();

    for (String keyName : telemetryKeys) {
      NetworkTableType entryType = telemetryTable.getEntry(keyName).getType();

      if (entryType == NetworkTableType.kDouble) {
        telemetryDoubleMap.put(keyName, telemetryTable.getEntry(keyName).getDouble(-1));
      } else if (entryType == NetworkTableType.kBoolean) {
        telemetryBooleanMap.put(keyName, telemetryTable.getEntry(keyName).getBoolean(false));
      }
    }

    return telemetryKeys.size();
  }

  // *** Available Telemetry Keys ***
  // "Arm Enabled"
  // "Arm Goal"
  // "Arm Angle"
  // "Arm Velocity"
  // "Arm Voltage"
  // "Arm Current"
  // "Arm Feedforward"
  // "Arm PID output"
  // "Arm SetPt Pos"
  // "Arm SetPt Vel"
}
