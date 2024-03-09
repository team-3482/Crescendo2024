// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.shooter;

import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.revrobotics.CANSparkFlex;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ShooterConstants;
import frc.robot.Constants.ShuffleboardTabConstants;
import frc.robot.Constants.SwerveModuleConstants;
import frc.robot.utilities.JSONManager;

public class ShooterSubsystem extends SubsystemBase {    
    // Singleton Design Pattern
    private static ShooterSubsystem instance;
    public static ShooterSubsystem getInstance() {
        if(instance == null) {
            instance = new ShooterSubsystem();
        }
        return instance;
    }

    /** This is used after a pivot command to be sure the robot can chain a shooting command */
    public boolean canShoot = false;

    private CANSparkFlex rightShooter = new CANSparkFlex(ShooterConstants.RIGHT_SHOOTER_MOTOR_ID, MotorType.kBrushless);
    private CANSparkFlex leftShooter = new CANSparkFlex(ShooterConstants.LEFT_SHOOTER_MOTOR_ID, MotorType.kBrushless);
    
    private MotionMagicVoltage motionMagicVoltage = new MotionMagicVoltage(0);
    private TalonFX rightPivotMotor = new TalonFX(ShooterConstants.LEFT_PIVOT_MOTOR_ID, SwerveModuleConstants.SWERVE_CAN_BUS);
    private TalonFX leftPivotMotor = new TalonFX(ShooterConstants.RIGHT_PIVOT_MOTOR_ID, SwerveModuleConstants.SWERVE_CAN_BUS);
    

    /** Creates a new ShooterSubsystem, sets pivot positions, and configures Motion Magic for the pivot */
    public ShooterSubsystem() {
        // leftShooter.setInverted(true);
        
        configureMotionMagic();
        // Reset position (ONLY DO THIS WITH THE PIVOT VERTICAL)
        // zeroPivotPosition(90);
        
        double[] positions = JSONManager.getInstance().getPivotPositions();
        leftPivotMotor.setPosition(Units.degreesToRotations(positions[0] * ShooterConstants.MOTOR_TO_PIVOT_RATIO));
        rightPivotMotor.setPosition(Units.degreesToRotations(positions[1] * ShooterConstants.MOTOR_TO_PIVOT_RATIO));

        // Reset the pivot's position
        Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
            .add("Reset Shooter 1",
                Commands.runOnce(() -> ShooterSubsystem.getInstance().zeroPivotPosition(ShooterConstants.PIVOT_ANGLE_LIMITS[0]))
                    .withName("Set Pivot " + ShooterConstants.PIVOT_ANGLE_LIMITS[0] + "d")
            )
            .withWidget(BuiltInWidgets.kCommand)
            // .withPosition(0, 0)
            .withSize(2, 1);
        Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
            .add("Reset Shooter 2",
                Commands.runOnce(() -> ShooterSubsystem.getInstance().zeroPivotPosition(90))
                    .withName("Set Pivot 90d")
            )
            .withWidget(BuiltInWidgets.kCommand)
            // .withPosition(0, 0)
            .withSize(2, 1);
        // Force save positions of the pivot
        Shuffleboard.getTab(ShuffleboardTabConstants.DEFAULT)
            .add("Force Save Pivot",
                Commands.runOnce(() -> {
                    double[] pos = ShooterSubsystem.getInstance().getPivotPositions();
                    JSONManager.getInstance().savePivotPositions(pos[0], pos[1]);
                }).withName("Save Pivot Position")
            )
            .withWidget(BuiltInWidgets.kCommand)
            // .withPosition(0, 0)
            .withSize(2, 1);
    }

    /**
     * Configures motion magic for the intake pivot talon
     */
    private void configureMotionMagic() {
        TalonFXConfiguration configuration = new TalonFXConfiguration();
        
        FeedbackConfigs feedbackConfigs = configuration.Feedback;
        feedbackConfigs.FeedbackSensorSource = FeedbackSensorSourceValue.RotorSensor;
        // Sets the gear ratio from the motor to the mechanism (pivot)
        feedbackConfigs.SensorToMechanismRatio = 0;
        
        MotorOutputConfigs motorOutputConfigs = configuration.MotorOutput;
        motorOutputConfigs.DutyCycleNeutralDeadband = 0.001;
        motorOutputConfigs.Inverted = InvertedValue.Clockwise_Positive; // Right motor not inverted
        motorOutputConfigs.NeutralMode = NeutralModeValue.Brake;
        
        // Set Motion Magic gains in slot0
        Slot0Configs slot0Configs = configuration.Slot0;
        slot0Configs.kS = ShooterConstants.SLOT_0_CONFIGS.kS;
        slot0Configs.kV = ShooterConstants.SLOT_0_CONFIGS.kV;
        slot0Configs.kP = ShooterConstants.SLOT_0_CONFIGS.kP;
        slot0Configs.kI = ShooterConstants.SLOT_0_CONFIGS.kI;
        slot0Configs.kD = ShooterConstants.SLOT_0_CONFIGS.kD;
        
        // Set acceleration and vcruise velocity
        MotionMagicConfigs motionMagicConfigs = configuration.MotionMagic;
        motionMagicConfigs.MotionMagicCruiseVelocity = ShooterConstants.CRUISE_SPEED;
        motionMagicConfigs.MotionMagicAcceleration = ShooterConstants.CRUISE_ACCELERATION;
        motionMagicConfigs.MotionMagicJerk = ShooterConstants.MOTION_MAGIC_JERK;
        
        this.rightPivotMotor.getConfigurator().apply(configuration);
        
        motorOutputConfigs.Inverted = InvertedValue.CounterClockwise_Positive; // Left motor inverted
        this.leftPivotMotor.getConfigurator().apply(configuration);
    }

