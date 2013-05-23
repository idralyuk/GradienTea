package org.hypher.gradientea.artnet.player;

import org.hypher.gradientea.transport.shared.DomeAnimationFrame;
import org.hypher.gradientea.transport.shared.DomeAnimationTransport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * A simple animation receiver which accepts simple UDP packets and treats them as dome data.
 *
 * Each packet is expected to start with the magic DWORD 'dome', followed by the length of the frame as a 16-bit
 * unsigned integer.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class UdpDomeAnimationReceiver {

	private DomeAnimationTransport animationTransport;
	private DatagramSocket socket;
	private Thread receiveThread;

	public UdpDomeAnimationReceiver(final DomeAnimationTransport animationTransport) {
		this.animationTransport = animationTransport;
	}

	public void start() throws SocketException {
		start(DomeAnimationServerMain.DOME_PORT);
	}

	public void start(int port) throws SocketException {
		stop();

		socket = new DatagramSocket(port);
		receiveThread = new Thread(new Runnable() {
			@Override
			public void run() {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				byte[] packetBuffer = new byte[4096];
				DatagramPacket packet = new DatagramPacket(packetBuffer, 0, packetBuffer.length);

				Integer expectedLength = null;
				int currentLength = 0;

				while (socket != null) {
					try {
						socket.receive(packet);

						if (expectedLength == null) {
							// new frame
							if (packet.getLength() >= 6) {
								if (packetBuffer[0] == 'D'
									&& packetBuffer[1] == 'O'
									&& packetBuffer[2] == 'M'
									&& packetBuffer[3] == 'E'
								) {
									expectedLength = ((packetBuffer[4]<<8)&0xFF) | ((packetBuffer[5])&0xFF);

									baos.write(packet.getData(), 6, packet.getLength()-6);
									currentLength += packet.getLength()-6;
								} else {
									throw new IOException("Initial packet did not start with 'DOME'");
								}
							} else {
								throw new IOException("Initial packet was less than 6 bytes long; could not read magic or length");
							}
						} else {
							baos.write(packet.getData(), 0, packet.getLength());
							currentLength += packet.getLength();
						}

						if (expectedLength != null && currentLength >= expectedLength) {
							final byte[] data = baos.toByteArray();
							animationTransport.displayFrame(new DomeAnimationFrame(
								data
							));

							baos.reset();
							currentLength = 0;
							expectedLength = null;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		receiveThread.setDaemon(true);
		receiveThread.start();

		System.out.println("Started UDP Dome Animation server on port " + port);
	}

	private void stop() {
		if (socket != null) {
			socket.close();
			socket = null;
			receiveThread = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
