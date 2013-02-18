package org.hypher.gradientea.animation.shared.value;

import org.hypher.gradientea.animation.shared.HsbColor;
import org.hypher.gradientea.animation.shared.ids.VariableName;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class AnimationVariable<T> implements AnimationValue<T> {
	private VariableName name;
	private Class<? extends AnimationValue<T>> type;

	protected AnimationVariable() {}

	protected AnimationVariable(
		final VariableName name,
		final Class<? extends AnimationValue<T>> type
	) {
		this.name = name;
		this.type = type;
	}

	public VariableName getName() {
		return name;
	}

	public Class<? extends AnimationValue<T>> getType() {
		return type;
	}

	@Override
	public T resolve(final AnimationContext context) {
		return context.resolveVariable(name, type);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final AnimationVariable that = (AnimationVariable) o;

		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (type != null ? !type.equals(that.type) : that.type != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return ((name.hashCode()*31) + type.getName().hashCode())*31 + getClass().getName().hashCode();
	}

	public static class ColorVar extends AnimationVariable<HsbColor> implements ColorValue {
		protected ColorVar() {}

		public ColorVar(
			final VariableName name
		) {
			super(name, ColorValue.class);
		}
	}

	public static class IntegerVar extends AnimationVariable<Integer> implements IntegerValue {
		protected IntegerVar() {}

		public IntegerVar(
			final VariableName name
		) {
			super(name, IntegerValue.class);
		}
	}

	public static class DoubleVar extends AnimationVariable<Double> implements DoubleValue {
		protected DoubleVar() {}

		public DoubleVar(
			final VariableName name
		) {
			super(name, DoubleValue.class);
		}
	}
}
