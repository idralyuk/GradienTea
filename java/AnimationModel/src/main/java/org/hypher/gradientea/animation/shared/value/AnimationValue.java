package org.hypher.gradientea.animation.shared.value;

import org.hypher.gradientea.animation.shared.HsbColor;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface AnimationValue<T> {
	T resolve(AnimationContext context);

	interface ColorValue extends AnimationValue<HsbColor> {}
	interface DoubleValue extends AnimationValue<Double> {}
	interface IntegerValue extends AnimationValue<Integer> {}
}
