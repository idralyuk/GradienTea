package org.hypher.gradientea.lightingmodel.shared.value;

import org.hypher.gradientea.lightingmodel.shared.context.RenderingContext;
import org.hypher.gradientea.lightingmodel.shared.color.HsbColor;

import java.io.Serializable;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface RenderingValue<T extends Serializable> extends Serializable{
	T resolve(RenderingContext context);

	interface Color extends RenderingValue<HsbColor> {}
	interface FloatingScalar extends RenderingValue<Double> {}
	interface IntegerScalar extends RenderingValue<Integer> {}
}
