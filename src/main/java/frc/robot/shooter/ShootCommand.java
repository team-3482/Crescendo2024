// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.shooter;

import java.util.Optional;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.ShooterConstants;
import frc.robot.lights.LEDSubsystem;
import frc.robot.lights.LEDSubsystem.LightState;
import frc.robot.swerve.SwerveSubsystem;

/** An example command that uses an example subsystem. */
public class ShootCommand extends Command {
    private double[] shootingSpeed;
    private boolean finished;
    private boolean manual;

    /**
    * Creates a new ShootCommand.
    *
    * @param manual the command does not end automatically
    */
    public ShootCommand(boolean manual) {
        this.manual = manual;
        // Use addRequirements() here to declare subsystem dependencies.
        addRequirements(ShooterSubsystem.getInstance());
    }
    
    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
        LEDSubsystem.getInstance().setLightState(LightState.CMD_INIT);
        if (!ShooterSubsystem.getInstance().canShoot) {
            end(true);
        }
        this.finished = false;

        double[] speeds = ShooterConstants.SHOOTER_MOTOR_SPEEDS;
        if (SwerveSubsystem.getInstance().getHeading() < 180) {
            this.shootingSpeed = speeds; 
        }
        else {
            this.shootingSpeed = new double[]{speeds[1], speeds[0]};
        }

        ShooterSubsystem.getInstance().setShootingVelocities(new double[]{
            shootingSpeed[0], shootingSpeed[1]
        });
    }

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {
        LEDSubsystem.getInstance().setLightState(LightState.CMD_RUNNING);
        
        double[] velocities = ShooterSubsystem.getInstance().getShootingVelocities();
        if (Units.radiansPerSecondToRotationsPerMinute(this.shootingSpeed[0] - velocities[0]) > ShooterConstants.ALLOWED_RPM_ERROR
            || Units.radiansPerSecondToRotationsPerMinute(this.shootingSpeed[1] - velocities[1]) > ShooterConstants.ALLOWED_RPM_ERROR
        ) return;

        Optional<Boolean> hasNote = SterilizerSubsystem.getInstance().hasNote();
        SterilizerSubsystem.getInstance().moveForward();
        if (this.manual) return;
        if (!hasNote.isPresent()) {
            Timer.delay(5);
            this.finished = true;
        } 
        else if (hasNote.get()) {
            Timer.delay(1);
        }
        else {
            this.finished = true;
        }
    }

    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {
        ShooterSubsystem.getInstance().setShootingVelocities(new double[]{0, 0});
        SterilizerSubsystem.getInstance().moveStop();

        LEDSubsystem.getInstance().setCommandStopState(interrupted);
    }

    // Returns true when the command should end.
    @Override
    public boolean isFinished() {
        return this.manual ? false : this.finished;
    }
}