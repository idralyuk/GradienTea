package org.hypher.gradientea.animation.shared.color;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RgbColor implements PixelColor {
	private /*final*/ double red;
	private /*final*/ double green;
	private /*final*/ double blue;

	protected RgbColor() { }

	public RgbColor(final int red, final int green, final int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public RgbColor(final double red, final double green, final double blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods


	@Override
	public int[] asRgb() {
		return new int[] { (int)red, (int)green, (int)blue };
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public double getRed() {
		return red;
	}

	public double getGreen() {
		return green;
	}

	public double getBlue() {
		return blue;
	}

	@Override
	public double getPriority() {
		return 0;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
