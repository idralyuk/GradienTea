package org.hypher.gradientea.artnet.player;

import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.Uninterruptibles;
import org.hypher.gradientea.artnet.player.e131.E131Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ControlBoxTester {
	public static int[] boardStartAddresses = new int[] {
		1, 28, 55, 82, 109, 136, 163
	};

	public static int[] boardChannelCount = new int[] {
		27, 27, 27, 27, 27, 27, 12 /*75*/
	};

	public static void main(String[] args) throws IOException {
		int universe = 1;
		String e131Host = "127.0.0.1";
		int e131Port = E131Socket.E131_PORT;

		if (args.length > 0) {
			universe = Integer.parseInt(args[0]);
		}

		if (args.length > 1) {
			e131Host = args[1];
		}

		if (args.length > 2) {
			e131Port = Integer.parseInt(args[2]);
		}

		System.out.println("Connecting to e131 host at " + e131Host + ":" + e131Port + ", universe " + universe);

		E131Socket socket = new E131Socket();
		socket.connect(e131Host, e131Port);

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String line = reader.readLine();

			boolean reverse = false;
			if (line.endsWith("r")) {
				line = line.substring(0, line.length()-1);
				reverse = true;
			}

			Integer boardNumber = Ints.tryParse(line);
			if (boardNumber == null || boardNumber < 0 || boardNumber >= boardStartAddresses.length) {
				System.out.println("Invalid board number. Enter an integer 0-" + (boardStartAddresses.length-1));
			} else {
				System.out.println("Running test on board " + boardNumber);

				int dmxStart = boardStartAddresses[boardNumber];
				int channelCount = boardChannelCount[boardNumber];
				byte[] data = new byte[512];

				for (int i=0; i<channelCount; i++) {
					int channelIndex = dmxStart + (reverse ? (channelCount-i) : i) - 1;
					System.out.print((channelIndex+1) + " ");
					if ((i%20) == 0) {
						System.out.println();
					}
					for (int l=0; l<255; l+=(255/10)) {
						Arrays.fill(data, (byte) 0);
						data[channelIndex] = (byte) l;

						socket.sendDmx(universe, data);
						Uninterruptibles.sleepUninterruptibly(20, TimeUnit.MILLISECONDS);
					}
					Uninterruptibles.sleepUninterruptibly(300, TimeUnit.MILLISECONDS);
				}

				Arrays.fill(data, (byte) 0);
				socket.sendDmx(universe, data);

				System.out.println("Done.");
				System.out.println();
			}
		}
	}
}
