package org.hypher.gradientea.artnet.player.io;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Uninterruptibles;
import gnu.io.NRSerialPort;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.TWO_PI;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.exponentialScale;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ArduinoLedPanelOutput {
	private static final int ARRAY_WIDTH = 16;
	private static final int ARRAY_HEIGHT = 16;

	private static final int LED_COUNT = ARRAY_WIDTH * ARRAY_HEIGHT;
	private static final int BUFFER_SIZE = 63;
	public static String[] DESIRED_SERIAL_PORT_NAMES = new String[]{
		"usb"
	};

	private String devicePath;
	private NRSerialPort serialPort;
	private OutputStream outputStream;
	private InputStream inputStream;
	private BufferedReader inputReader;
	private byte[] imageBuffer = new byte[LED_COUNT*3];
	private BufferedImage bufferedImage;

	private byte[] scanLineBuffer = new byte[ARRAY_WIDTH*3];

	{
		DataBufferByte dataBuffer = new DataBufferByte(imageBuffer, ARRAY_WIDTH*ARRAY_HEIGHT*3);

		WritableRaster raster = Raster.createInterleavedRaster(
			dataBuffer,
			ARRAY_WIDTH,
			ARRAY_HEIGHT,
			ARRAY_WIDTH * 3,
			3,
			new int[]{0, 1, 2},
			null
		);

		ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
		bufferedImage = new BufferedImage(colorModel, raster, false, null);
	}


	public static void main(String[] args) {
		ArduinoLedPanelOutput output = getInstance().get();
		Graphics2D g = (Graphics2D) output.bufferedImage.getGraphics();

		g.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON
		);

		while (true) {
			float timeFraction0 = (System.currentTimeMillis()%2000)/2000f;
			float timeFraction1 = (System.currentTimeMillis()%20000)/20000f;
			double angle = timeFraction0*TWO_PI;

			g.setColor(Color.getHSBColor(1-timeFraction1, 1.0f, .01f));
			g.fillRect(0, 0, ARRAY_WIDTH, ARRAY_HEIGHT);

			g.setColor(Color.getHSBColor(timeFraction0, 1.0f, .1f));
			g.drawLine(
				(int)((.5 + .5* cos(angle)) * ARRAY_WIDTH),
				(int)((.5 + .5* sin(angle)) * ARRAY_HEIGHT),
				(int)((.5 - .5* cos(angle)) * ARRAY_WIDTH),
				(int)((.5 - .5* sin(angle)) * ARRAY_HEIGHT)
			);

			g.drawOval(
				0,
				0,
				(int)Math.abs(cos(angle) * ARRAY_WIDTH),
				(int)Math.abs(sin(angle) * ARRAY_HEIGHT)
			);

			g.drawLine(
				0,
				(int)(timeFraction0*ARRAY_HEIGHT),
				ARRAY_WIDTH,
				(int)(timeFraction0*ARRAY_HEIGHT)
			);

			output.writeImageBuffer();
			Uninterruptibles.sleepUninterruptibly(30, TimeUnit.MILLISECONDS);

		}
	}

	public static Optional<ArduinoLedPanelOutput> getInstance() {
		Optional<String> portId = selectSerialPort();
		if (portId.isPresent()) {
			return Optional.of(new ArduinoLedPanelOutput(portId.get()));
		} else {
			return Optional.absent();
		}
	}

	public ArduinoLedPanelOutput(final String devicePath) {
		this.devicePath = devicePath;
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

	private void connect() {
		if (serialPort == null) {
			serialPort = new NRSerialPort(devicePath, 115200);
			serialPort.connect();

			outputStream = serialPort.getOutputStream();
			inputStream = serialPort.getInputStream();
			inputReader = new BufferedReader(new InputStreamReader(inputStream));

			// Send all zeros to get everything sync'd up correctly
			write(new byte[LED_COUNT * 3]);

			// And wait for "READY"
			waitForReady();
		}
	}

	private void waitForReady() {
		try {
			long start = System.currentTimeMillis();

			while (inputStream.available() < 1 && (System.currentTimeMillis()-start) < 20) {
				Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
			}

			if (inputStream.available() > 0) {
				inputReader.readLine();
			} else {
				System.err.println("Didn't receive READY from Arduino");
			}
		} catch (IOException e) {
			fail(e);
		}
	}

	public void writeImage(Image image) {
		final Graphics2D g = (Graphics2D) bufferedImage.getGraphics();

		g.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON
		);
		g.setRenderingHint(
			RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BILINEAR
		);

		g.setColor(Color.black);
		g.fillRect(0, 0, ARRAY_WIDTH, ARRAY_HEIGHT);
		g.drawImage(
			image, 0, 0, ARRAY_WIDTH, ARRAY_HEIGHT, null
		);
		writeImageBuffer();
	}

	private void writeImageBuffer() {
		write(imageBuffer);
	}

	private void write(final byte[] data) {
		try {
			if (! isConnected()) {
				connect();
			} else {
				waitForReady();
			}

			if (ARRAY_WIDTH*3 < BUFFER_SIZE) {
				for (int y=0; y<ARRAY_HEIGHT; y++) {
					System.arraycopy(data, y*ARRAY_WIDTH*3, scanLineBuffer, 0, ARRAY_WIDTH*3);
					if (y%2==1) {
						// Swap this scanline
						for (int x=0; x<ARRAY_WIDTH/2; x++) {
							byte tempR = scanLineBuffer[x*3];
							byte tempG = scanLineBuffer[x*3+1];
							byte tempB = scanLineBuffer[x*3+2];

							scanLineBuffer[x*3+0] = scanLineBuffer[(ARRAY_WIDTH-(x+1))*3];
							scanLineBuffer[x*3+1] = scanLineBuffer[(ARRAY_WIDTH-(x+1))*3+1];
							scanLineBuffer[x*3+2] = scanLineBuffer[(ARRAY_WIDTH-(x+1))*3+2];

							scanLineBuffer[(ARRAY_WIDTH-(x+1))*3+0] = tempR;
							scanLineBuffer[(ARRAY_WIDTH-(x+1))*3+1] = tempG;
							scanLineBuffer[(ARRAY_WIDTH-(x+1))*3+2] = tempB;
						}
					}

					for (int i=0; i<scanLineBuffer.length; i++) {
						scanLineBuffer[i] = (byte) (exponentialScale(scanLineBuffer[i]&0xFF, 255d));
					}

					outputStream.write(scanLineBuffer);
					outputStream.flush();
				}
			} else {
				throw new IllegalStateException("Array width * 3 must be less than buffer size");
			}

		} catch (IOException e) {
			fail(e);
		}
	}

	private void fail(final IOException e) {
		System.err.println("Failed to write to " + devicePath + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
		disconnect();
	}

	private boolean isConnected() {
		return serialPort != null;
	}

	private void disconnect() {
		if (serialPort != null) {
			serialPort.disconnect();
			serialPort = null;
			outputStream = null;
			inputReader = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
