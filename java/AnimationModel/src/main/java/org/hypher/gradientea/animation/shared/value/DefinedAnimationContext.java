package org.hypher.gradientea.animation.shared.value;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.hypher.gradientea.animation.shared.ids.VariableName;

import java.util.Map;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DefinedAnimationContext implements AnimationContext {
	private Optional<AnimationContext> parentContext;
	private Map<VariableName, StoredVariable<?>> variableMap;

	public DefinedAnimationContext(
		final Optional<AnimationContext> parentContext,
		final Map<VariableName, StoredVariable<?>> variableMap
	) {
		this.parentContext = parentContext;
		this.variableMap = ImmutableMap.copyOf(variableMap);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Methods
	@Override
	public <V> V resolveVariable(
		final VariableName name,
		final Class<? extends AnimationValue<V>> expectedType
	) {
		@SuppressWarnings("unchecked")
		Optional<StoredVariable<V>> storedVar = Optional.fromNullable ((StoredVariable<V>) variableMap.get(name));

		if (storedVar.isPresent()) {
			if (storedVar.get().getType().equals(expectedType)) {
				return storedVar.get().getValue().resolve(this);
			} else {
				throw new IllegalStateException("Could not resolve variable " + name + " of type " + expectedType + " because the variable is defined as a " + storedVar.get().getType() + " in this context.");
			}
		} else {
			if (parentContext.isPresent()) {
				return parentContext.get().resolveVariable(name, expectedType);
			} else {
				throw new IllegalArgumentException("Could not resolve variable " + name + " of type " + expectedType + " because it is not present in this context and there is no parent context");
			}
		}
	}
	//endregion


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters
	public Optional<AnimationContext> getParentContext() {
		return parentContext;
	}

	public Map<VariableName, StoredVariable<?>> getVariableMap() {
		return variableMap;
	}
	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Inner Classes

	public static class StoredVariable<T> {
		private Class<? extends AnimationValue<T>> type;
		private AnimationValue<T> value;

		public StoredVariable(final Class<? extends AnimationValue<T>> type, final AnimationValue<T> value) {
			this.type = type;
			this.value = value;
		}

		public Class<? extends AnimationValue<T>> getType() {
			return type;
		}

		public AnimationValue<T> getValue() {
			return value;
		}
	}

	//endregion
}
