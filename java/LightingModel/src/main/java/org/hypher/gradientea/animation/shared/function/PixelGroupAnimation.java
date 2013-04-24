package org.hypher.gradientea.animation.shared.function;

import org.hypher.gradientea.animation.shared.pixel.PixelGroup;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;

import java.util.List;

/**
 * Interface for animations which can be applied to a {@link PixelGroup}.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface PixelGroupAnimation {
	/**
	 * Renders a frame of the animation onto the given pixel group at the given fraction value.
	 *
	 * @param group The {@link PixelGroup} to render the animation onto
	 * @param fraction What point in the animation should be rendered, must be between 0 and 1.
	 * @return The pixel values at the given time fraction
	 */
	List<PixelValue> render(PixelGroup group, double fraction);
}
