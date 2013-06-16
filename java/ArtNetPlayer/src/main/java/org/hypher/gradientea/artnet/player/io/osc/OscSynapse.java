package org.hypher.gradientea.artnet.player.io.osc;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;
import org.hypher.gradientea.geometry.shared.GeoVector3;

import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.Set;

/**
 * A utility to integrate with Synapse.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class OscSynapse {
	public static int INPUT_PORT = 12345;
	public static int OUTPUT_PORT = 12346;

	private static OscSynapse instance;
	public static OscSynapse instance() {
		if (instance == null) {
			instance = new OscSynapse();
			instance.start(INPUT_PORT);
		}

		return instance;
	}

	private SourceAwareOSCPortIn receiver;
	private Multimap<String, OscJointValue> mappedValues = ArrayListMultimap.create();

	public OscSynapse() {}

	private void start() {
		start(OSCPort.defaultSCOSCPort());
	}

	private void start(final int outgoingPort) {
		try {
			receiver = new SourceAwareOSCPortIn(outgoingPort);
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}

		// Add listeners so we can enable tracking specific joints
		receiver.addListener(".*", new OSCListener() {
			long joinRequestLastSentAt = System.currentTimeMillis();

			@Override
			public void acceptMessage(final Date time, final OSCMessage message) {
				try {
					if (message instanceof SourceAwareOSCPortIn.SourceAwareOSCMessage) {
						final OSCPortOut portOut = new OSCPortOut(
							((SourceAwareOSCPortIn.SourceAwareOSCMessage) message).getSenderAddress(),
							OUTPUT_PORT
						);

						if (System.currentTimeMillis() - joinRequestLastSentAt > 2000) {
							joinRequestLastSentAt = System.currentTimeMillis();

							OSCBundle bundle = new OSCBundle();
							for (OscJointValue value : mappedValues.values()) {
								bundle.addPacket(new OSCMessage(
									"/" + value.getJoint().getId() + "_trackjointpos",
									ImmutableList.<Object>of(1)
								));

								bundle.addPacket(new OSCMessage(
									"/" + value.getJoint().getId() + "_trackjointpos",
									ImmutableList.<Object>of(2)
								));

								bundle.addPacket(new OSCMessage(
									"/" + value.getJoint().getId() + "_trackjointpos",
									ImmutableList.<Object>of(3)
								));
							}

							portOut.send(bundle);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		receiver.addListener("/righthand_pos_body", new ExtremePrinter());
		receiver.addListener("/righthand_pos_screen", new ExtremePrinter());
		receiver.addListener("/righthand_pos_world", new ExtremePrinter());

		// Debug listener
		receiver.addListener("/.*",  new OSCListener() {
			private Set<String> ignoredAddresses = ImmutableSet.of(

			);
			public void acceptMessage(Date time, OSCMessage message) {
				if (! message.getAddress().contains("_pos_")) {
					//System.out.println("OscSynapse: " + message.getAddress() + ": " + Joiner.on(", ").join(message.getArguments()));
				}
			}
		});
		receiver.startListening();
	}

	public <T extends OscJointValue> T mapValue(String addressPattern, final T mappedValue) {
		receiver.addListener(addressPattern, new OSCListener() {
			@Override
			public void acceptMessage(final Date time, final OSCMessage message) {
				mappedValue.applyValue(message.getAddress(), message.getArguments());
			}
		});
		mappedValues.put(addressPattern, mappedValue);
		return mappedValue;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion

	public interface OscJointValue {
		void applyValue(final String address, Object[] params);
		Joint getJoint();
	}


	public static enum Joint {
		RIGHTHAND("righthand"),
		LEFTHAND("lefthand"),
		RIGHTELBOW("rightelbow"),
		LEFTELBOW("leftelbow"),
		RIGHTFOOT("rightfoot"),
		LEFTFOOT("leftfoot"),
		RIGHTKNEE("rightknee"),
		LEFTKNEE("leftknee"),
		HEAD("head"),
		TORSO("torso"),
		LEFTSHOULD("leftshoulderER"),
		RIGHTSHOULDER("rightshoulder"),
		LEFTHIP("lefthip"),
		RIGHTHIP("righthip"),
		CLOSESTHAND("closesthand");

		String id;

		private Joint(final String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}


	public static class OscJoint implements OscJointValue {
		private Joint joint;
		private double x, y, z;
		private double lastX, lastY, lastZ;

		public OscJoint(final Joint joint) {
			this.joint = joint;
		}

		@Override
		public void applyValue(final String address, final Object[] params) {
			lastX = x;
			lastY = y;
			lastZ = z;

			if (params.length >= 2 && params[0] instanceof Number && params[1] instanceof Number) {
				this.x = ((Number) params[0]).doubleValue() / 640;
				this.y = ((Number) params[1]).doubleValue() / 480;
				this.z = ((Number) params[2]).doubleValue() / 640;
			}
		}

		@Override
		public Joint getJoint() {
			return joint;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public double getZ() {
			return z;
		}

		public double getLastX() {
			return lastX;
		}

		public double getLastY() {
			return lastY;
		}

		public double getLastZ() {
			return lastZ;
		}

		public double getDeltaX() {
			return x - lastX;
		}

		public double getDeltaY() {
			return y - lastY;
		}

		public double getAngle() {
			return Math.atan2(getDeltaY(), getDeltaX());
		}

		public double getVelocity() {
			return Math.sqrt(Math.pow(getDeltaX(),2) + Math.pow(getDeltaY(),2));
		}
	}

	public static class OscJointVector {
		private GeoVector3 lastPositionMeters;
		private double lastPositionTimeSeconds;

		private GeoVector3 currentPositionMeters;
		private double currentPositionTimeSeconds;


		public double getVelocity() {
			return currentPositionMeters.distanceTo(lastPositionMeters) / (currentPositionTimeSeconds - lastPositionTimeSeconds);
		}


	}

	public static OscJoint jointScreen(
		Joint joint
	) {
		return instance().mapValue("/" + joint.getId() + "_pos_screen", new OscJoint(joint));
	}

	private class ExtremePrinter implements OSCListener {
		double minX=Double.MAX_VALUE, maxX=0;
		double minY=Double.MAX_VALUE, maxY=0;
		double minZ=Double.MAX_VALUE, maxZ=0;
		long lastPrint = System.currentTimeMillis();

		@Override
		public void acceptMessage(final Date time, final OSCMessage message) {
			minX = Math.min(minX, ((Number) message.getArguments()[0]).doubleValue());
			maxX = Math.max(maxX, ((Number) message.getArguments()[0]).doubleValue());

			minY = Math.min(minY, ((Number) message.getArguments()[1]).doubleValue());
			maxY = Math.max(maxY, ((Number) message.getArguments()[1]).doubleValue());

			minZ = Math.min(minZ, ((Number) message.getArguments()[2]).doubleValue());
			maxZ = Math.max(maxZ, ((Number) message.getArguments()[2]).doubleValue());

			if (System.currentTimeMillis() - lastPrint > 5000) {
				lastPrint = System.currentTimeMillis();

				System.out.println(
					message.getAddress() + ": ("+minX+"-"+maxX+", "+minY+"-"+maxY+", "+minZ+"-"+maxZ+")"
				);
			}
		}
	}
}
