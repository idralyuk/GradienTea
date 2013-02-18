package org.hypher.gradientea.animation.shared.value;

import com.google.common.base.Preconditions;
import org.hypher.gradientea.animation.shared.HsbColor;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class AnimationConstant<T> implements AnimationValue<T> {
	private T value;

	protected AnimationConstant() { }

	protected AnimationConstant(final T value) {
		Preconditions.checkNotNull(value, "Value must not be null");

		this.value = value;
	}

	public T getValue() {
		return value;
	}

	@Override
	public T resolve(final AnimationContext context) {
		return value;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final AnimationConstant that = (AnimationConstant) o;

		if (value != null ? !value.equals(that.value) : that.value != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return value.hashCode()*31 + getClass().getName().hashCode();
	}

	public static class ConstantColor extends AnimationConstant<HsbColor> implements ColorValue {
		protected ConstantColor() {}

		public ConstantColor(final HsbColor value) {
			super(value);
		}
	}

	public static class ConstantInteger extends AnimationConstant<Integer> implements IntegerValue {
		protected ConstantInteger() {}

		public ConstantInteger(final Integer value) {
			super(value);
		}
	}

	public static class ConstantDouble extends AnimationConstant<Double> implements DoubleValue {
		protected ConstantDouble() {}

		public ConstantDouble(final Double value) {
			super(value);
		}
	}
}
