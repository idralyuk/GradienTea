package org.hypher.gradientea.artnet.player.io.osc;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility to aid in integration with OSC. The static methods provide a means to listen to specific OSC addresses
 * and interpret that data. This class is designed to be used with TouchOSC, and as such, as some code-specific to that
 * software.
 *
 * <p>Sending Data</p>
 * By default, {@link OscHelper} will detect "ping" type messages, and upon receiving them, will send the current
 * state of all variables to the sender of that message. The helper assumes that the data sender is configured to
 * listen one port higher than it is sending data on. For example, if using the default port 57120, data will be
 * sent to 57121.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class OscHelper {
	private static final int OSC_PORT = 4242;

	private static OscHelper instance;
	public static OscHelper instance() {
		if (instance == null) {
			instance = new OscHelper();
			instance.start();
		}

		return instance;
	}

	private Map<String, OscHost> knownHosts = Maps.newHashMap();
	private SourceAwareOSCPortIn receiver;
	private Multimap<String, WritableOscValue> mappedValues = ArrayListMultimap.create();

	public OscHelper() {}

	private void start() {
		start(OSC_PORT);
	}

	private void start(final int outgoingPort) {
		try {
			receiver = new SourceAwareOSCPortIn(outgoingPort);
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}

		// Debug listener
		receiver.addListener("/.*",  new OSCListener() {

			private Set<String> ignoredAddresses = ImmutableSet.of(
				"/accxyz"
			);
			public void acceptMessage(java.util.Date time, OSCMessage message) {
				if (message instanceof SourceAwareOSCPortIn.SourceAwareOSCMessage) {
					String hostname = ((SourceAwareOSCPortIn.SourceAwareOSCMessage) message).getSenderAddress().toString();

					if (! knownHosts.containsKey(hostname)) {
						knownHosts.put(
							hostname, new OscHost(
							((SourceAwareOSCPortIn.SourceAwareOSCMessage) message).getSenderAddress(),
							outgoingPort + 1
						)
						);
					}
				}

				if (message.getAddress().startsWith("/ping")) {
					pushToKnownHosts();
				}

				if (! ignoredAddresses.contains(message.getAddress())) {
					System.out.println("OSC: " + message.getAddress() + ": " + Joiner.on(", ").join(message.getArguments()));
				}
			}
		});
		receiver.startListening();
	}

	public void pushToKnownHosts() {
		final OSCBundle bundle = new OSCBundle();

		for (WritableOscValue value : mappedValues.values()) {
			if (value instanceof ReadableOscValue) {
				final Optional<Map<String, Object[]>> currentValues = ((ReadableOscValue) value).getCurrentAddressValues();

				if (currentValues.isPresent()) {
					for (Map.Entry<String, Object[]> entry : currentValues.get().entrySet()) {
						bundle.addPacket(new OSCMessage(
							entry.getKey(),
							Arrays.asList(entry.getValue())
						));
					}
				}
			}
		}

		for (OscHost host : knownHosts.values()) {
			final Optional<OSCPortOut> outPort = host.getOutPort();
			if (outPort.isPresent()) {
				try {
					outPort.get().send(bundle);
				} catch (IOException e) {
					System.err.println("Failed to send to OSC Host " + host + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}
	}

	public <T extends WritableOscValue> T mapValue(String addressPattern, final T mappedValue) {
		receiver.addListener(addressPattern, new OSCListener() {
			@Override
			public void acceptMessage(final Date time, final OSCMessage message) {
				mappedValue.applyValue(message.getAddress(), message.getArguments());
			}
		});
		mappedValues.put(addressPattern, mappedValue);
		return mappedValue;
	}

	public <T extends BaseSimpleValue> T mapValue(final T mappedValue) {
		receiver.addListener(mappedValue.getAddress(), new OSCListener() {
			@Override
			public void acceptMessage(final Date time, final OSCMessage message) {
				mappedValue.applyValue(message.getAddress(), message.getArguments());
			}
		});
		mappedValues.put(mappedValue.getAddress(), mappedValue);
		return mappedValue;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion

	public interface WritableOscValue {
		void applyValue(final String address, Object[] params);
	}

	public interface ReadableOscValue {
		Optional<Map<String, Object[]>> getCurrentAddressValues();
	}

	public static abstract class BaseSimpleValue implements WritableOscValue, ReadableOscValue {
		private String address;

		protected BaseSimpleValue(final String address) {
			this.address = address;
		}

		protected abstract Object[] getCurrentOscValue();

		public String getAddress() {
			return address;
		}

		@Override
		public Optional<Map<String, Object[]>> getCurrentAddressValues() {
			return Optional.<Map<String, Object[]>>of(ImmutableMap.of(
				address, getCurrentOscValue()
			));
		}
	}

	public static class OscMultitouch implements WritableOscValue {
		private final double touchLifetimeSeconds;
		private Map<Integer, Touch> touches = Maps.newConcurrentMap();
		private final static Pattern ADDRESS_PATTERN = Pattern.compile(".*/(\\d+)(/z)?");

		public OscMultitouch(final double touchLifetimeSeconds) {
			this.touchLifetimeSeconds = touchLifetimeSeconds;
		}

		@Override
		public void applyValue(final String address, final Object[] params) {
			Matcher matcher = ADDRESS_PATTERN.matcher(address);

			if (matcher.find()) {
				int touchIndex = Integer.parseInt(matcher.group(1));

				synchronized (touches) {
					if (matcher.group(2) != null) {
						// control data
						if (((Number) params[0]).intValue() == 0) {
							// Touch ended
							touches.remove(touchIndex);
						}
					} else {
						cullTouches();

						if (touches.containsKey(touchIndex)) {
							touches.get(touchIndex).setXY(
								((Number) params[0]).doubleValue(),
								((Number) params[1]).doubleValue()
							);
						} else {
							touches.put(
								touchIndex, new Touch(
								((Number) params[0]).doubleValue(),
								((Number) params[1]).doubleValue()
							)
							);
						}
					}
				}

			}
		}

		public Map<Integer, Touch> getTouches() {
			cullTouches();

			return touches;
		}

		private void cullTouches() {
			synchronized (touches) {
				for (Iterator<Map.Entry<Integer, Touch>> i = touches.entrySet().iterator(); i.hasNext();) {
					Map.Entry<Integer, Touch> entry = i.next();

					if (entry.getValue().isDead()) {
						i.remove();;
					}
				}
			}
		}

		public class Touch {
			private long lastUpdate;

			private double initialX;
			private double initialY;

			private double lastX;
			private double lastY;

			private double currentX;
			private double currentY;

			public Touch(final double initialX, final double initialY) {
				lastUpdate = System.currentTimeMillis();
				this.initialX = this.lastX = this.currentX = initialX;
				this.initialY = this.lastY = this.currentY = initialY;
			}

			private void setXY(double newX, double newY) {
				lastX = currentX;
				lastY = currentY;

				currentX = newX;
				currentY = newY;

				lastUpdate = System.currentTimeMillis();
			}

			public double getInitialX() {
				return initialX;
			}

			public double getInitialY() {
				return initialY;
			}

			public double getDeltaX() {
				return currentX - lastX;
			}

			public double getDeltaY() {
				return currentY - lastY;
			}

			public double getAngle() {
				return Math.atan2(getDeltaY(), getDeltaX());
			}

			public double getVelocity() {
				return Math.sqrt(Math.pow(getDeltaX(),2) + Math.pow(getDeltaY(),2));
			}

			public double getCurrentX() {
				return currentX;
			}

			public double getCurrentY() {
				return currentY;
			}

			public boolean isDead() {
				return (System.currentTimeMillis()-lastUpdate) > touchLifetimeSeconds*1000;
			}
		}
	}

	public static OscMultitouch multitouch(final String path, double touchLifetimeSeconds) {
		return OscHelper.instance().mapValue(path + "/\\d+(/z)?", new OscMultitouch(touchLifetimeSeconds));
	}

	public static OscMultitouch multitouch(final String path) {
		return multitouch(path, 10.0);
	}

	public static class OscDouble extends BaseSimpleValue {
		double minValue = 0;
		double maxValue = 1.0;

		double value;

		private String lastAddress;

		public OscDouble(String address, final double minValue, final double maxValue, final double value) {
			super(address);

			this.minValue = minValue;
			this.maxValue = maxValue;
			this.value = value;
		}

		public final void applyValue(final String address, Object[] args) {
			if (args.length > 0 && args[0] instanceof Number) {
				lastAddress = address;

				value = minValue + (maxValue-minValue) * ((Number) args[0]).doubleValue();
				applyDouble(value);
			}
		}

		protected void applyDouble(double value) {}

		@Override
		protected Object[] getCurrentOscValue() {
			return new Object[] {(float) ((getValue() - minValue) / (maxValue - minValue))};
		}

		public double getValue() {
			return value;
		}

		public float floatValue() {
			return (float) getValue();
		}
	}

	public static class OscBoolean extends BaseSimpleValue {
		private boolean value;

		public OscBoolean(final String address, final boolean value) {
			super(address);
			this.value = value;
		}

		public boolean value() {
			return value;
		}

		public void setValue(final boolean value) {
			this.value = value;
		}

		@Override
		public void applyValue(final String address, final Object[] params) {
			if (params.length >= 1) {
				if (params[0] instanceof Boolean) {
					this.value = (Boolean) params[0];
				}
				else if (params[0] instanceof Number) {
					this.value = ((Number) params[0]).doubleValue() > 0.5;
				}
			}
		}

		@Override
		protected Object[] getCurrentOscValue() {
			return new Object[] { value ? 1f : 0f };
		}
	}

	public static class OscXY extends BaseSimpleValue {
		private double x, y;

		public OscXY(final String address, final double x, final double y) {
			super(address);
			this.x = x;
			this.y = y;
		}

		@Override
		public void applyValue(final String address, final Object[] params) {
			if (params.length >= 2 && params[0] instanceof Number && params[1] instanceof Number) {
				this.x = ((Number) params[0]).doubleValue();
				this.y = ((Number) params[1]).doubleValue();
			}
		}


		@Override
		protected Object[] getCurrentOscValue() {
			return new Object[] {(float) x, (float) y};
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
	}

	public static class OscAccelerometer implements WritableOscValue {
		private double rawX;
		private double rawY;
		private double rawZ;

		@Override
		public void applyValue(final String address, final Object[] params) {
			if (
				params.length == 3
					&& params[0] instanceof Number
					&& params[1] instanceof Number
					&& params[2] instanceof Number
				) {
				rawX = ((Number) params[0]).doubleValue();
				rawY = ((Number) params[1]).doubleValue();
				rawZ = ((Number) params[2]).doubleValue();
			}
		}

		public double getRawX() {
			return rawX;
		}

		public double getRawY() {
			return rawY;
		}

		public double getRawZ() {
			return rawZ;
		}
	}

	public static OscDouble doubleValue(
		String address,
		final double minValue,
		final double maxValue,
		final double value
	) {
		return OscHelper.instance().mapValue(address, new OscDouble(address, minValue, maxValue, value));
	}

	public static OscXY xyValue(
		String address,
		final double initialX,
		final double initialY
	) {
		return OscHelper.instance().mapValue(address, new OscXY(address, initialX, initialY));
	}

	public static OscBoolean booleanValue(final String address, final boolean defaultValue) {
		return OscHelper.instance().mapValue(address, new OscBoolean(address, defaultValue));
	}

	public static OscAccelerometer accelerometer = OscHelper.instance().mapValue("/accxyz", new OscAccelerometer());

	protected static class OscHost {
		InetAddress address;
		int outgoingPort;
		OSCPortOut outPort;

		public OscHost(final InetAddress address, final int outgoingPort) {
			this.address = address;
			this.outgoingPort = outgoingPort;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final OscHost oscHost = (OscHost) o;

			if (outgoingPort != oscHost.outgoingPort) return false;
			if (address != null ? !address.equals(oscHost.address) : oscHost.address != null) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = address != null ? address.hashCode() : 0;
			result = 31 * result + outgoingPort;
			return result;
		}

		public Optional<OSCPortOut> getOutPort() {
			if (outPort == null) {
				try {
					outPort = new OSCPortOut(
						address,
						outgoingPort
					);
				} catch (SocketException e) {
					return Optional.absent();
				}
			}
			return Optional.of(outPort);
		}
	}
}
