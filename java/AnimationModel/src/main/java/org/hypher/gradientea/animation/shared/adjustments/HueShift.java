package org.hypher.gradientea.animation.shared.adjustments;

import org.hypher.gradientea.animation.shared.HsbColor;

/**
 * Shifts the hue of a color by the specified amount
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class HueShift extends BaseColorAdjustment {
	protected double hueShift;

	protected HueShift() {}

	public HueShift(final double hueShift) {
		this.hueShift = hueShift % 1.0;
	}

	@Override
	public HsbColor adjust(final HsbColor color) {
		double shiftedHue = (color.getHue() + hueShift) % 1.0;

		return new HsbColor(
			shiftedHue < 0 ? (1.0-shiftedHue) : (shiftedHue),
			color.getHue(),
			color.getHue()
		);
	}
}