    /**
     * Set the motor encoder's position to the pivot angle
     * 
     * @param angle in degrees
     */
    public void zeroPivotPosition(double angle) {
        double position = Units.degreesToRotations(angle) * ShooterConstants.MOTOR_TO_PIVOT_RATIO;
        leftPivotMotor.setPosition(position);
        rightPivotMotor.setPosition(position);
        double[] pos = getPivotPositions();
        JSONManager.getInstance().savePivotPositions(pos[0]);
    }
    
    /**
     * Goes to the position of the pivot using Motion Magic slot 0
     * 
     * @param position in degrees
     */
    public void pivotGoToPosition(double position) {
        position = MathUtil.clamp(position, ShooterConstants.PIVOT_ANGLE_LIMITS[0], ShooterConstants.PIVOT_ANGLE_LIMITS[1]);
        MotionMagicVoltage control = motionMagicVoltage
        // Select Slot 0 for Motion Magic (should be done by default)
        .withSlot(0)
        .withPosition(Units.degreesToRotations(position * ShooterConstants.MOTOR_TO_PIVOT_RATIO));
        rightPivotMotor.setControl(control);
        leftPivotMotor.setControl(control);
    }

    /**
     * Set the pivot speeds (last resort) between -1.0 and 1.0.
     * Will set the speed to 0 for each motor individually per {@link ShooterConstants} {@code PIVOT_ANGLE_LIMITS}
     */
    public void setPivotSpeed(double speed) {
        double leftSpeed = speed, rightSpeed = speed;
        leftSpeed = (speed < 0 && getPivotPositions()[0] <= ShooterConstants.PIVOT_ANGLE_LIMITS[0]) ||
            (speed > 0 && getPivotPositions()[0] >= ShooterConstants.PIVOT_ANGLE_LIMITS[1]) ? 0 : leftSpeed;
        rightSpeed = (speed < 0 && getPivotPositions()[1] <= ShooterConstants.PIVOT_ANGLE_LIMITS[0]) ||
            (speed > 0 && getPivotPositions()[1] >= ShooterConstants.PIVOT_ANGLE_LIMITS[1]) ? 0 : rightSpeed;

        rightPivotMotor.set(rightSpeed);
        leftPivotMotor.set(leftSpeed);

        if ((int) (speed * 100) == 0) {
            double[] positions = ShooterSubsystem.getInstance().getPivotPositions();
            JSONManager.getInstance().savePivotPositions(positions[0], positions[1]);
        }
    }

    /**
     * Gets the positions of the pivots (after gear ratio) using the motor's rotor.
     * <p> Left [0] and right [1] </p>
     * <p> Note : it is recommended to follow only the right [1] motor for pivot positioning </p>
     * 
     * @return positions in degrees
     */
    public double[] getPivotPositions() {
        return new double[]{
            Units.rotationsToDegrees(leftPivotMotor.getPosition().getValueAsDouble() / ShooterConstants.MOTOR_TO_PIVOT_RATIO),
            Units.rotationsToDegrees(rightPivotMotor.getPosition().getValueAsDouble() / ShooterConstants.MOTOR_TO_PIVOT_RATIO)
        };
    }

    /**
     * Gets the velocities of the shooter motors. Left is [0] and right is [1]
     * 
     * @return velocities in rad/s
     */
    public double[] getShootingVelocities() {
        return new double[]{
            Units.rotationsPerMinuteToRadiansPerSecond(leftShooter.getEncoder().getVelocity()),
            Units.rotationsPerMinuteToRadiansPerSecond(rightShooter.getEncoder().getVelocity())
        };
    }

    /**
     * Sets the velocities of the shooter motors. Left is [0] and right is [1]
     * 
     * @param velocities between -1.0 and 1.0
     */
    public void setShootingVelocities(double[] velocities) {
        leftShooter.set(velocities[0]);
        rightShooter.set(velocities[1]);
    }

    /**
     * Stops the shooting motors (overloaded)
     */
    public void setShootingVelocities() {
        setShootingVelocities(new double[2]);
    }

    @Override
    public void periodic() {
        // System.out.println(getPivotPositions()[0] + " " + getPivotPositions()[1]);
    }
}