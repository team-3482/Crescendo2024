// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LEDConstants;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;

public class LEDSubsystem extends SubsystemBase {
  public static AddressableLED led = new AddressableLED(LEDConstants.ledPort);
  public static AddressableLEDBuffer ledBuffer = new AddressableLEDBuffer(LEDConstants.ledCount);

  public enum LEDState {
    COLOR,
    RAINBOW,
    GRADIENT
  };

  private int rainbowFirstPixelHue = 0;
  private int rgb[] = { 0, 0, 0 };
  private LEDState state = LEDState.RAINBOW;

  /** Creates a new ExampleSubsystem. */
  public LEDSubsystem() {
    led.setLength(ledBuffer.getLength());
    led.start();
  }

  public void SetColor(int[] rgb, LEDState state) {
    this.rgb[0] = rgb[0];
    this.rgb[1] = rgb[1];
    this.rgb[2] = rgb[2];
    this.state = state;
  }

  private void Color() {
    for (var i = 0; i < ledBuffer.getLength(); i++) {
      ledBuffer.setRGB(i, rgb[0], rgb[1], rgb[2]);
    }
    ;
  }

  private void Rainbow() { // https://docs.wpilib.org/en/stable/docs/software/hardware-apis/misc/addressable-leds.html
    for (var i = 0; i < ledBuffer.getLength(); i++) {
      // Calculate the hue - hue is easier for rainbows because the color
      // shape is a circle so only one value needs to precess
      final var hue = (rainbowFirstPixelHue + (i * 360 / ledBuffer.getLength())) % 360;
      ledBuffer.setHSV(i, hue, 255, 128);
    }

    // Increase by to make the rainbow "move"
    rainbowFirstPixelHue += 3;
    // Check bounds
    rainbowFirstPixelHue %= 180;
  }

  private void Gradient(int color1[], int color2[]) {
    // TODO
  }

  @Override
  public void periodic() {
    switch (state) {
      case RAINBOW:
        Rainbow();
      case COLOR:
        Color();
      case GRADIENT:
        Gradient();
    }

    led.setData(ledBuffer);
  }
}
