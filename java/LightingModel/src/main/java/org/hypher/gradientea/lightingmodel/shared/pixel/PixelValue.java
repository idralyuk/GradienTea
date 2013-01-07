package org.hypher.gradientea.lightingmodel.shared.pixel;

import org.hypher.gradientea.lightingmodel.shared.color.PixelColor;

/**
 * Represents a colored pixel. These are the result of the output of animations, and can be applied to a pixel array.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class PixelValue {
	protected /*final*/ Pixel pixel;
	protected /*final*/ PixelColor color;

	public PixelValue() {}

	public PixelValue(final Pixel pixel, final PixelColor color) {
		this.pixel = pixel;
		this.color = color;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final PixelValue that = (PixelValue) o;

		if (color != null ? !color.equals(that.color) : that.color != null) return false;
		if (pixel != null ? !pixel.equals(that.pixel) : that.pixel != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = pixel != null ? pixel.hashCode() : 0;
		result = 31 * result + (color != null ? color.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "PixelValue{" +
			"pixel=" + pixel +
			", color=" + color +
			'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public Pixel getPixel() {
		return pixel;
	}

	public PixelColor getColor() {
		return color;
	}
}
