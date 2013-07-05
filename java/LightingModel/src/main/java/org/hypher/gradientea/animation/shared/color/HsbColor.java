package org.hypher.gradientea.animation.shared.color;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class HsbColor implements PixelColor {
	private final static double lowestNonBlackValue = (1.0 / 255) / 2;

	private double hue;
	private double saturation;
	private double brightness;

	private double hueOpacity;
	private double saturationOpacity;
	private double brightnessOpacity;

	protected HsbColor() {}

	public HsbColor(final double hue, final double saturation, final double brightness) {
		this.hue = hue;
		this.saturation = Math.max(0, Math.min(1, saturation));
		this.brightness = Math.max(0, Math.min(1, brightness));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	/**
	 * A slightly modified version of {@link java.awt.Color#HSBtoRGB(float, float, float)}
	 *
	 * @return
	 */
	public int[] asRgb() {
		return HSBtoRGB((float) hue, (float) saturation, (float) brightness);
	}

	public static int[] HSBtoRGB(float hue, float saturation, float brightness) {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float)Math.floor(hue)) * 6.0f;
			float f = h - (float)java.lang.Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
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

	public static float[] RGBtoHSB(int r, int g, int b) {
		float hue, saturation, brightness;
		float[] hsbvals = new float[3];

		int cmax = (r > g) ? r : g;
		if (b > cmax) cmax = b;
		int cmin = (r < g) ? r : g;
		if (b < cmin) cmin = b;

		brightness = ((float) cmax) / 255.0f;
		if (cmax != 0)
			saturation = ((float) (cmax - cmin)) / ((float) cmax);
		else
			saturation = 0;
		if (saturation == 0)
			hue = 0;
		else {
			float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
			float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
			float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
			if (r == cmax)
				hue = bluec - greenc;
			else if (g == cmax)
				hue = 2.0f + redc - bluec;
			else
				hue = 4.0f + greenc - redc;
			hue = hue / 6.0f;
			if (hue < 0)
				hue = hue + 1.0f;
		}
		hsbvals[0] = hue;
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}

	public HsbColor multiply(double hueFactor, double saturationFactor, double brightnessFactor) {
		return new HsbColor(
			this.hue * hueFactor,
			this.saturation * saturationFactor,
			this.brightness * brightnessFactor
		);
	}

	public HsbColor add(double hueAddition, double saturationAddition, double brightnessAddition) {
		return new HsbColor(
			this.hue + hueAddition,
			this.saturation + saturationAddition,
			this.brightness + brightnessAddition
		);
	}

	@Override
	public boolean isBlack() {
		return brightness < lowestNonBlackValue;
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

	@Override
	public double getPriority() {
		return 1.0;
	}

	public static HsbColor hsbColor(final PixelColor color) {
		if (color instanceof HsbColor) {
			return (HsbColor) color;
		}
		else {
			int[] rgb = color.asRgb();
			final float[] hsb = RGBtoHSB(rgb[0], rgb[1], rgb[2]);
			return new HsbColor(hsb[0], hsb[1], hsb[2]);
		}
	}

	public HsbColor withBrightness(final double newBrightness) {
		if (newBrightness == brightness) {
			return this;
		} else {
			return new HsbColor(
				hue, saturation, brightness
			);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
