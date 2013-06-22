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

				Integer expectedDataLength = null;

				int expectedFaceLength=0, expectedVertexLength=0;

				int currentLength = 0;
				DomeIdentifier domeIdentifier = DomeIdentifier.Unknown;

				while (socket != null) {
					try {
						socket.receive(packet);

						if (expectedDataLength == null) {
							// new frame
							if (packet.getLength() >= 8) {
								if (packetBuffer[0] == 'D'
									&& packetBuffer[1] == 'O'
									&& packetBuffer[2] == 'M'
									&& packetBuffer[3] == 'E'
								) {
									domeIdentifier = DomeIdentifier.values()[packetBuffer[4]];

									expectedFaceLength = (packetBuffer[5]&0xFF)<<8 | packetBuffer[6]&0xFF;
									expectedVertexLength = (packetBuffer[7]&0xFF)<<8 | packetBuffer[8]&0xFF;
									expectedDataLength = expectedFaceLength + expectedVertexLength;

									baos.write(packet.getData(), 9, packet.getLength()-9);
									currentLength += packet.getLength()-9;
								} else {
									throw new IOException("Initial packet did not start with 'DOME'");
								}
							} else {
								throw new IOException("Initial packet was less than 7 bytes long; could not read magic or length");
							}
						} else {
							baos.write(packet.getData(), 0, packet.getLength());
							currentLength += packet.getLength();
						}

						if (expectedDataLength != null && currentLength >= expectedDataLength) {
							final byte[] allData = baos.toByteArray();
							final byte[] faceData = new byte[expectedFaceLength];
							final byte[] vertexData = new byte[expectedVertexLength];

							System.arraycopy(allData, 0, faceData, 0, expectedFaceLength);
							System.arraycopy(allData, expectedFaceLength, vertexData, 0, expectedVertexLength);

							animationTransport.displayFrame(new DomeAnimationFrame(
								faceData,
								vertexData
							));

							baos.reset();
							currentLength = 0;
							expectedDataLength = null;
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
