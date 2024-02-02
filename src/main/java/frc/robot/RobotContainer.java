// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.ControllerConstants;
import frc.robot.Constants.LEDConstants;
import frc.robot.commands.SwerveDrive;
import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.commands.LED;
import frc.robot.commands.MIDI;
import frc.robot.subsystems.LEDSubsystem;
import frc.robot.subsystems.MIDISubsystem;

public class RobotContainer {
  // Singleton design pattern
  public static RobotContainer instance;

  /**
   * Gets the instance of the RobotContainer
   * 
   * @return instance
   */
  public static RobotContainer getInstance() {
    if (instance == null) {
      instance = new RobotContainer();
    }
    return instance;
  }

  // Instance of the Subsystems
  private SwerveSubsystem swerveSubsystem;
  private final LEDSubsystem LEDSubsystem = new LEDSubsystem();
  private final MIDISubsystem MIDISubsystem = new MIDISubsystem();
  // Instance of the controller used to drive the robot
  private CommandXboxController driveController;

  /**
   * Creates an instance of the robot controller
   */
  public RobotContainer() {
    this.swerveSubsystem = new SwerveSubsystem();
    this.driveController = new CommandXboxController(ControllerConstants.DRIVE_CONTROLLER_ID);

    // Sets the default command to driving swerve
    this.swerveSubsystem.setDefaultCommand(new SwerveDrive(
        swerveSubsystem,
        () -> -driveController.getLeftY(),
        () -> -driveController.getLeftX(),
        // () ->
        // driveController.getRawAxis(Constants.ControllerConstants.DRIVE_ROT_AXIS),
        () -> driveController.getLeftTriggerAxis() - driveController.getRightTriggerAxis(),
        () -> !driveController.getHID().getAButton(),
        () -> driveController.getHID().getRightBumper()));
    // () -> driveController.getHID().getXButton(),
    // () -> driveController.getHID().getBButton()));

    configureBindings();
  }

  /**
   * Configures the button bindings of the controllers
   */
  private void configureBindings() {
    driveController.y().whileTrue(Commands.run(() -> swerveSubsystem.zeroHeading()));

    driveController.a().whileTrue(new LED(LEDSubsystem, LEDConstants.redColor));
    driveController.b().whileTrue(new LED(LEDSubsystem, LEDConstants.blueColor));

    driveController.x().whileTrue(new MIDI(MIDISubsystem, "Entry_of_the_Gladiators.mid"));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return Commands.run(() -> {
    });
  }
}
