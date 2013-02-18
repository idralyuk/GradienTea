package org.hypher.gradientea.lightingmodel.shared.compositing;

import org.hypher.gradientea.lightingmodel.shared.color.PixelColor;

import java.util.Collection;

/**
 * Interface for a pixel compositor.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface PixelCompositor {
	int[] compositeToRgb(Collection<PixelColor> colors);
}
