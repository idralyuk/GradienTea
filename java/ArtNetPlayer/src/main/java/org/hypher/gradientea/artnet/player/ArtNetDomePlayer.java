package org.hypher.gradientea.artnet.player;

import fr.azelart.artnetstack.server.ArtNetServer;
import fr.azelart.artnetstack.utils.ArtNetPacketEncoder;
import org.hypher.gradientea.transport.shared.DomeAnimationFrame;
import org.hypher.gradientea.transport.shared.DomeAnimationTransport;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ArtNetDomePlayer implements DomeAnimationTransport {
	protected ArtNetServer server;
	protected DmxDomeMapping mapping;

	protected int[][] dmxBuffer;

	public ArtNetDomePlayer(final DmxDomeMapping mapping) {
		this.mapping = mapping;
	}

	/*////////////////////////////////////////////////////////////////////////////////////////////////////////////////*/
	//region// Instance Methods

	public void start(
		final InetAddress listenAddress,
		final InetAddress broadcastAddress,
		final int port
	) throws IOException {
		server = new ArtNetServer(listenAddress, broadcastAddress, port);
		server.start();
	}

	@Override
	public void displayFrame(final DomeAnimationFrame frame) {
		if (dmxBuffer == null) {
			dmxBuffer = mapping.allocateBuffer();
		}

		mapping.map(frame.getPixelData(), dmxBuffer);

		try {
			for (int i=0; i<dmxBuffer.length; i++) {
				server.sendPacket(ArtNetPacketEncoder.encodeArtDmxPacket(
					i + 1,
					0,
					dmxBuffer[i]
				));
			}
		} catch (IOException e) {
			System.err.println("Failed to send frame because of " + e.getClass().getName() + ": " + e.getMessage());
		}
	}

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
