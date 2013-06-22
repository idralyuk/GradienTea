package org.hypher.gradientea.artnet.player;

import org.hypher.gradientea.transport.shared.DomeAnimationFrame;
import org.hypher.gradientea.transport.shared.DomeAnimationTransport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * A client for the {@link UdpDomeAnimationReceiver}.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class UdpDomeClient implements DomeAnimationTransport {

	private DatagramSocket socket;
	private DomeIdentifier domeIdentifier = DomeIdentifier.Unknown;

	public void connect(String host) throws SocketException, UnknownHostException {
		connect(host, DomeAnimationServerMain.DOME_PORT);
	}

	public void connect(String host, int port) throws UnknownHostException, SocketException {
		disconnect();

		socket = new DatagramSocket();
		socket.connect(InetAddress.getByName(host), port);
	}

	public DomeIdentifier getDomeIdentifier() {
		return domeIdentifier;
	}

	public void setDomeIdentifier(final DomeIdentifier domeIdentifier) {
		this.domeIdentifier = domeIdentifier;
	}

	private void disconnect() {
		if (socket != null) {
			socket.disconnect();
			socket = null;
		}
	}

	private int frameIndex = 0;

	@Override
	public void displayFrame(final DomeAnimationFrame frame) {
		if (socket == null) return;

		byte[] faceData = frame.getFacePixelData();
		byte[] vertexData = frame.getVertexPixelData();

		byte[] buffer = new byte[faceData.length + vertexData.length + 9];
		buffer[0] = 'D';
		buffer[1] = 'O';
		buffer[2] = 'M';
		buffer[3] = 'E';

		buffer[4] = (byte) (domeIdentifier.ordinal());

		buffer[5] = (byte) (faceData.length >> 8);
		buffer[6] = (byte) (faceData.length & 0xFF);

		buffer[7] = (byte) (vertexData.length >> 8);
		buffer[8] = (byte) (vertexData.length & 0xFF);

		System.arraycopy(faceData, 0, buffer, 9, faceData.length);
		System.arraycopy(vertexData, 0, buffer, 9 + faceData.length, vertexData.length);

		DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
		try {
			socket.send(packet);
		}
		catch (IOException e) {
			if (e instanceof PortUnreachableException) {
				if ((frameIndex++)%100 == 0) {
					System.err.println("Dome server at " + socket.getInetAddress() + " not available");
				}
			} else {
				e.printStackTrace();
			}
		}
	}
}
