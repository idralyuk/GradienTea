package org.hypher.gradientea.animation.shared;

import java.io.Serializable;

/**
 * An immutable color in the HSB color model.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class HsbColor implements Serializable {
	private double hue;
	private double saturation;
	private double brightness;

	protected HsbColor() { /* For Serialization */ }

	public HsbColor(final double hue, final double saturation, final double brightness) {
		this.hue = hue;
		this.saturation = saturation;
		this.brightness = brightness;
	}

	public double getHue() {
		return hue;
	}

	public double getSaturation() {
		return saturation;
	}

	public double getBrightness() {
		return brightness;
	}

}
