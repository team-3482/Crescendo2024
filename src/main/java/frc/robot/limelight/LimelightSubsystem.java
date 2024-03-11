// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.limelight;

import java.util.Map;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.HttpCamera;
import edu.wpi.first.cscore.HttpCamera.HttpCameraKind;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LimelightConstants;
import frc.robot.Constants.ShuffleboardTabConstants;

public class LimelightSubsystem extends SubsystemBase {

    // Singleton Design Pattern
    private static LimelightSubsystem instance;

    public static LimelightSubsystem getInstance() {
        if (instance == null) {
            instance = new LimelightSubsystem();
        }
        return instance;
    }

    Field2d limelight_field = new Field2d();

    // Shuffleboard
    private GenericEntry SB_D_TID = Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
        .add("T ID", 0)
        .withWidget(BuiltInWidgets.kTextView)
        .withPosition(3, 0)
        .withSize(1, 1)
        .getEntry();
    // private GenericEntry SB_D_NSEE = Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
    //     .add("NOTE-SEE", false)
    //     .withWidget(BuiltInWidgets.kBooleanBox)
    //     .withPosition(3, 0)
    //     .withSize(3, 3)
    //     .getEntry();
    // private GenericEntry SB_D_TSEE = Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
    //     .add("TAG-SEE", false)
    //     .withWidget(BuiltInWidgets.kBooleanBox)
    //     .withPosition(6, 0)
    //     .withSize(3, 3)
    //     .getEntry();
    private GenericEntry SB_D_TUPDATE = Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
        .add("Vision Odometry", false)
        .withWidget(BuiltInWidgets.kBooleanBox)
        .withPosition(4, 0)
        .withSize(2, 1)
        .getEntry();

    // private GenericEntry SB_F_TID = Shuffleboard.getTab(ShuffleboardTabConstants.FIELDS)
    //     .add("T-ID", 0)
    //     .withWidget(BuiltInWidgets.kTextView)
    //     .withPosition(14, 4)
    //     .withSize(2, 1)
    //     .getEntry();
    // private GenericEntry SB_F_TSEE = Shuffleboard.getTab(ShuffleboardTabConstants.FIELDS)
    //     .add("TAG-SEE", false)
    //     .withWidget(BuiltInWidgets.kBooleanBox)
    //     .withPosition(14, 0)
    //     .withSize(2, 2)
    //     .getEntry();
    // private GenericEntry SB_F_TUPDATE = Shuffleboard.getTab(ShuffleboardTabConstants.FIELDS)
    //     .add("ADD-VISION", false)
    //     .withWidget(BuiltInWidgets.kBooleanBox)
    //     .withPosition(14, 2)
    //     .withSize(2, 2)
    //     .getEntry();

    /** Creates a new LimelightSubsystem. */
    public LimelightSubsystem() {
        super("LimelightSubsystem");
        HttpCamera limelightBackFeed = new HttpCamera(
            LimelightConstants.INTAKE_LLIGHT,
            "http://" + LimelightConstants.INTAKE_LLIGHT + ".local:5800/stream.mjpg",
            HttpCameraKind.kMJPGStreamer);
        CameraServer.addCamera(limelightBackFeed);
        Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
            .add("Back Limelight", limelightBackFeed)
            .withWidget(BuiltInWidgets.kCameraStream)
            .withPosition(9, 1)
            .withSize(6, 3)
            .withProperties(Map.of("Show Crosshair", false, "Show Controls", false));
        
        HttpCamera limelightFrontFeed = new HttpCamera(
            LimelightConstants.SHOOTER_LLIGHT,
            "http://" + LimelightConstants.SHOOTER_LLIGHT + ".local:5800/stream.mjpg",
            HttpCameraKind.kMJPGStreamer);
        CameraServer.addCamera(limelightBackFeed);
        Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
            .add("Front Limelight", limelightFrontFeed)
            .withWidget(BuiltInWidgets.kCameraStream)
            .withPosition(3, 1)
            .withSize(6, 3)
            .withProperties(Map.of("Show Crosshair", false, "Show Controls", false));

        // Shuffleboard.getTab(ShuffleboardTabConstants.FIELDS)
        //     .add("Field (limelight)", limelight_field)
        //     .withWidget(BuiltInWidgets.kField)
        //     .withPosition(7, 0)
        //     .withSize(7, 4);
    }

    /**
     * Horizontal Offset From Crosshair To Target
     *
     * @return offset
     */
    public double getHorizontalOffset(String limelight) {
        return LimelightHelpers.getTX(limelight);
    }

    /**
     * Vertical Offset From Crosshair To Target
     *
     * @return offset
     */
    public double getVerticalOffset(String limelight) {
        return LimelightHelpers.getTY(limelight);
    }

    /**
     * Target Area (0% of image to 100% of image)
     * (uses LimelightConstants.BACK_LIMELIGHT)
     *
     * @return area
     */
    public double getTargetArea() {
        return LimelightHelpers.getTA(LimelightConstants.INTAKE_LLIGHT);
    }

    /**
     * Wheter the Limelight has a target
     *
     * @return offset
     */
    public boolean hasTarget(String limelight) {
        return LimelightHelpers.getTV(limelight);
    }

    /**
     * Gets the ID of the nearest AprilTag or 0 if not found
     * (using LimelightConstants.FRONT_LIMELIGHT)
     *
     * @return ID
     */
    public int getTargetID() {
        return (int) NetworkTableInstance.getDefault().getTable(LimelightConstants.SHOOTER_LLIGHT)
            .getEntry("tid").getInteger(0);
    }

    /**
     * Gets the botpose relative to the current blue alliance
     * (using LimelightConstants.FRONT_LIMELIGHT)
     *
     * @return botpose
     */
    public Pose2d getBotpose() {
        return LimelightHelpers.getBotPose2d_wpiBlue(LimelightConstants.SHOOTER_LLIGHT);
    }

    /**
     * Gets the latency of the limelight to be used for odometry
     * 
     * @return latency in seconds
     */
    public double getLatency(String limelight) {
        return LimelightHelpers.getLatency_Pipeline(limelight) / 1000
                - LimelightHelpers.getLatency_Capture(limelight) / 1000;
    }

    // /**
    // * Gets the position of an object being targeted
    // * (uses LimelightConstants.BACK_LIMELIGHT)
    // *
    // * @return the translation from the bot
    // */
    // public Optional<Translation2d>

    /**
     * Update the ADD-VISION Shuffleboard entries
     * 
     * @param bool whether or not it is updating odometry using vision
     */
    public void updateAddVisionEntry(boolean bool) {
        SB_D_TUPDATE.setBoolean(bool);
        // SB_F_TUPDATE.setBoolean(bool);
    }

    @Override
    public void periodic() {
        limelight_field.setRobotPose(this.getBotpose());
        int tid = this.getTargetID();
        // boolean tSee = this.hasTarget(LimelightConstants.SHOOTER_LLIGHT);
        // boolean nSee = this.hasTarget(LimelightConstants.INTAKE_LLIGHT);

        // Default Shuffleboard
        SB_D_TID.setInteger(tid);
        // SB_D_TSEE.setBoolean(tSee);
        // SB_D_NSEE.setBoolean(nSee);

        // Field Shuffleboard (disabled for competition)
        // SB_F_TID.setInteger(tid);
        // SB_F_TSEE.setBoolean(tSee);
    }
}
