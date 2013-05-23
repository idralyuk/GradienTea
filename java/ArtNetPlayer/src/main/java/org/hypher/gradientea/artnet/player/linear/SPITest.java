package org.hypher.gradientea.artnet.player.linear;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.wiringpi.Spi;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class SPITest {
	public final static byte[] TEST_DATA = new byte[] {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		-127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127, -127,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		-86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86
	};

	public final static int[] SPEEDS = new int[] {
		4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000, 2048000, 4096000,
	};

	public static void main(String[] args) throws Exception{
		GpioController gpio = GpioFactory.getInstance();

		byte[] buffer = new byte[TEST_DATA.length];
		while (true) {
			for (int speed : SPEEDS) {
				Spi.wiringPiSPISetup(0, speed);

				System.arraycopy(TEST_DATA, 0, buffer, 0, TEST_DATA.length);

				Spi.wiringPiSPIDataRW(0, buffer, buffer.length);

				int errors = 0;
				for (int i=0; i<buffer.length; i++) {
					if (buffer[i] != TEST_DATA[i]) errors ++;
				}

				System.out.println("Wrote/Read " + buffer.length + " at " + speed + "bps with " + errors + " (" + Math.round(((double)errors/buffer.length)*100) + "%) errors");
				Thread.sleep(1000);
			}

			System.out.println();
		}
	}
}
