package org.hypher.gradientea.animation.shared.color;

import org.hypher.gradientea.animation.shared.pixel.Pixel;

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

	boolean isBlack();
}
