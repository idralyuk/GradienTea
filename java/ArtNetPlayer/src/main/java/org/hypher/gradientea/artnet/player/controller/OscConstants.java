package org.hypher.gradientea.artnet.player.controller;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface OscConstants {
	interface Status {
		String HEART_BEAT = "/gt/status/heartbeat";

		interface Controller {
			String OFF = "/gt/status/controller/off";
			String MANUAL = "/gt/status/controller/manual";
			String MOTION = "/gt/status/controller/motion";
			String MUSIC = "/gt/status/controller/music";
		}
	}

	interface Control {
		interface Presets {
			String OFF = "/gt/control/presets/off";
			String SOFT_MUSIC = "/gt/control/presets/softMusic";
			String LOUD_MUSIC = "/gt/control/presets/loudMusic";
		}

		interface Fluid {
			String MANUAL_PAD = "/gt/control/fluid/manualPad";

			String FLUID_SIZE = "/gt/control/fluid/size";
			String VISCOSITY = "/gt/control/fluid/viscosity";
			String SPEED = "/gt/control/fluid/speed";
			String FADE = "/gt/control/fluid/fade";

			String SHOW_DOME_OVERLAY_1 = "/gt/control/fluid/overlay/1";
			String SHOW_DOME_OVERLAY_2 = "/gt/control/fluid/overlay/2";
		}

		interface Music {
			String FREQ_BANDS = "/gt/control/music/freqBands";
			String FREQ_LOW = "/gt/control/music/freqLow";
			String FREQ_HIGH = "/gt/control/music/freqHigh";
			String SENSITIVITY = "/gt/control/music/sensitivity";

			String VELOCITY = "/gt/control/music/velocity";
			String INTENSITY = "/gt/control/music/intensity";
			String SUSTAIN = "/gt/control/music/sustain";

			String EMITTER_RADIUS = "/gt/control/music/emitterRadius";
			String EMITTER_ROTATION = "/gt/control/music/emitterRotation";
			String COLOR_ROTATION = "/gt/control/music/colorRotation";
		}

		interface Motion {
			String ENABLED = "/gt/control/motion/enabled";

			String ENABLE_LEFT_HAND = "/gt/control/motion/enableLeftHand";
			String ENABLE_RIGHT_HAND = "/gt/control/motion/enableRightHand";

			String OFFSET = "/gt/control/motion/offset";
			String SCALE = "/gt/control/motion/scale";

			String VELOCITY = "/gt/control/motion/velocity";
			String INTENSITY = "/gt/control/motion/intensity";

			String CUTOFF = "/gt/control/motion/cutoff";
			String COLOR_ROTATION = "/gt/control/motion/colorRotation";
		}
	}
}
