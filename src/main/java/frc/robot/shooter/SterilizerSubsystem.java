// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.shooter;

import java.util.Optional;
import java.util.OptionalInt;

import com.revrobotics.CANSparkFlex;

import au.grapplerobotics.LaserCan;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkLowLevel.PeriodicFrame;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.constants.PhysicalConstants.SterilizerConstants;
import frc.robot.constants.PrimeNumbers;
import frc.robot.lights.LEDSubsystem;
import frc.robot.lights.LEDSubsystem.LightState;

/**
 * A subsytem that moves the sterilizer and queries laser data.
 */
public class SterilizerSubsystem extends SubsystemBase {
    // Thread-safe singleton design pattern.
    private static volatile SterilizerSubsystem instance;
    private static Object mutex = new Object();
    public static SterilizerSubsystem getInstance() {
        SterilizerSubsystem result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new SterilizerSubsystem();
            }
        }
        return instance;
    }

    private CANSparkFlex feederMotor = new CANSparkFlex(SterilizerConstants.NEO_MOTOR_ID, MotorType.kBrushless);
    private LaserCan backLaser = new LaserCan(SterilizerConstants.BACK_LASER_ID);
    private LaserCan frontLaser = new LaserCan(SterilizerConstants.FRONT_LASER_ID);

    /**
     * Creates a new SterilizerSubsystem.
     * @apiNote LaserCAN should be configured in the GrappleHook app.
     */
    public SterilizerSubsystem() {
        super("SterilizerSubsystem");

        setStatusFrames();
    }

    @Override
    public void periodic() {
        LEDSubsystem.getInstance().setLightState(
            (hasNote() ? LightState.HOLDING_NOTE : LightState.OFF), false);
    }
    
    /**
     * Gets the distances measured by each laser.
     * @return measurements, back laser [0] and front laser [1].
     * @apiNote empty optional when the measurement is invalid.
     */
    public OptionalInt[] getLaserMeasurements() {
        LaserCan.Measurement backMm = backLaser.getMeasurement();
        LaserCan.Measurement frontMm = frontLaser.getMeasurement();
        
        OptionalInt[] measurements = new OptionalInt[]{
            backMm != null && backMm.status == LaserCan.LASERCAN_STATUS_VALID_MEASUREMENT ?
                OptionalInt.of(backMm.distance_mm) : OptionalInt.empty(),
            frontMm != null && frontMm.status == LaserCan.LASERCAN_STATUS_VALID_MEASUREMENT ?
                OptionalInt.of(frontMm.distance_mm) : OptionalInt.empty()
        };

        return measurements;
    }

    /**
     * Checks the distances of {@link SterilizerSubsystem#getLaserMeasurements()} against the measurements for a note.
     * @return has notes, back laser [0] and front laser [1].
     * @apiNote empty optional when the measurement is invalid.
     */
    public Optional<Boolean>[] getHasNotes() {
        OptionalInt[] measurements = getLaserMeasurements();

        @SuppressWarnings("unchecked")
        Optional<Boolean>[] sketchy = (Optional<Boolean>[]) new Optional<?>[2];
        sketchy[0] = measurements[0].isPresent()
            ? Optional.ofNullable(measurements[0].getAsInt() <= SterilizerConstants.NOTE_DISTANCE_LASER) : Optional.empty();
        sketchy[1] = measurements[1].isPresent()
            ? Optional.ofNullable(measurements[1].getAsInt() <= SterilizerConstants.NOTE_DISTANCE_LASER) : Optional.empty();
        return sketchy;
    }

    /**
     * Checks if either of {@link SterilizerSubsystem#getHasNotes()} is true.
     * @return if either laser sees a note.
     * @apiNote Treats invalid measurements as {@code false}.
     */
    public boolean hasNote() {
        Optional<Boolean>[] notes = getHasNotes();
        return (notes[0].isPresent() && notes[0].get()) || (notes[1].isPresent() && notes[1].get());
    }
    
    /**
     * Spins the sterilizer at the given speed.
     * @param speed from -1.0 to 1.0. Positive speeds are towards the shooting wheels.
     */
    public void setSpeed(double speed) {
        feederMotor.set(speed);
    }

    /**
     * Stops the sterilizer.
     */
    public void setSpeed() {
        setSpeed(0);
    }

    /**
     * Limits the publishing of CAN messages to the bus.
     */
    private void setStatusFrames() {
        // feederMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus0, 20);
        feederMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus1, PrimeNumbers.getNextPrimeNumber());
        feederMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus2, PrimeNumbers.getNextPrimeNumber());
        feederMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus3, PrimeNumbers.getNextPrimeNumber());
        feederMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus4, PrimeNumbers.getNextPrimeNumber());
        feederMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus5, PrimeNumbers.getNextPrimeNumber());
        feederMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus6, PrimeNumbers.getNextPrimeNumber());
        // feederMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus7, 250);
    }
}
