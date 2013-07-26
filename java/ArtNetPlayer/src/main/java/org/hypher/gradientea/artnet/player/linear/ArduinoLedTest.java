package org.hypher.gradientea.artnet.player.linear;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Uninterruptibles;
import gnu.io.NRSerialPort;
import org.hypher.gradientea.animation.shared.color.HsbColor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hypher.gradientea.geometry.shared.math.DomeMath.TWO_PI;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ArduinoLedTest {
	private static final int ARRAY_WIDTH = 16;
	private static final int ARRAY_HEIGHT = 16;

	private static final int LED_COUNT = ARRAY_WIDTH * ARRAY_HEIGHT;
	private static final int BUFFER_SIZE = 63;
	public static String[] DESIRED_SERIAL_PORT_NAMES = new String[]{
		"usb"
	};

	public static void main(String[] args) {
		Optional<String> portName = selectSerialPort();
		if (portName.isPresent()) {
			System.out.println("Using Serial Port: " + portName.get());

			testWithRXTX(portName.get());
		} else {
			System.err.println("No Available Serial Ports");
		}
	}

	private static void testWithRXTX(String portName) {
		final NRSerialPort port = new NRSerialPort(portName, 115200);
		port.connect();

		final OutputStream outputStream = port.getOutputStream();
		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, BUFFER_SIZE);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(port.getInputStream()));

		try {
			byte data[] = new byte[LED_COUNT*3];

			boolean firstIteration = true;

			int frameCountSinceLastFpsOutput = 0;
			long lastFpsTime = System.currentTimeMillis();

			while (true) {
				// Wait for the Arduino to let us know it's ready for more data before sending data. This helps ensure
				// that we're only sending data when it can be received.
				if (! firstIteration) {
					while (port.getInputStream().available() < 1) {
						Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MICROSECONDS);
					}
					reader.readLine();
				}
				else {
					firstIteration = false;
				}

				// Build the frame
				renderFrameInto(data);
				swapScanlines(data);

				// Send out the data in chunks equal to BUFFER_SIZE. If data is sent too quickly to the Arduino, it will
				// crash, presumably due to a buffer overflow in the Serial handling code.
				for (int i=0; i<data.length; i+=BUFFER_SIZE) {
					outputStream.write(data, i, Math.min(BUFFER_SIZE, data.length-i));
					outputStream.flush();
				}

				if (frameCountSinceLastFpsOutput++ == 100) {
					long now = System.currentTimeMillis();
					double framesPerSecond = (frameCountSinceLastFpsOutput / ((now-lastFpsTime)/1000d));
					System.out.println("FPS: " + NumberFormat.getNumberInstance().format(framesPerSecond));

					frameCountSinceLastFpsOutput = 0;
					lastFpsTime = now;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Closing...");
			port.disconnect();
			System.out.println("Done.");
		}
	}

	private static void swapScanlines(byte[] rgbData) {
		byte tempR, tempG, tempB;
		for (int y=0, scanStart=0; y<ARRAY_HEIGHT; y++, scanStart+=ARRAY_WIDTH) {
			if (y%2==1) {
				// Swap this scanline
				for (int x=0; x<ARRAY_WIDTH/2; x++) {
					tempR = rgbData[(scanStart+x)*3];
					tempG = rgbData[(scanStart+x)*3+1];
					tempB = rgbData[(scanStart+x)*3+2];

					rgbData[(scanStart+x)*3] = rgbData[(scanStart+ARRAY_WIDTH-(x+1))*3];
					rgbData[(scanStart+x)*3+1] = rgbData[(scanStart+ARRAY_WIDTH-(x+1))*3+1];
					rgbData[(scanStart+x)*3+2] = rgbData[(scanStart+ARRAY_WIDTH-(x+1))*3+2];

					rgbData[(scanStart+ARRAY_WIDTH-(x+1))*3] = tempR;
					rgbData[(scanStart+ARRAY_WIDTH-(x+1))*3+1] = tempG;
					rgbData[(scanStart+ARRAY_WIDTH-(x+1))*3+2] = tempB;
				}
			}
		}
	}

	private static void renderFrameInto(final byte[] rgbData) {
		float timeFraction0 = (System.currentTimeMillis()%2000)/2000f;
		float timeFraction1 = (System.currentTimeMillis()%3000)/3000f;
		float timeFraction2 = (System.currentTimeMillis()%7000)/7000f;
		float timeFraction3 = (System.currentTimeMillis()%10000)/10000f;

		for (int y=0; y<ARRAY_HEIGHT; y++) {
			double yFraction = ((double) y/ARRAY_HEIGHT + Math.sin(timeFraction1*TWO_PI)) % 1.0;

			for (int x=0; x<ARRAY_WIDTH; x++) {
				double xFraction = ((double) x/ARRAY_WIDTH + Math.cos(timeFraction2*TWO_PI)) % 1.0;

				int[] colorRgb = new HsbColor(
					(Math.abs(Math.sin(yFraction*Math.PI)*Math.cos(xFraction*Math.PI)*Math.sin(timeFraction1*TWO_PI))+timeFraction2)%1d,
					1.0,
					.10
				).asRgb();

				rgbData[(y*ARRAY_WIDTH+x)*3+0] = (byte) colorRgb[0];
				rgbData[(y*ARRAY_WIDTH+x)*3+1] = (byte) colorRgb[1];
				rgbData[(y*ARRAY_WIDTH+x)*3+2] = (byte) colorRgb[2];
			}
		}
	}

	private static Optional<String> selectSerialPort() {
		final Set<String> availableSerialPorts = NRSerialPort.getAvailableSerialPorts();

		for (String name : DESIRED_SERIAL_PORT_NAMES) {
			for (String portId : availableSerialPorts) {
				if (portId.toLowerCase().contains(name)) {
					return Optional.of(portId);
				}
			}
		}

		if (availableSerialPorts.isEmpty()) {
			return Optional.absent();
		}

		return Optional.of(availableSerialPorts.iterator().next());
	}
}


