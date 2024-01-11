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
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.OIConstants;
import frc.robot.commands.ExampleCommand;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.ExampleSubsystem;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...

  // First we do things that are in all Robots.
  private PowerDistribution pdp = new PowerDistribution();
  // The driver's controller
  private CommandXboxController driverController =
      new CommandXboxController(OIConstants.DRIVER_CONTROLLER_PORT);

  // Now all the subsystems.
  // The Example.
  private final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
  private final ExampleCommand autoCommand =
      new ExampleCommand("ExampleCommand", this.exampleSubsystem);
  // The Arm.
  private final ArmSubsystem robotArm = new ArmSubsystem(ArmSubsystem.initializeHardware());
  // The drive.
  private final DriveSubsystem robotDrive = new DriveSubsystem();

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {

    // Configure the button bindings
    configureButtonBindings();

    // Configure default commands
    // Set the default drive command to split-stick tank drive
    this.robotDrive.setDefaultCommand(
        // A split-stick tank command, with left side forward/backward controlled by the left
        // joystick, and right side controlled by the right joystick.
        new RunCommand(
                () ->
                    this.robotDrive.tankDrive(
                        -this.driverController.getLeftY(),
                        -this.driverController.getRightY(),
                        this.driverController.rightBumper().getAsBoolean()),
                this.robotDrive)
            .withName("Drive: Tank"));
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    // Move the arm to the low position when the 'A' button is pressed.
    driverController
        .a()
        .onTrue(
            robotArm
                .moveToPosition(Constants.ArmConstants.ARM_LOW_POSITION)
                .withName("Arm: Move to Low Position"));

    // Move the arm to the high position when the 'B' button is pressed.
    driverController
        .b()
        .onTrue(
            robotArm
                .moveToPosition(Constants.ArmConstants.ARM_HIGH_POSITION)
                .withName("Arm: Move to High Position"));

    // Shift position down a small amount when the POV Down is pressed.
    driverController.povDown().onTrue(robotArm.shiftDown());

    // Shift position up a small amount when the POV Down is pressed.
    driverController.povUp().onTrue(robotArm.shiftUp());

    // Disable the arm controller when the 'X' button is pressed.
    // NOTE: This is intended for initial arm testing and should be removed in the final robot
    // to prevent accidental disable resulting in lowering of the arm.
    driverController.x().onTrue(Commands.runOnce(robotArm::disable));
  }

  /**
   * Disables all subsystems. This should be called on robot disable to prevent integrator windup in
   * subsystems with PID controllers. It also allows subsystems to setup disabled state so
   * simulation matches RoboRio behavior. Commands are canceled at the Robot level.
   */
  public void disableSubsystems() {
    robotArm.disable();
    robotDrive.disable();
    DataLogManager.log("disableSubsystems");
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
   * Use this to get the Arm Subsystem.
   *
   * @return the command to run in autonomous
   */
  public ArmSubsystem getArmSubsystem() {
    return robotArm;
  }

  /**
   * Use this to get the Drivetrain Subsystem.
   *
   * @return the Drivetrain Subsystem
   */
  public DriveSubsystem getDriveSubsystem() {
    return robotDrive;
  }
}
