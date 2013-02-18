package org.hypher.gradientea.animation.shared.adjustments;

import org.hypher.gradientea.animation.shared.HsbColor;

/**
 * Color adjustment which simply sets the value of the color to the contained color.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ColorOverride extends BaseColorAdjustment {
	private HsbColor color;

	public ColorOverride() {}

	public ColorOverride(final HsbColor color) {
		this.color = color;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Interface Implementation
	@Override
	public HsbColor adjust(final HsbColor color) {
		return this.color;
	}
}
