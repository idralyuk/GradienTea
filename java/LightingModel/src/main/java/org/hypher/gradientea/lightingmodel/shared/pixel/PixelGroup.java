package org.hypher.gradientea.lightingmodel.shared.pixel;

import org.hypher.gradientea.lightingmodel.shared.color.PixelColor;

import java.util.List;

/**
 * A group of pixels or other groups containing pixels. A node in a tree of pixel groups.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface PixelGroup {
	/**
	 * Applies the given {@link PixelColor} to all the pixels in this group, returning all the {@link PixelValue}s
	 * that result from the application.
	 *
	 * @param color The color to apply
	 * @return The resulting pixel values
	 */
	List<PixelValue> applyColor(PixelColor color);

	/**
	 * @return The children of this pixel group
	 */
	List<PixelGroup> getChildren();
}
