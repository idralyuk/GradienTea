package org.hypher.gradientea.lightingmodel.shared.color;

import org.hypher.gradientea.lightingmodel.shared.pixel.Pixel;

import java.io.Serializable;

/**
 * A color for a {@link Pixel}.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface PixelColor extends Serializable {
	/**
	 * Gives the composition priority for this color.
	 *
	 * @return
	 */
	double getPriority();

	Double[] asHSB();
	Double[] asRGB();
}
