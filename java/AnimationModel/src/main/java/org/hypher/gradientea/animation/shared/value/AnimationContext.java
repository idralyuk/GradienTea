package org.hypher.gradientea.animation.shared.value;

import org.hypher.gradientea.animation.shared.ids.VariableName;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface AnimationContext {
	<V> V resolveVariable(VariableName name, Class<? extends AnimationValue<V>> expectedTypeInterface);
}
