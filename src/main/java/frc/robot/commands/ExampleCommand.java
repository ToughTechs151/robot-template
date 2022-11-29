// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.DataLogging;
import frc.robot.subsystems.ExampleSubsystem;

/** An example command that uses an example subsystem. */
public class ExampleCommand extends CommandBase {

  private final ExampleSubsystem subsystem;

  /**
   * Creates a new ExampleCommand.
   *
   * @param commandName Name of the command
   * @param subsystem Subsystem it requires
   */
  public ExampleCommand(String commandName, ExampleSubsystem subsystem) {
    this.subsystem = subsystem;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(this.subsystem);
    this.setName(commandName);
    DataLogging.getInstance().logCommand(this.subsystem.getName(), commandName, this);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    /* Place initialization here */
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    /* Place execute code here */
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    /* Place clean up code here */
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
