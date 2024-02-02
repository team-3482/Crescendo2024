// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.SwerveModuleConstants;

public class MIDISubsystem extends SubsystemBase {
  private CANSparkMax[] motors = {
      new CANSparkMax(SwerveModuleConstants.One.TURN, MotorType.kBrushless),
      new CANSparkMax(SwerveModuleConstants.Two.TURN, MotorType.kBrushless),
      new CANSparkMax(SwerveModuleConstants.Three.TURN, MotorType.kBrushless),
      new CANSparkMax(SwerveModuleConstants.Four.TURN, MotorType.kBrushless) };

  public MIDISubsystem() {
  }

  public void PlayMIDI(String MIDIFolder, String MIDIFile) {
    for (int i = 0; i < motors.length; i++) {
      System.out.println("this does not work... yet");
    }
  }
}
