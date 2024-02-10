// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.LimelightHelpers;
import frc.robot.Constants.AutonConstants;
import frc.robot.Constants.LimelightConstants;
import frc.robot.Constants.ShuffleboardTabConstants;

public class LimelightSubsystem extends SubsystemBase {
    Field2d limelight_field = new Field2d();

    // Shuffleboard
    private GenericEntry SB_D_TID = Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
        .add("T-ID", 0)
        .withWidget(BuiltInWidgets.kTextView)
        .withPosition(3, 3)
        .withSize(1, 1)
        .getEntry();
    private GenericEntry SB_D_TSEE = Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
        .add("T-SEE", false)
        .withWidget(BuiltInWidgets.kBooleanBox)
        .withPosition(3, 0)
        .withSize(3, 3)
        .getEntry();
    
    private GenericEntry SB_F_TID = Shuffleboard.getTab(ShuffleboardTabConstants.FIELDS)
        .add("T-ID", 0)
        .withWidget(BuiltInWidgets.kTextView)
        .withPosition(14, 0)
        .withSize(2, 1)
        .getEntry();
    private GenericEntry SB_F_TSEE = Shuffleboard.getTab(ShuffleboardTabConstants.FIELDS)
        .add("T-SEE", false)
        .withWidget(BuiltInWidgets.kBooleanBox)
        .withPosition(14, 1)
        .withSize(2, 3)
        .getEntry();
    
    /** Creates a new LimelightSubsystem. */
    public LimelightSubsystem() {
        Shuffleboard.getTab(ShuffleboardTabConstants.FIELDS)
            .add("Field (limelight)", limelight_field)
            .withWidget(BuiltInWidgets.kField)
            .withPosition(7, 0)
            .withSize(7, 4);
    }
    
    /**
     * Horizontal Offset From Crosshair To Target
     *
     * @return offset
     */
    public double getTX() {
        return LimelightHelpers.getTX(LimelightConstants.FRONT_LIMELIGHT);
    }
    
    /**
     * Vertical Offset From Crosshair To Target
     *
     * @return offset
     */
    public double getTY() {
        return LimelightHelpers.getTY(LimelightConstants.FRONT_LIMELIGHT);
    }
    
    /**
     * Gets the ID of the nearest AprilTag or 0 if not found
     *
     * @return ID
     */
    public int getID() {
        return (int) NetworkTableInstance.getDefault().getTable(LimelightConstants.FRONT_LIMELIGHT)
            .getEntry("tid").getInteger(0);
    }

    /**
     * Gets the botpose relative to the current alliance, or Blue
     * if no alliance is found
     *
     * @return botpose
     */
    public Pose2d getBotpose() {
        // var alliance = DriverStation.getAlliance();
        // if (alliance.isPresent() && alliance.get() == DriverStation.Alliance.Red) {
        //     return LimelightHelpers.getBotPose2d_wpiRed(LimelightConstants.FRONT_LIMELIGHT);
        // }
        return LimelightHelpers.getBotPose2d_wpiBlue(LimelightConstants.FRONT_LIMELIGHT);
    }

    /**
     * Gets the botpose in target space array of 6
     * 
     * @return botpose array
     */
    public double[] getBotPose_TargetSpace() {
        return LimelightHelpers.getBotPose_TargetSpace(LimelightConstants.FRONT_LIMELIGHT);
    }

    // /**
     // * Gets the angle the robot needs to have to face the current tag
     // * 
     // * @return Rotation2d for the angle
     // */
    // public Rotation2d getFacingAngle() {
        // double[] botpose_targetspace = getBotPoseTargetSpace();
        // return new Rotation2d(Math.atan2(botpose_targetspace[0], -botpose_targetspace[2]));
    // }
    
    @Override
    public void periodic() {
        limelight_field.setRobotPose(this.getBotpose());
        int id = this.getID();
        boolean see = id > 0;
        SB_D_TID.setInteger(id);
        SB_F_TID.setInteger(id);
        SB_D_TSEE.setBoolean(see);
        SB_F_TSEE.setBoolean(see);
    }

    // private class ExtraHelpers extends LimelightHelpers {
    //     public static Pose2d toPose2d(double[] inData) {
    //         // From LimelightHelpers 397-406
    //         if(inData.length < 6) {
    //             System.err.println("Bad LL 2D Pose Data!");
    //             return new Pose2d();
    //         }
    //         Translation2d tran2d = new Translation2d(inData[0], inData[1]);
    //         Rotation2d r2d = new Rotation2d(Units.degreesToRadians(inData[5]));
    //         return new Pose2d(tran2d, r2d);
    //     }
    // }
}
