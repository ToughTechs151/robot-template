// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
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
import org.mockito.AdditionalMatchers;

class ArmSubsystemTest {
  private static final double DELTA = 5e-3;
  private Map<String, Double> telemetryDoubleMap = new HashMap<>();
  private Map<String, Boolean> telemetryBooleanMap = new HashMap<>();

  private ArmSubsystem.Hardware armHardware;
  private ArmSubsystem arm;
  private CANSparkMax mockMotor;
  private RelativeEncoder mockEncoder;

  @BeforeEach
  public void initEach() {
    // Create mock hardware devices
    mockMotor = mock(CANSparkMax.class);
    mockEncoder = mock(RelativeEncoder.class);

    // Reset preferences to default values so test results are consistent
    RobotPreferences.resetPreferences();

    // Create subsystem object using mock hardware
    armHardware = new ArmSubsystem.Hardware(mockMotor, mockEncoder);
    arm = new ArmSubsystem(armHardware);
  }

  @AfterEach
  public void closeArm() {
    arm.close(); // motor is closed from the arm close method
  }

  @Test
  @DisplayName("Test constructor and initialization.")
  void testConstructor() {
    // We haven't enabled it yet, so command to motor and saved value should be zero.
    verify(mockMotor).setVoltage(0.0);
    assertThat(arm.getVoltageCommand()).isZero();

    // Position should be set to starting position
    assertThat(arm.getMeasurement()).isEqualTo(ArmConstants.ARM_OFFSET_RADS);
  }

  @Test
  @DisplayName("Test move command and disable.")
  void testMoveCommand() {

    // Create a command to move the arm then initialize
    Command moveCommand = arm.moveToPosition(Constants.ArmConstants.ARM_LOW_POSITION);
    moveCommand.initialize();

    // Run the periodic method to generate telemetry and verify it was published
    arm.periodic();
    int numEntries = readTelemetry();
    assertThat(numEntries).isPositive();
    assertEquals(
        Units.radiansToDegrees(ArmConstants.ARM_LOW_POSITION),
        telemetryDoubleMap.get("Arm Goal"),
        DELTA);

    // Execute the command to run the controller
    moveCommand.execute();
    arm.periodic();
    readTelemetry();
    assertThat(telemetryDoubleMap.get("Arm Voltage")).isPositive();
    assertThat(telemetryBooleanMap.get("Arm Enabled")).isTrue();

    // When disabled mMotor should be commanded to zero
    arm.disable();
    arm.periodic();
    readTelemetry();
    verify(mockMotor, times(2)).setVoltage(0.0);
    assertThat(telemetryDoubleMap.get("Arm Voltage")).isZero();
    assertThat(telemetryBooleanMap.get("Arm Enabled")).isFalse();
  }

  @Test
  @DisplayName("Test Motor and Encoder Sensors.")
  void testSensors() {

    // Set values for mocked sensors
    final double fakeCurrent = -3.3;
    when(mockMotor.getOutputCurrent()).thenReturn(fakeCurrent);
    final double fakePosition = 1.5;
    when(mockEncoder.getPosition()).thenReturn(fakePosition);
    final double fakeVelocity = 0.123;
    when(mockEncoder.getVelocity()).thenReturn(fakeVelocity);

    // The motor voltage should be set twice: once to 0 when configured and once  to a
    // positive value when controller is run.
    Command moveCommand = arm.moveToPosition(Constants.ArmConstants.ARM_LOW_POSITION);
    moveCommand.initialize();
    moveCommand.execute();
    verify(mockMotor, times(2)).setVoltage(anyDouble());
    verify(mockMotor).setVoltage(0.0);
    verify(mockMotor, times(1)).setVoltage(AdditionalMatchers.gt(0.0));

    // This unused code is provided as an example of looking for a specific value.
    // This value was cheated by running working code as an example since calculating actual
    // controller expected values is difficult.  Instead the test above just checks direction
    // of the command, and controller response tests are done in simulation by checking desired
    // response over time.
    //
    // final double expectedCommand = 0.34092;
    // verify(mockMotor, times(1)).setVoltage(AdditionalMatchers.eq(expectedCommand, DELTA));

    // Alternative method: capture values and then use them in a test criteria
    // ArgumentCaptor<Double> argument = ArgumentCaptor.forClass(Double.class);
    // verify(mockMotor).setVoltage(argument.capture()); // Can use this if only called once
    // verify(mockMotor, times(2)).setVoltage(argument.capture());
    // assertEquals(expectedCommand, argument.getValue(), DELTA);

    // Test position measurements from the encoder
    assertThat(arm.getMeasurement()).isEqualTo(ArmConstants.ARM_OFFSET_RADS + fakePosition);

    // Check that telemetry was sent to dashboard
    arm.periodic();
    readTelemetry();
    assertEquals(fakeCurrent, telemetryDoubleMap.get("Arm Current"), DELTA);
    assertEquals(
        Units.radiansToDegrees(ArmConstants.ARM_OFFSET_RADS + fakePosition),
        telemetryDoubleMap.get("Arm Angle"),
        DELTA);
    assertEquals(
        Units.radiansToDegrees(fakeVelocity), telemetryDoubleMap.get("Arm Velocity"), DELTA);
  }

  @Test
  @DisplayName("Test range limit and hold.")
  void testLimitAndHoldCommand() {

    // Try a command to move the arm above the limit
    Command moveCommand = arm.moveToPosition(Constants.ArmConstants.MAX_ANGLE_RADS + 0.1);
    moveCommand.initialize();
    arm.periodic();
    readTelemetry();
    assertEquals(
        Units.radiansToDegrees(ArmConstants.MAX_ANGLE_RADS),
        telemetryDoubleMap.get("Arm Goal"),
        DELTA);

    // Verify that the hold command runs the controller
    Command moveCommandHigh = arm.moveToPosition(Constants.ArmConstants.ARM_HIGH_POSITION);
    Command holdCommand = arm.holdPosition();
    // Initialize to set goal but don't execute so hold can be checked
    moveCommandHigh.initialize();
    holdCommand.execute();
    arm.periodic();
    readTelemetry();

    // Motor command should be positive to move arm up.
    assertThat(telemetryDoubleMap.get("Arm Voltage")).isPositive();
    assertThat(telemetryBooleanMap.get("Arm Enabled")).isTrue();
  }

  @Test
  @DisplayName("Test shift down and up commands.")
  void testShiftDownCommand() {
    Command moveCommand = arm.moveToPositionOrig(Constants.ArmConstants.ARM_LOW_POSITION);
    Command upCommand = arm.shiftUp();

    // Command to a position and then shift up
    moveCommand.initialize();
    upCommand.initialize();
    arm.periodic();
    readTelemetry();
    assertEquals(
        Units.radiansToDegrees(ArmConstants.ARM_LOW_POSITION + ArmConstants.POS_INCREMENT),
        telemetryDoubleMap.get("Arm Goal"),
        DELTA);

    // Shift down
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
        DELTA);
  }

  // ---------- Utility Functions --------------------------------------

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
