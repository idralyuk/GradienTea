package org.hypher.gradientea.artnet.player;

import fr.azelart.artnetstack.constants.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeAnimationServerMain {
	/**
	 * Port used for dome communication. Produced from the following scala:
	 * <code>(('d'.asInstanceOf[Int]<<8) | 'o'.asInstanceOf[Int]) ^ (('m'.asInstanceOf[Int]<<8) | 'e'.asInstanceOf[Int])</code>
	 */
	public static final short DOME_PORT = 2314;

	public static void main(String[] args) throws IOException {

		for (int i=0; i<args.length; i+=2) {
			String domeMappingFilename = args[i + 1];

			DmxDomeMapping mapping = new DmxDomeMapping();
			mapping.applyProperties(new FileInputStream(domeMappingFilename));

			ArtNetDomePlayer player = new ArtNetDomePlayer(mapping);
			player.start(
				InetAddress.getLocalHost(),
				InetAddress.getByName(args[i]),
				Constants.DEFAULT_ART_NET_UDP_PORT
			);

			HttpDomeAnimationReceiver httpReceiver = new HttpDomeAnimationReceiver(player);
			httpReceiver.start(DOME_PORT+i/2);

			UdpDomeAnimationReceiver udpReceiver = new UdpDomeAnimationReceiver(player);
			udpReceiver.start(DOME_PORT+i/2);

			System.out.println("Started UDP -> ArtNet DomeServer for " + domeMappingFilename + " on port " + DOME_PORT+(i/2));
		}
	}
}
