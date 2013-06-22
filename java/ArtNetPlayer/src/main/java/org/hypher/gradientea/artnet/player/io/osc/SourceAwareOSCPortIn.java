package org.hypher.gradientea.artnet.player.io.osc;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.utility.OSCByteArrayToJavaConverter;
import com.illposed.osc.utility.OSCPacketDispatcher;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Mostly copied from {@link OSCPortIn}, but adds support for determining the host address and port of incoming
 * messages.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class SourceAwareOSCPortIn extends OSCPort implements Runnable {

	// state for listening
	private boolean listening;
	private OSCByteArrayToJavaConverter converter
		= new OSCByteArrayToJavaConverter();
	private OSCPacketDispatcher dispatcher = new OSCPacketDispatcher();

	/**
	 * Create an OSCPort that listens on the specified port.
	 * @param port UDP port to listen on.
	 * @throws SocketException
	 */
	public SourceAwareOSCPortIn(int port) throws SocketException {
		super(new DatagramSocket(port), port);
	}

	public void dispatchPacket(OSCPacket packet) {
		dispatcher.dispatchPacket(packet);
	}

	/**
	 * Buffers were 1500 bytes in size, but were
	 * increased to 1536, as this is a common MTU.
	 */
	private static final int BUFFER_SIZE = 1536;

	/**
	 * Run the loop that listens for OSC on a socket until
	 * {@link #isListening()} becomes false.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		byte[] buffer = new byte[BUFFER_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
		DatagramSocket socket = getSocket();
		while (listening) {
			try {
				try {
					socket.receive(packet);
				} catch (SocketException ex) {
					if (listening) {
						throw ex;
					} else {
						// if we closed the socket while receiving data,
						// the exception is expected/normal, so we hide it
						continue;
					}
				}

				OSCPacket oscPacket = addSourceInfoTo(
					converter.convert(buffer, packet.getLength()),
					packet.getAddress(),
					packet.getPort()
				);
				dispatcher.dispatchPacket(oscPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected OSCPacket addSourceInfoTo(final OSCPacket oscPacket, final InetAddress address, final int port) {
		if (oscPacket instanceof SourceAwareOSCMessage) {
			return oscPacket;
		}
		else if (oscPacket instanceof OSCMessage) {
			return new SourceAwareOSCMessage(
				address, port,
				((OSCMessage) oscPacket).getAddress(),
				((OSCMessage) oscPacket).getArguments()
			);
		}
		else if (oscPacket instanceof OSCBundle) {
			return new OSCBundle(
				FluentIterable.from(Arrays.asList(((OSCBundle) oscPacket).getPackets()))
					.transform(
						new Function<OSCPacket, OSCPacket>() {
							@Nullable
							@Override
							public OSCPacket apply(@Nullable final OSCPacket input) {
								return addSourceInfoTo(input, address, port);
							}
						}
					)
					.toImmutableList()
				,
				((OSCBundle) oscPacket).getTimestamp()
			);
		}
		else {
			throw new IllegalArgumentException("Unsupported OSCPacket: " + oscPacket);
		}
	}

	/**
	 * Start listening for incoming OSCPackets
	 */
	public void startListening() {
		listening = true;
		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Stop listening for incoming OSCPackets
	 */
	public void stopListening() {
		listening = false;
	}

	/**
	 * Am I listening for packets?
	 */
	public boolean isListening() {
		return listening;
	}

	/**
	 * Register the listener for incoming OSCPackets addressed to an Address
	 * @param anAddress  the address to listen for
	 * @param listener   the object to invoke when a message comes in
	 */
	public void addListener(String anAddress, OSCListener listener) {
		dispatcher.addListener(anAddress, listener);
	}

	public static class SourceAwareOSCMessage extends OSCMessage {
		private InetAddress senderAddress;
		private int senderPort;

		public SourceAwareOSCMessage(
			final InetAddress senderAddress,
			final int senderPort,
			final String address,
			final Object[] arguments
		) {
			super(address, Arrays.asList(arguments));
			this.senderAddress = senderAddress;
			this.senderPort = senderPort;
		}

		public InetAddress getSenderAddress() {
			return senderAddress;
		}

		public int getSenderPort() {
			return senderPort;
		}
	}
}
