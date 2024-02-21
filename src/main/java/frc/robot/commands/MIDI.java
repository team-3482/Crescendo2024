// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
 
package frc.robot.commands;

import frc.robot.subsystems.MIDISubsystem;
import edu.wpi.first.wpilibj2.command.Command;

/** An example command that uses an example subsystem. */
public class MIDI extends Command {
  @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
  private final MIDISubsystem m_subsystem;

  private String MIDIFolder = "../../../../../../midi/";
  private String MIDIFile;

  public MIDI(MIDISubsystem subsystem, String MIDIFile) {
    m_subsystem = subsystem;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(subsystem);

    MIDIFile = this.MIDIFile;
  }

  @Override
  public void execute() {
    m_subsystem.PlayMIDI(MIDIFolder, MIDIFile);
  }
}
