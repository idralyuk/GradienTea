package org.hypher.gradientea.lightingmodel.shared.color;

/**
 * A color for a {@link org.hypher.gradientea.lightingmodel.shared.pixel.Pixel} which can be rendered into
 * the RGB color-space.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface PixelColor {
	int[] asRgb();
}
