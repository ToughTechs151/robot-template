// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj.simulation.XboxControllerSim;
import frc.robot.subsystems.ArmSubsystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("timing")
class RobotCommandTest {
  private Robot robot;
  private ArmSubsystem arm;
  private RobotContainer container;
  private Thread competitionThread;

  private XboxControllerSim xboxControllerSim;
  private static final double POS_DELTA = 0.5;
  private static final double TIME_STEP = 0.05;

  @BeforeEach
  void startThread() {
    System.out.println(" =================== Starting Robot for Unit Test =================== ");
    HAL.initialize(500, 0);
    SimHooks.pauseTiming();
    DriverStationSim.resetData();
    robot = new Robot();
    competitionThread = new Thread(robot::startCompetition);
    xboxControllerSim = new XboxControllerSim(Constants.OIConstants.DRIVER_CONTROLLER_PORT);

    competitionThread.start();
    SimHooks.stepTiming(0.0); // Wait for Notifiers
    container = robot.getRobotContainer();
    arm = container.getArmSubsystem();

    // Reset preferences to default values so test results are consistent
    RobotPreferences.resetPreferences();
  }

  @AfterEach
  void stopThread() {
    robot.endCompetition();
    try {
      competitionThread.interrupt();
      competitionThread.join();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    robot.close();
    RoboRioSim.resetData();
    DriverStationSim.resetData();
    DriverStationSim.notifyNewData();
    System.out.println(" =================== Stopped Robot for Unit Test =================== ");
  }

  @Disabled("Needs rework")
  @Test
  void armStartupTest() {
    // Verify that the arm stays at start when robot is started
    DriverStationSim.setAutonomous(false);
    DriverStationSim.setEnabled(false);
    DriverStationSim.notifyNewData();

    // Advance 10 time steps before enabling
    SimHooks.stepTiming(10 * TIME_STEP);

    // Ensure arm is still at start position and voltage command is 0.
    assertEquals(Constants.ArmConstants.ARM_OFFSET_RADS, arm.getMeasurement(), POS_DELTA);
    assertThat(arm.getVoltageCommand()).isZero();

    // Enable the robot, advance and ensure arm is still at start position.
    DriverStationSim.setEnabled(true);
    SimHooks.stepTiming(10 * TIME_STEP);

    assertEquals(Constants.ArmConstants.ARM_OFFSET_RADS, arm.getMeasurement(), POS_DELTA);
    assertThat(arm.getVoltageCommand()).isZero();

    // Press and then release the B button to reach goal high position
    xboxControllerSim.setBButton(true);
    xboxControllerSim.notifyNewData();
    SimHooks.stepTiming(0.1);
    xboxControllerSim.setBButton(false);
    xboxControllerSim.notifyNewData();

    // advance to let arm reach the new goal
    SimHooks.stepTiming(3.0);

    assertEquals(Constants.ArmConstants.ARM_HIGH_POSITION, arm.getMeasurement(), POS_DELTA);
    assertTrue(arm.atGoalPosition());

    // advance further to see that arm is held.
    SimHooks.stepTiming(0.5);

    assertEquals(Constants.ArmConstants.ARM_HIGH_POSITION, arm.getMeasurement(), POS_DELTA);

    // Disable the robot and check that motor command is 0
    DriverStationSim.setAutonomous(false);
    DriverStationSim.setEnabled(false);
    DriverStationSim.notifyNewData();
    SimHooks.stepTiming(0.5);

    assertThat(arm.getVoltageCommand()).isZero();
  }
}
