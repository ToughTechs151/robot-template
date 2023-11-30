// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.OIConstants;
import frc.robot.commands.ExampleCommand;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.ExampleSubsystem;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();

  private final ExampleCommand autoCommand =
      new ExampleCommand("ExampleCommand", this.exampleSubsystem);
  private PowerDistribution pdp = new PowerDistribution();
  private final ArmSubsystem robotArm = new ArmSubsystem();

  // The driver's controller
  private CommandXboxController driverController =
      new CommandXboxController(OIConstants.DRIVER_CONTROLLER_PORT);

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {

    // Configure the button bindings
    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    // Move the arm to neutral (starting) position when the 'A' button is pressed.
    driverController
        .a()
        .onTrue(
            Commands.runOnce(
                () -> {
                  robotArm.setGoal(Constants.ArmConstants.ARM_OFFSET_RADS);
                  robotArm.enable();
                },
                robotArm));

    // Move the arm to the goal position when the 'B' button is pressed.
    driverController
        .b()
        .onTrue(
            Commands.runOnce(
                () -> {
                  robotArm.setGoal(Constants.ArmConstants.ARM_GOAL_POSITION);
                  robotArm.enable();
                },
                robotArm));

    // Shift position down a small amount when the POV Down is pressed.
    driverController
        .povDown()
        .onTrue(
            Commands.runOnce(
                () -> {
                  robotArm.setGoal(robotArm.decreasedGoal());
                  robotArm.enable();
                },
                robotArm));

    // Shift position up a small amount when the POV Down is pressed.
    driverController
        .povUp()
        .onTrue(
            Commands.runOnce(
                () -> {
                  robotArm.setGoal(robotArm.increasedGoal());
                  robotArm.enable();
                },
                robotArm));

    /* Reset the encoders to zero when the 'Y' button is pressed.
    Should only be used when arm is in neutral (starting) position. */
    driverController.y().onTrue(Commands.runOnce(robotArm::resetPosition));

    // Disable the arm controller when the 'X' button is pressed.
    driverController.x().onTrue(Commands.runOnce(robotArm::disable));
  }

  /**
   * Disables all ProfiledPIDSubsystem and PIDSubsystem instances. This should be called on robot
   * disable to prevent integral windup.
   */
  public void disablePIDSubsystems() {
    robotArm.disable();
    DataLogManager.log("disablePIDSubsystems");
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return this.autoCommand;
  }

  /**
   * Use this to get the PDP for data logging.
   *
   * @return The PowerDistribution module.
   */
  public PowerDistribution getPdp() {
    return this.pdp;
  }

  /**
   * Use this to get the Arm Subsystem
   *
   * @return the command to run in autonomous
   */
  public ArmSubsystem getArmSubsystem() {
    return robotArm;
  }
}
