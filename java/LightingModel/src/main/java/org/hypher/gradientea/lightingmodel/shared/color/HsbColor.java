package org.hypher.gradientea.lightingmodel.shared.color;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class HsbColor implements PixelColor {
	private double hue;
	private double saturation;
	private double brightness;

	protected HsbColor() {}

	public HsbColor(final double hue, final double saturation, final double brightness) {
		this.hue = hue;
		this.saturation = saturation;
		this.brightness = brightness;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	/**
	 * A slightly modified version of {@link java.awt.Color#HSBtoRGB(float, float, float)}
	 *
	 * @return
	 */
	@Override
	public int[] asRgb() {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			double h = (hue - (double)Math.floor(hue)) * 6.0f;
			double f = h - (double)java.lang.Math.floor(h);
			double p = brightness * (1.0f - saturation);
			double q = brightness * (1.0f - saturation * f);
			double t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
				case 0:
					r = (int) (brightness * 255.0f + 0.5f);
					g = (int) (t * 255.0f + 0.5f);
					b = (int) (p * 255.0f + 0.5f);
					break;
				case 1:
					r = (int) (q * 255.0f + 0.5f);
					g = (int) (brightness * 255.0f + 0.5f);
					b = (int) (p * 255.0f + 0.5f);
					break;
				case 2:
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (brightness * 255.0f + 0.5f);
					b = (int) (t * 255.0f + 0.5f);
					break;
				case 3:
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (q * 255.0f + 0.5f);
					b = (int) (brightness * 255.0f + 0.5f);
					break;
				case 4:
					r = (int) (t * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (brightness * 255.0f + 0.5f);
					break;
				case 5:
					r = (int) (brightness * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (q * 255.0f + 0.5f);
					break;
			}
		}
		return new int[] {r, g, b};
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public double getHue() {
		return hue;
	}

	public double getSaturation() {
		return saturation;
	}

	public double getBrightness() {
		return brightness;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
