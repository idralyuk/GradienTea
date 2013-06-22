package org.hypher.gradientea.artnet.player.e131;

import org.hypher.gradientea.animation.shared.color.HsbColor;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;

/**
 * Sends a simple color pattern to the specified host and universe using the E131 protocol.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class E131Tester {
	public static void main(String[] args) throws SocketException, UnknownHostException {
		if (args.length < 2) {
			System.out.println("Usage: " + E131Tester.class.getSimpleName() + " <universe> <host> [<port>]");
			System.exit(-1);
		}

		int universe = Integer.parseInt(args[0]);
		String host = args[1];
		int port = args.length > 2 ? Integer.parseInt(args[2]) : E131Socket.E131_PORT;

		byte[] dmxData = new byte[512];

		E131Socket socket = new E131Socket();
		socket.connect(host, port);

		long start = System.currentTimeMillis();
		while (true) {
			double shift = ((System.currentTimeMillis() - start) % 2000) / 2000;

			for (int i=0; i<170; i++) {
				int[] rgb = new HsbColor((double)i/170 + shift, 1.0, 1.0).asRgb();
				dmxData[i*3 + 0] = (byte) rgb[0];
				dmxData[i*3 + 1] = (byte) rgb[1];
				dmxData[i*3 + 2] = (byte) rgb[2];
			}

			socket.sendDmx(universe, dmxData);

			sleepUninterruptibly(1000 / 33, TimeUnit.MILLISECONDS);
		}
	}
}
