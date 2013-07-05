package org.hypher.gradientea.artnet.player.controller.programs;

import org.hypher.gradientea.artnet.player.controller.OscConstants;

/**
 * Animation Program interface. Animations implementing this are the primary programs that run on the dome and may be
 * selected by the user.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface DomeAnimationProgram extends DomeAnimation {
	ProgramId getProgramId();

	/**
	 * Called periodically to check if this program would like to be active.
	 */
	boolean isFocusDesired();

	enum ProgramId {
		OFF(OscConstants.Status.Controller.OFF, false),
		MANUAL(OscConstants.Status.Controller.MANUAL, true),
		MUSIC(OscConstants.Status.Controller.MUSIC, true),
		MOTION(OscConstants.Status.Controller.MOTION, true),
		DEBUG(OscConstants.Status.Controller.DEBUG, false);

		private String oscIndicatorAddress;
		private boolean fluidBased;

		private ProgramId(final String oscIndicatorAddress, final boolean fluidBased) {
			this.oscIndicatorAddress = oscIndicatorAddress;
			this.fluidBased = fluidBased;
		}

		public String getOscIndicatorAddress() {
			return oscIndicatorAddress;
		}

		public boolean isFluidBased() {
			return fluidBased;
		}
	}
}
