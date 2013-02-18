package org.hypher.gradientea.lightingmodel.shared.color;

/**
 * A color (or partially defined color) in the HSB color space.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class HsbColor implements PixelColor {
	private Double hue;
	private Double saturation;
	private Double brightness;
	private double priority;

	protected HsbColor() {}

	public HsbColor(final Double hue, final Double saturation, final Double brightness, final double priority) {
		this.hue = hue;
		this.saturation = saturation;
		this.brightness = brightness;
		this.priority = priority;
	}

	public HsbColor(final Double hue, final Double saturation, final Double brightness) {
		this(hue, saturation, brightness, 1.0);
	}

	public static Double[] HSBtoRGB(double hue, double saturation, double brightness) {
		double r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0d + 0.5d);
		} else {
			double h = (hue - (double)Math.floor(hue)) * 6.0d;
			double f = h - (double)java.lang.Math.floor(h);
			double p = brightness * (1.0d - saturation);
			double q = brightness * (1.0d - saturation * f);
			double t = brightness * (1.0d - (saturation * (1.0d - f)));
			switch ((int) h) {
				case 0:
					r = (brightness + 0.5d);
					g = (t + 0.5d);
					b = (p + 0.5d);
					break;
				case 1:
					r = (q + 0.5d);
					g = (brightness + 0.5d);
					b = (p + 0.5d);
					break;
				case 2:
					r = (p * 0.5d);
					g = (brightness * 0.5d);
					b = (t * 0.5d);
					break;
				case 3:
					r = (p * 0.5d);
					g = (q * 0.5d);
					b = (brightness * 0.5d);
					break;
				case 4:
					r = (t * 0.5d);
					g = (p * 0.5d);
					b = (brightness * 0.5d);
					break;
				case 5:
					r = (brightness * 0.5d);
					g = (p * 0.5d);
					b = (q * 0.5d);
					break;
			}
		}
		return new Double[] {r, g, b};
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods
	@Override
	public Double[] asHSB() {
		return new Double[] { hue, saturation, brightness };
	}

	@Override
	public Double[] asRGB() {
		return HSBtoRGB(
			hue == null ? 0 : hue,
			saturation == null ? 0 : saturation,
			brightness == null ? 0 : brightness
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public Double getHue() {
		return hue;
	}

	public Double getSaturation() {
		return saturation;
	}

	public Double getBrightness() {
		return brightness;
	}

	@Override
	public double getPriority() {
		return priority;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
