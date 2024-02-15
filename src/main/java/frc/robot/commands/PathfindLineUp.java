// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import java.util.Optional;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathConstraints;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.AutonConstants;
import frc.robot.subsystems.SwerveSubsystem;

/** A pathfinding command that uses limelight and swerve subsystems. */
public class PathfindLineUp extends Command {
    @SuppressWarnings("unused")
    private final SwerveSubsystem swerveSubsystem;
    private Command path;
    private Character goal;
    private boolean finished;

    /**
    * Creates a new PathfindAprilTagCommand.
    *
    * @param swerveSubsystem The swerve subsystem used by this command.
    * @param goal The location to line up at
    */
    public PathfindLineUp(SwerveSubsystem swerveSubsystem, Character goal) {
        this.swerveSubsystem = swerveSubsystem;
        this.goal = goal;
        this.finished = false;

        // Use addRequirements() here to declare subsystem dependencies.
        // addRequirements(swerveSubsystem);
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
        Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
        if (!alliance.isPresent() || this.path != null) {
            this.finished = true;
            return;
        }
        this.finished = false;

        Pose2d idealPosition = AutonConstants.IDEAL_TAG_POSITIONS.get(alliance.get()).get(this.goal);
        
        PathConstraints constraints = new PathConstraints(
            AutonConstants.MAX_LINEAR_VELOCITY,
            AutonConstants.MAX_LINEAR_ACCELERATION,
            AutonConstants.MAX_ANGULAR_VELOCITY,
            AutonConstants.MAX_ANGULAR_ACCELERATION);

        this.path = AutoBuilder.pathfindToPose(
            idealPosition,
            constraints,
            0, // Goal end velocity in meters/sec
            0); // Rotation delay distance in meters
        path.schedule();
    }

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {
        if (this.path.isFinished()) {
            this.finished = true; // Will also cancel path if it exists
        }
    }

    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {
        if (this.path != null) {
            this.path.cancel();
        }
        this.path = null;
    }

    // Returns true when the command should end.
    @Override
    public boolean isFinished() {
        return this.finished;
    }
}