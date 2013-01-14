package org.hypher.gradientea.lightingmodel.shared.color;

import org.hypher.gradientea.lightingmodel.shared.pixel.Pixel;

/**
 * A color for a {@link Pixel}.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface PixelColor {
	/**
	 * Gives the composition priority for this color.
	 *
	 * @return
	 */
	double getPriority();

	int[] asRgb();
}
