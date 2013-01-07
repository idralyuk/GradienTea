package org.hypher.gradientea.lightingmodel.shared.color;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RgbColor implements PixelColor {
	private /*final*/ int red;
	private /*final*/ int green;
	private /*final*/ int blue;

	protected RgbColor() { }

	public RgbColor(final int red, final int green, final int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public int[] asRgb() {
		return new int[] { red, green, blue };
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
