package org.hypher.gradientea.animation.shared.function;

import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.animation.shared.pixel.PixelGroup;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;

import java.util.List;

/**
 * A simple linear tween between two HSB colors
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class HsbTween implements PixelGroupAnimation {
	protected HsbColor color1;
	protected HsbColor color2;

	protected HsbTween() {}

	public HsbTween(final HsbColor color1, final HsbColor color2) {
		this.color1 = color1;
		this.color2 = color2;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> render(
		final PixelGroup group,
		final double fraction
	) {
		return group.applyColor(
			new HsbColor(
				color1.getHue() + (color2.getHue() - color1.getHue()) * fraction,
				color1.getSaturation() + (color2.getSaturation() - color1.getSaturation()) * fraction,
				color1.getBrightness() + (color2.getBrightness() - color1.getBrightness()) * fraction
			)
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "HsbTween{" +
			"color1=" + color1 +
			", color2=" + color2 +
			'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public HsbColor getColor1() {
		return color1;
	}

	public HsbColor getColor2() {
		return color2;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
