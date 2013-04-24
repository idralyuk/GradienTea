package org.hypher.gradientea.animation.shared.function;

import org.hypher.gradientea.animation.shared.pixel.PixelValue;

import java.util.List;

/**
 * An animation which is already associated with pixels and can be rendered with only a fractional time value.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface DefinedAnimation {
	/**
	 * Renders the animation at the given time fraction.
	 *
	 * @param fraction
	 * @return
	 */
	List<PixelValue> render(double fraction);
}
