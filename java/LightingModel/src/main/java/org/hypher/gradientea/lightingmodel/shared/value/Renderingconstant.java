package org.hypher.gradientea.lightingmodel.shared.value;

import org.hypher.gradientea.lightingmodel.shared.context.RenderingContext;
import org.hypher.gradientea.lightingmodel.shared.color.HsbColor;

import java.io.Serializable;

/**
 * A constant animation value.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class RenderingConstant<T extends Serializable> implements RenderingValue<T> {
	protected T value;

	protected RenderingConstant() {}

	public RenderingConstant(final T value) {
		this.value = value;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	@Override
	public T resolve(final RenderingContext context) {
		return value;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
	public static class Color extends RenderingConstant<HsbColor> {
		protected Color() {}

		public Color(final HsbColor value) {
			super(value);
		}
	}

	public static class FloatingScalar extends RenderingConstant<Double> {
		protected FloatingScalar() {}

		public FloatingScalar(final Double value) {
			super(value);
		}
	}

	public static class IntegerScalar extends RenderingConstant<Integer> {
		protected IntegerScalar() {}

		public IntegerScalar(final Integer value) {
			super(value);
		}
	}
}
