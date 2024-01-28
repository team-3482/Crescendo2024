// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import java.util.function.Function;
import java.util.function.Supplier;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.Constants.OrbitConstants;
import frc.robot.subsystems.LimelightSubsystem;
import frc.robot.subsystems.SwerveSubsystem;

public class SwerveOrbit extends Command {
    // Instance of the subsystems
    private SwerveSubsystem swerveSubsystem;
    private LimelightSubsystem limelightSubsystem;
    // Instances of suppliers that will gather the inputs from the controller
    private final Supplier<Double> xSpeedFunction;
    private final Supplier<Double> ySpeedFunction;
    private final Supplier<Boolean> fieldOrientedFunction;
    private final Supplier<Boolean> fineControlFunction;
    private final boolean enableDPadInput;
    private final Function<Integer, Boolean> povFunction;

    // Instances of Rate Limiters to ensure that the robot moves smoothly
    private final SlewRateLimiter xLimiter;
    private final SlewRateLimiter yLimiter;
    private final SlewRateLimiter turningLimiter;
    private PIDController pid;

    /**
    * Creates new Swerve Drive Command
    * 
    * @param swerveSubsystem       - instance of swerve subsystem
    * @param xSpeedFunction        - function that will return the driver input for
    *                              the x direction
    * @param ySpeedFunction        - function that will return the driver input for
    *                              the y direction
    * @param turningSpeedFunction  - function that will return the driver input for
    *                              turing the robot
    * @param fieldOrientedFunction - function that will return if the driver wants
    *                              to be field Oriented or robot oriented
    */
    public SwerveOrbit(SwerveSubsystem swerveSubsystem, LimelightSubsystem limelightSubsystem,
        Supplier<Double> xSpeedFunction, Supplier<Double> ySpeedFunction,
        Supplier<Boolean> fieldOrientedFunction, Supplier<Boolean> fineControlFunction,
        boolean enableDPadInput, Function<Integer, Boolean> povFunction) {
        
        this.swerveSubsystem = swerveSubsystem;
        this.limelightSubsystem = limelightSubsystem;
        this.xSpeedFunction = xSpeedFunction;
        this.ySpeedFunction = ySpeedFunction;
        this.fieldOrientedFunction = fieldOrientedFunction;
        this.fineControlFunction = fineControlFunction;
        this.enableDPadInput = enableDPadInput;
        this.povFunction = povFunction;

        this.xLimiter = new SlewRateLimiter(OrbitConstants.MAX_DRIVE_ACCELERATION_METERS_PER_SECOND_SQUARED_ORBIT);
        this.yLimiter = new SlewRateLimiter(OrbitConstants.MAX_DRIVE_ACCELERATION_METERS_PER_SECOND_SQUARED_ORBIT);
        this.turningLimiter = new SlewRateLimiter(
        Constants.SwerveKinematics.MAX_TURN_ACCELERATION_RADIANS_PER_SECOND_SQUARED);
        pid = new PIDController(
            OrbitConstants.KP, OrbitConstants.KI, OrbitConstants.KD);
        // Adds the swerve subsyetm to requirements to ensure that it is the only class
        // modifying its data at a single time
        // Do not require limelight subsystem because it is getter functions only
        this.addRequirements(this.swerveSubsystem); 
    }

    @Override
    public void execute() {
        if (limelightSubsystem.getID() <= 0) {
            swerveSubsystem.stopModules();
            return;
        }
        // Gets the driver's input
        double xSpeed = xSpeedFunction.get();
        double ySpeed = ySpeedFunction.get();
        boolean fineControl = fineControlFunction.get();
        
        double[] botpose = limelightSubsystem.getBotPoseTargetSpace();        
        // No angle difference if the bot is within the allowed deviation
        double turningSpeed = pid.calculate(
            swerveSubsystem.getHeading(), Math.toDegrees(Math.atan2(botpose[0], -botpose[2])))
            / 60 * 1.1;

        // Checks for controller deadband in case joysticks do not return perfectly to origin
        xSpeed = Math.abs(xSpeed) > Constants.ControllerConstants.DEADBAND ? xSpeed : 0.0;
        ySpeed = Math.abs(ySpeed) > Constants.ControllerConstants.DEADBAND ? ySpeed : 0.0;

        // Limits the input to ensure smooth and depending on if fine control is active
        xSpeed = xLimiter.calculate(xSpeed) * Constants.SwerveKinematics.MAX_DRIVE_SPEED_METERS_PER_SECOND
            / (fineControl ? OrbitConstants.ORBIT_FINEST_MOVEMENT : OrbitConstants.ORBIT_FINER_MOVEMENT);
        ySpeed = yLimiter.calculate(ySpeed) * Constants.SwerveKinematics.MAX_DRIVE_SPEED_METERS_PER_SECOND
            / (fineControl ? OrbitConstants.ORBIT_FINEST_MOVEMENT : OrbitConstants.ORBIT_FINER_MOVEMENT);
        turningSpeed = turningLimiter.calculate(turningSpeed)
            * Constants.SwerveKinematics.MAX_DRIVE_ANGULAR_SPEED_RADIANS_PER_SECOND;
        
        // Creates the chassis speeds from the driver input depending on current orientation
        ChassisSpeeds chassisSpeeds;
        int[] dPadSpeeds = this.calculateDPad();
        if (this.enableDPadInput && (dPadSpeeds[0] != 0 || dPadSpeeds[1] != 0)) {
            if (fieldOrientedFunction.get()) {
                chassisSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(
                    dPadSpeeds[0], dPadSpeeds[1], 0, swerveSubsystem.getRotation2d());
            }
            else {
                chassisSpeeds = new ChassisSpeeds(dPadSpeeds[0], dPadSpeeds[1], 0);
            }
        }
        else {
            if (fieldOrientedFunction.get()) {
                chassisSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(
                    xSpeed, ySpeed, turningSpeed, swerveSubsystem.getRotation2d());
            }
            else {
                chassisSpeeds = new ChassisSpeeds(xSpeed, ySpeed, turningSpeed);
            }
        }

        // Converts the chassis speeds to module states and sets them as the desired
        // ones for the modules
        swerveSubsystem.setChassisSpeeds(chassisSpeeds);
        // Ouputs the swerve system information
        swerveSubsystem.outputEncoderValues();
    }

    /**
     * Calculates and returns D-Pad input
     *
     * @return the x and y speeds between -1 and 1
     */
    public int[] calculateDPad() {
        int[] speeds = new int[]{0, 0};
        
        // Up
        if (povFunction.apply(315) || povFunction.apply(0) || povFunction.apply(45)) {
            speeds[0] = 1;
        }
        // Down
        else if (povFunction.apply(225) || povFunction.apply(180) || povFunction.apply(135)) {
            speeds[0] = -1;
        }
        // Left
        if (povFunction.apply(225) || povFunction.apply(270) || povFunction.apply(315)) {
            speeds[1] = 1;
        }
        // Right
        else if (povFunction.apply(45) || povFunction.apply(90) || povFunction.apply(135)) {
            speeds[1] = -1;
        }
        return speeds;
    }

    /**
    * Makes the swerve modules stop when the command ends or is interrupted
    * 
    * @param interrupted
    */
    @Override
    public void end(boolean interrupted) {
        swerveSubsystem.stopModules();
    }

    /**
    * Returns false because this command should run until the user releases the button
    * 
    * @return boolean - always false
    */
    @Override
    public boolean isFinished() {
        return false;
    }
}
