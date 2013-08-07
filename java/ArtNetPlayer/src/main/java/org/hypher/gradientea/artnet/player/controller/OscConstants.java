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
			String DEBUG = "/gt/status/controller/debug";
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
			String SHOW_OVERLAY_ADDRESSES = "/gt/control/fluid/overlay/addresses";
			String SHOW_OUTPUT_OVERLAY = "/gt/control/fluid/overlay/output";
			String SHOW_FLUID_OVERLAY = "/gt/control/fluid/overlay/fluid";
			String SHOW_VERTICES = "/gt/control/fluid/overlay/vertices";
			String SHOW_OUTLINE = "/gt/control/fluid/overlay/outline";
			String INTENTISTY_MULTIPLIER = "/gt/control/fluid/intensityMultiplier";
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
			String EMITTER_MOVEMENT = "/gt/control/music/emitterMovement";
			String SHOW_HISTOGRAM = "/gt/control/music/showHistogram";
			String SHOW_EMITTERS = "/gt/control/music/showEmitters";
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

		interface Debug {
			String ENABLED = "/gt/control/debug/enabled";

			String ENABLE_1 = "/gt/control/debug/enable/1";
			String ENABLE_2 = "/gt/control/debug/enable/2";

			String SINGLE_PANEL_INDEX = "/gt/control/debug/single-panel";
			String SINGLE_PANEL_LABEL = "/gt/control/debug/single-panel/label";
			String ALL_PANELS_RED = "/gt/control/debug/all-panels/red";
			String ALL_PANELS_GREEN = "/gt/control/debug/all-panels/green";
			String ALL_PANELS_BLUE = "/gt/control/debug/all-panels/blue";

			String SINGLE_VERTEX_INDEX = "/gt/control/debug/single-vertex";
			String SINGLE_VERTEX_LABEL = "/gt/control/debug/single-vertex/label";
			String ALL_VERTICES_RED = "/gt/control/debug/all-vertices/red";
			String ALL_VERTICES_GREEN = "/gt/control/debug/all-vertices/green";
			String ALL_VERTICES_BLUE = "/gt/control/debug/all-vertices/blue";
		}

		interface Door {
			String INDEX_1 = "/gt/control/door/1/index";
			String INDEX_1_LABEL = "/gt/control/door/1/index-label";
			String INDEX_2 = "/gt/control/door/2/index";
			String INDEX_2_LABEL = "/gt/control/door/2/index-label";
		}

		interface Color {
			String PALETTE_TYPE = "/gt/control/color/paletteType";
			String PALETTE_HUE = "/gt/control/color/paletteHue";

			String PALETTE_TYPE_LABEL = "/gt/control/color/paletteTypeLabel";
			String PALETTE_HUE_LABEL = "/gt/control/color/paletteHueLabel";

			String PALETTE_COLOR_COUNT = "/gt/control/color/paletteColorCount";
		}
	}
}
