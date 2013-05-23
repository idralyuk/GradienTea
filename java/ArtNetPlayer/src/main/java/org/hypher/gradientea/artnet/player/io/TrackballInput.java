package org.hypher.gradientea.artnet.player.io;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Handles reading data from a trackball or mouse.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class TrackballInput {
	public static final List<String> preferredKeywords = ImmutableList.of("trackball", "mouse", "");
	public static final long RECHECK_INTERVAL_MS = 60000;

	private static TrackballInput instance;
	public static TrackballInput instance() {
		if (instance == null) {
			instance = new TrackballInput();
		}

		return instance;
	}

	private Controller controller;
	private long lastFind = 0;

	public Optional<TrackballReading> read() {
		if (controller == null || (System.currentTimeMillis() - lastFind) > RECHECK_INTERVAL_MS) {
			findController();
		}

		try {
			if (controller.poll()) {
				return Optional.of(
					new TrackballReading(
						controller.getComponent(Component.Identifier.Axis.X).getPollData(),
						controller.getComponent(Component.Identifier.Axis.Y).getPollData()
					)
				);
			} else {
				findController();
			}
		} catch (Exception e) {
			findController();
		}

		return Optional.absent();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Protected Methods

	private void findController() {
		try {
			ControllerEnvironment environment = ControllerEnvironment.getDefaultEnvironment();

			// Reset the list of controllers if possible
			if (controller != null) {
				final Constructor<? extends ControllerEnvironment> constructor =
					environment.getClass().getDeclaredConstructor();

				constructor.setAccessible(true);
				environment = constructor.newInstance();
			}

			final Controller[] controllers = environment.getControllers();

			for (String desiredNamePart : preferredKeywords) {
				for (Controller controller : controllers) {
					if (controller.getType() == Controller.Type.MOUSE && controller.getName().toLowerCase().contains(desiredNamePart)) {
						if (this.controller != controller) {
							this.controller = controller;
							System.out.println("Using trackball input: " + controller);
						}

						this.lastFind = System.currentTimeMillis();

						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			 /* Oh well, we can't reset the list. */
		}
	}

	//endregion

	public static class TrackballReading {
		private float deltaX;
		private float deltaY;

		public TrackballReading(final float deltaX, final float deltaY) {
			this.deltaX = deltaX;
			this.deltaY = deltaY;
		}

		public float getDeltaX() {
			return deltaX;
		}

		public float getDeltaY() {
			return deltaY;
		}

		@Override
		public String toString() {
			return "TrackballReading{" +
				"deltaX=" + deltaX +
				", deltaY=" + deltaY +
				'}';
		}
	}
}
