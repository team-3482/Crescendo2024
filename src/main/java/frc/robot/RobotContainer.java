// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.ControllerConstants;
import frc.robot.Constants.IntakeConstants;
import frc.robot.Constants.ShooterConstants;
import frc.robot.Constants.ShuffleboardTabConstants;
import frc.robot.Constants.AutonConstants.PathfindingPosition;
import frc.robot.Constants.ShooterConstants.ShooterState;
import frc.robot.auto.PathfindToGoalCommand;
import frc.robot.intake.IntakeSubsystem;
import frc.robot.intake.SpinIntakeCommand;
import frc.robot.lights.LEDSubsystem;
import frc.robot.lights.LEDSubsystem.LightState;
import frc.robot.limelight.LimelightSubsystem;
import frc.robot.shooter.PivotShooterCommand;
import frc.robot.shooter.ShooterSubsystem;
import frc.robot.shooter.SterilizerSubsystem;
import frc.robot.swerve.SwerveDriveCommand;
import frc.robot.swerve.CenterSpeakerCommand;
import frc.robot.swerve.SwerveSubsystem;
import frc.robot.utilities.JSONManager;
import frc.robot.utilities.SequencedCommands;

public class RobotContainer {
    // Singleton design pattern
    private static RobotContainer instance;

    public static RobotContainer getInstance() {
        if (instance == null) {
            instance = new RobotContainer();
        }
        return instance;
    }

    // private final SendableChooser<Command> autoChooser;

    // Instance of the controllers used to drive the robot
    private CommandXboxController driveController;
    private CommandXboxController operatorController;

    /**
     * Creates an instance of the robot controller
     */
    public RobotContainer() {
        this.driveController = new CommandXboxController(ControllerConstants.DRIVE_CONTROLLER_ID);
        this.operatorController = new CommandXboxController(ControllerConstants.OPERATOR_CONTROLLER_ID);

        // Register named commands for pathplanner (do this after subsystem
        // initialization)
        NamedCommands.registerCommand("Pathfind AMP",
            new PathfindToGoalCommand(PathfindingPosition.AMP));
        NamedCommands.registerCommand("Pathfind SPEAKER",
            new PathfindToGoalCommand(PathfindingPosition.SPEAKER));

        // Sets the default command to driving swerve
        // SwerveSubsystem.getInstance().setDefaultCommand(new SwerveDriveCommand(
        //     () -> -driveController.getLeftY(),
        //     () -> -driveController.getLeftX(),
        //     () -> -driveController.getRightX(),
        //     () -> !(driveController.getHID().getLeftTriggerAxis() >= 0.5),
        //     () -> driveController.getHID().getRightTriggerAxis() >= 0.5,
        //     // D-Pad / POV movement
        //     ControllerConstants.DPAD_DRIVE_INPUT,
        //     (Integer angle) -> driveController.pov(angle).getAsBoolean()
        // ));
        configureBindings();
        initializeSubsystems();

        // autoChooser = AutoBuilder.buildAutoChooser(); // Default auto will be Commands.none()
        // Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
        //     .add("Auto Chooser", autoChooser)
        //     .withWidget(BuiltInWidgets.kComboBoxChooser)
        //     .withPosition(0, 3)
        //     .withSize(3, 2);
    }

    /** Configures the button bindings of the controllers */
    private void configureBindings() {
        // Driver controller
        // Zeroing functions
        // driveController.back()
            // .onTrue(Commands.runOnce(() -> SwerveSubsystem.getInstance().resetOdometryLimelight()));
        // driveController.start().onTrue(Commands.runOnce(() -> SwerveSubsystem.getInstance().zeroHeading()));
        
        // Cancel all scheduled commands and turn off LEDs
        driveController.b().onTrue(Commands.runOnce(() -> {
            CommandScheduler.getInstance().cancelAll();
            LEDSubsystem.getInstance().setCommandStopState(false);
        }));
        
        // driveController.rightBumper().onTrue(SequencedCommands.collectNote());
        // driveController.y().onTrue(SequencedCommands.intakeCommand());
        // driveController.x().onTrue(new PathfindToGoalCommand(PathfindingPosition.AMP));
        // driveController.a().onTrue(new PathfindToGoalCommand(PathfindingPosition.SPEAKER));
        
        // driveController.a().onTrue(new CenterSpeakerCommand()); // Need to test this
        
        driveController.a().whileTrue(new SpinIntakeCommand(IntakeConstants.INTAKE_SPEED));
        driveController.x().whileTrue(new SpinIntakeCommand(-IntakeConstants.INTAKE_SPEED));
        // driveController.a()
        //     .onTrue(new PivotShooterCommand(ShooterState.INTAKE))
        //     .onFalse(new PivotShooterCommand(ShooterState.VERTICAL));

        driveController.y().whileTrue(
            Commands.runEnd(
                () -> {
                    // ShooterSubsystem.getInstance().setShootingVelocities(new double[]{0.5, 0.5});
                    SterilizerSubsystem.getInstance().moveForward();
                },
                () -> {
                    // ShooterSubsystem.getInstance().setShootingVelocities();
                    SterilizerSubsystem.getInstance().moveBackward();
                    Timer.delay(0.1);
                    SterilizerSubsystem.getInstance().moveStop();
                }
        ));
        // driveController.x().whileTrue(
        //     Commands.runEnd(
        //         () -> {
        //             ShooterSubsystem.getInstance().setShootingVelocities(new double[]{0.5, 0.5});
        //             // SterilizerSubsystem.getInstance().moveForward();
        //         },
        //         () -> {
        //             ShooterSubsystem.getInstance().setShootingVelocities();
        //             // SterilizerSubsystem.getInstance().moveStop();
        //         }
        // ));

        // Operator controller
        // Line up to SPEAKER
        // operatorController.x().onTrue(new PathfindToGoalCommand(AutonConstants.SPEAKER));
        // Line up to AMP
        // operatorController.y().onTrue(new PathfindLineUp(SwerveSubsystem.getInstance(),
        // AutonConstants.AMP));

        // Move the pivot manually (last resort, not recommended)
        driveController.povUp().whileTrue(Commands.runEnd(
            () -> ShooterSubsystem.getInstance().setPivotSpeed(0.1),
            () -> ShooterSubsystem.getInstance().setPivotSpeed(0)
        ));
        driveController.povDown().whileTrue(Commands.runEnd(
            () -> ShooterSubsystem.getInstance().setPivotSpeed(-0.1),
            () -> ShooterSubsystem.getInstance().setPivotSpeed(0)
        ));
        // Move the intake manually (last resort, not recommended)
        driveController.povRight().whileTrue(Commands.runEnd(
            () -> IntakeSubsystem.getInstance().setPivotSpeed(0.1),
            () -> IntakeSubsystem.getInstance().setPivotSpeed(0)
        ));
        driveController.povLeft().whileTrue(Commands.runEnd(
            () -> IntakeSubsystem.getInstance().setPivotSpeed(-0.05),
            () -> IntakeSubsystem.getInstance().setPivotSpeed(0)
        ));
    }

    /** Creates instances of each subsystem so periodic runs */
    private void initializeSubsystems() {
        JSONManager.getInstance();
        // SwerveSubsystem.getInstance();
        // IntakeSubsystem.getInstance();
        // SterilizerSubsystem.getInstance();
        ShooterSubsystem.getInstance();
        // LEDSubsystem.getInstance();
        // LimelightSubsystem.getInstance();
    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
        // return autoChooser.getSelected();
        return null;
    }
}