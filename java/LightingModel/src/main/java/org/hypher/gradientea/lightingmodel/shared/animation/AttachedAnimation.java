package org.hypher.gradientea.lightingmodel.shared.animation;

import org.hypher.gradientea.lightingmodel.shared.context.RenderingContext;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;

import java.io.Serializable;
import java.util.List;

/**
 * An animation which is already associated with pixels and can be rendered with only a fractional time value.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface AttachedAnimation extends Serializable {
	/**
	 * Renders the animation at the given time fraction.
	 *
	 * @param fraction
	 * @return
	 */
	List<PixelValue> render(RenderingContext renderingContext, double fraction);
}
