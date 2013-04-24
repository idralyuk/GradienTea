package org.hypher.gradientea.animation.shared.function;

import org.hypher.gradientea.animation.shared.color.RgbColor;
import org.hypher.gradientea.animation.shared.pixel.PixelGroup;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;

import java.util.List;

/**
 * A simple linear tween between two RGB colors
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RgbTween implements PixelGroupAnimation {
	protected RgbColor color1;
	protected RgbColor color2;

	protected RgbTween() {}

	public RgbTween(final RgbColor color1, final RgbColor color2) {
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
			new RgbColor(
				(int) (color1.getRed() + (color2.getRed() - color1.getRed()) * fraction),
				(int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction),
				(int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction)
			)
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "RgbTween{" +
			"color1=" + color1 +
			", color2=" + color2 +
			'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public RgbColor getColor1() {
		return color1;
	}

	public RgbColor getColor2() {
		return color2;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
