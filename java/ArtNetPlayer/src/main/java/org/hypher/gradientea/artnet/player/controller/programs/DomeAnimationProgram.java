package org.hypher.gradientea.artnet.player.controller.programs;

import org.hypher.gradientea.artnet.player.controller.DomeController;
import org.hypher.gradientea.artnet.player.controller.OscConstants;

/**
 * Interface for "animation programs" -- the providers of the animation and interaction for the dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface DomeAnimationProgram {
	ProgramId getProgramId();

	/**
	 * Called to initialize the program. Will be called once.
	 */
	void init(DomeController controller);

	/**
	 * Called when this program has been selected to run.
	 */
	void start();

	/**
	 * Called on each frame. Program logic should be implemented here.
	 */
	void update();

	/**
	 * Called periodically to check if this program would like to be active.
	 */
	boolean isFocusDesired();

	/**
	 * Called when another program has been selected to run. This should stop any threads or other processing
	 * to allow other programs to use the resources.
	 */
	void stop();

	enum ProgramId {
		OFF(OscConstants.Status.Controller.OFF),
		MANUAL(OscConstants.Status.Controller.MANUAL),
		MUSIC(OscConstants.Status.Controller.MUSIC),
		MOTION(OscConstants.Status.Controller.MOTION);

		private String oscIndicatorAddress;

		private ProgramId(final String oscIndicatorAddress) {
			this.oscIndicatorAddress = oscIndicatorAddress;
		}

		public String getOscIndicatorAddress() {
			return oscIndicatorAddress;
		}
	}
}
