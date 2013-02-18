package org.hypher.gradientea.lightingmodel.shared.value;

import org.hypher.gradientea.lightingmodel.shared.context.RenderingContext;
import org.hypher.gradientea.lightingmodel.shared.color.HsbColor;

import java.io.Serializable;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class RenderingVariable<T extends Serializable> implements RenderingValue<T> {
	protected String name;

	protected RenderingVariable() {}

	public RenderingVariable(final String name) {
		this.name = name;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public String getName() {
		return name;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	public static class Color extends RenderingVariable<HsbColor> {
		protected Color() {}

		public Color(final String name) {
			super(name);
		}

		@Override
		public HsbColor resolve(final RenderingContext context) {
			return context.colorVariable(name).get();
		}
	}

	public static class FloatingScalar extends RenderingVariable<Double> {
		protected FloatingScalar() {}

		public FloatingScalar(final String name) {
			super(name);
		}

		@Override
		public Double resolve(final RenderingContext context) {
			return context.doubleVariable(name).get();
		}
	}

	public static class IntegerScalar extends RenderingVariable<Integer> {
		protected IntegerScalar() {}

		public IntegerScalar(final String name) {
			super(name);
		}

		@Override
		public Integer resolve(final RenderingContext context) {
			return context.intVariable(name).get();
		}
	}
}
