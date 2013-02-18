package org.hypher.gradientea.lightingmodel.shared.context;

import com.google.common.base.Optional;
import org.hypher.gradientea.lightingmodel.shared.color.HsbColor;

/**
 * Defines the context for rendering an animation.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface RenderingContext {
	Optional<HsbColor> colorVariable(String name);
	Optional<Double> doubleVariable(String name);
	Optional<Integer> intVariable(String name);

	/**
	 * Creates a new {@link RenderingContext} with the values of the given context, but which defaults to this
	 * one for any data not present in the provided context.
	 *
	 * @param renderingContext The context with which to extend this context
	 * @return
	 */
	RenderingContext extend(RenderingContext renderingContext);
}
