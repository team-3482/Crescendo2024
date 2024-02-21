// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.auto;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.LimelightConstants;
import frc.robot.Constants.NoteConstants;
import frc.robot.Constants.SwerveKinematics;
import frc.robot.lights.LEDSubsystem;
import frc.robot.lights.LEDSubsystem.LightState;
import frc.robot.limelight.LimelightSubsystem;
import frc.robot.swerve.SwerveSubsystem;

/** An example command that uses an example subsystem. */
public class DriveToNoteCommand extends Command {
    private final String LIMELIGHT = LimelightConstants.BACK_LIMELIGHT;

    private LimelightSubsystem limelightSubsystem;
    private SwerveSubsystem swerveSubsystem;

    private final SlewRateLimiter driveLimiter;
    private PIDController pidController;

    /**
    * Creates a new CenterNoteCommand.
    *
    * @param subsystem The subsystem used by this command.
    */
    public DriveToNoteCommand() {
        this.limelightSubsystem = LimelightSubsystem.getInstance();
        this.swerveSubsystem = SwerveSubsystem.getInstance();

        this.driveLimiter = new SlewRateLimiter(NoteConstants.NOTE_DRIVE_SLEW_RATE_LIMIT);
        this.pidController = new PIDController(
            NoteConstants.DRIVING_SPEED_PID_CONTROLLER.KP,
            NoteConstants.DRIVING_SPEED_PID_CONTROLLER.KI,
            NoteConstants.DRIVING_SPEED_PID_CONTROLLER.KD);
        this.pidController.setTolerance(NoteConstants.DRIVING_SPEED_PID_CONTROLLER.TOLERANCE);

        // Use addRequirements() here to declare subsystem dependencies.
        addRequirements(swerveSubsystem);
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
        if (!limelightSubsystem.hasTarget(LIMELIGHT)) {
            LEDSubsystem.getInstance().setLightState(LightState.WARNING);
            return;
        }
        LEDSubsystem.getInstance().setLightState(LightState.SOLID_GREEN);
        pidController.reset();
    }

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {
        double targetArea = limelightSubsystem.getTargetArea();

        double drivingSpeed = pidController.calculate(targetArea, NoteConstants.TARGET_AREA_TARGET);
        drivingSpeed = driveLimiter.calculate(drivingSpeed) * SwerveKinematics.DRIVE_SPEED_COEFFICENT;

        // Negative drivingSpeed because the note detection occurs opposite of the heading
        ChassisSpeeds chassisSpeeds = new ChassisSpeeds(-drivingSpeed, 0, 0);

        swerveSubsystem.setChassisSpeeds(chassisSpeeds);
    }

    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {
        swerveSubsystem.stopModules();
        LEDSubsystem.getInstance().setLightState(LightState.OFF);
    }

    // Returns true when the command should end.
    @Override
    public boolean isFinished() {
        return pidController.atSetpoint();
    }
}