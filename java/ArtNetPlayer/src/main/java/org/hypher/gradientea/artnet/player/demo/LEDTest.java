package org.hypher.gradientea.artnet.player.demo;

import com.pi4j.wiringpi.Spi;

/**
 * Attempts to control the LEDs by writing directly to SPI.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class LEDTest {
	public static void main(String[] args) throws Exception{
		int speed = args.length < 1 ? 1024000 : Integer.parseInt(args[0]);
		Spi.wiringPiSPISetup(0, speed);

		byte[] buffer;
		while (true) {
			buffer = buildBuffer(50);
			Spi.wiringPiSPIDataRW(0, buffer, buffer.length);
			System.out.println("Wrote " + buffer.length + " bytes at " + speed + "bps");
			Thread.sleep(1000);
		}
	}

	public static byte[] buildBuffer(int pixels) {
		byte b = (byte) ((Math.random() - 0.5) * 255);

		byte[] buffer = new byte[pixels * 3];

		for (int i=0; i<pixels; i++) {
			if (i%2 == 0) {
				buffer[i*3] = b;
				buffer[i*3+1] = (byte) (b*2);
				buffer[i*3+2] = (byte) (b/2);
			}
		}

		return buffer;
	}
}
