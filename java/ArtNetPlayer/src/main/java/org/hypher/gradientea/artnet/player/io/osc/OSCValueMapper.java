package org.hypher.gradientea.artnet.player.io.osc;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortIn;

import java.net.SocketException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple wrapper for dealing with OSC-controlled values.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class OSCValueMapper {
	private static OSCValueMapper instance;
	public static OSCValueMapper instance() {
		if (instance == null) {
			instance = new OSCValueMapper();
		}

		return instance;
	}

	private OSCPortIn receiver;
	private Multimap<String, MappedValue> mappedValues = ArrayListMultimap.create();

	public OSCValueMapper() {
		start();
	}

	private void start() {
		try {
			receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}

		receiver.addListener("/.*",  new OSCListener() {
			public void acceptMessage(java.util.Date time, OSCMessage message) {
				System.out.println("OSC: " + message.getAddress() + ": " + Joiner.on(", ").join(message.getArguments()));
			}
		});
		receiver.startListening();
	}

	public <T extends MappedValue> T mapValue(String addressPattern, final T mappedValue) {
		receiver.addListener(addressPattern, new OSCListener() {
			@Override
			public void acceptMessage(final Date time, final OSCMessage message) {
				mappedValue.applyValue(message.getAddress(), message.getArguments());
			}
		});
		return mappedValue;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion

	public interface MappedValue {
		void applyValue(final String address, Object[] params);
	}

	public static class OscMultitouch implements MappedValue {
		private final double touchLifetimeSeconds;
		private Map<Integer, Touch> touches = Maps.newConcurrentMap();

		public OscMultitouch(final double touchLifetimeSeconds) {
			this.touchLifetimeSeconds = touchLifetimeSeconds;
		}

		@Override
		public void applyValue(final String address, final Object[] params) {
			int index = Integer.parseInt(address.substring(address.lastIndexOf('/')+1));
			cullTouches();

			synchronized (touches) {
				if (touches.containsKey(index)) {
					touches.get(index).setXY(
						((Number) params[0]).doubleValue(),
						((Number) params[1]).doubleValue()
					);
				} else {
					touches.put(index, new Touch(
						((Number) params[0]).doubleValue(),
						((Number) params[1]).doubleValue()
					));
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
		return OSCValueMapper.instance().mapValue(path + "/\\d+", new OscMultitouch(touchLifetimeSeconds));
	}

	public static abstract class MappedDouble implements MappedValue {
		double minValue = 0;
		double maxValue = 1.0;

		protected MappedDouble(final double minValue, final double maxValue) {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}

		public final void applyValue(final String address, Object[] args) {
			if (args.length > 0 && args[0] instanceof Number) {
				Number val = (Number) args[0];
				double mappedVal = minValue + (maxValue-minValue) * val.doubleValue();

				applyDouble(mappedVal);
			}
		}

		public abstract void applyDouble(double value);
	}

	public static class OscDouble extends MappedDouble {
		private double value;

		public OscDouble(final double minValue, final double maxValue, final double value) {
			super(minValue, maxValue);
			this.value = value;
		}

		@Override
		public void applyDouble(final double value) {
			this.value = value;
		}

		public float floatValue() {
			return (float) value;
		}

		public double doubleValue() {
			return value;
		}
	}

	public static class OscBoolean implements MappedValue {
		private boolean value;

		public OscBoolean(final boolean value) {
			this.value = value;
		}

		public boolean value() {
			return value;
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
	}

	public static class OscXY implements MappedValue {
		private double x, y;

		public OscXY(final double x, final double y) {
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

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
	}

	public static OscDouble doubleValue(
		String path,
		final double minValue,
		final double maxValue,
		final double value
	) {
		return OSCValueMapper.instance().mapValue(path, new OscDouble(minValue, maxValue, value));
	}

	public static OscXY xyValue(
		String path,
		final double initialX,
		final double initialY
	) {
		return OSCValueMapper.instance().mapValue(path, new OscXY(initialX, initialY));
	}

	public static OscBoolean booleanValue(final String path, final boolean defaultValue) {
		return OSCValueMapper.instance().mapValue(path, new OscBoolean(defaultValue));
	}
}
