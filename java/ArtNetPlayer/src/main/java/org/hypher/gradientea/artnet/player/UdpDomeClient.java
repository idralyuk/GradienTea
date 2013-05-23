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

	public void connect(String host) throws SocketException, UnknownHostException {
		connect(host, DomeAnimationServerMain.DOME_PORT);
	}

	public void connect(String host, int port) throws UnknownHostException, SocketException {
		disconnect();

		socket = new DatagramSocket();
		socket.connect(InetAddress.getByName(host), port);
	}

	private void disconnect() {
		if (socket != null) {
			socket.disconnect();
			socket = null;
		}
	}

	@Override
	public void displayFrame(final DomeAnimationFrame frame) {
		byte[] pixelData = frame.getPixelData();
		byte[] buffer = new byte[pixelData.length + 6];
		buffer[0] = 'D';
		buffer[1] = 'O';
		buffer[2] = 'M';
		buffer[3] = 'E';

		buffer[4] = (byte) (pixelData.length << 8);
		buffer[5] = (byte) (pixelData.length & 0xFF);

		System.arraycopy(pixelData, 0, buffer, 6, pixelData.length);

		DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
		try {
			socket.send(packet);
		}
		catch (IOException e) {
			if (e instanceof PortUnreachableException) {
				System.err.println("Dome server not available");
			} else {
				e.printStackTrace();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {}
		}
	}
}
