package org.hypher.gradientea.lightingmodel.shared.context;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.hypher.gradientea.lightingmodel.shared.color.HsbColor;

import java.util.Map;

/**
 * A rendering context which defines variable values.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DefinedRenderingContext implements RenderingContext {
	protected Map<String, HsbColor> colorVariables = Maps.newHashMap();
	protected Map<String, Double> doubleVariables = Maps.newHashMap();
	protected Map<String, Integer> integerVariables = Maps.newHashMap();

	protected DefinedRenderingContext() {}

	public DefinedRenderingContext(
		final Map<? extends String, ? extends HsbColor> colorVariables,
		final Map<? extends String, ? extends Double> doubleVariables,
		final Map<? extends String, ? extends Integer> integerVariables
	) {
		this.colorVariables.putAll(colorVariables);
		this.doubleVariables.putAll(doubleVariables);
		this.integerVariables.putAll(integerVariables);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public Optional<HsbColor> colorVariable(final String name) {
		return Optional.fromNullable(colorVariables.get(name));
	}

	@Override
	public Optional<Double> doubleVariable(final String name) {
		return Optional.fromNullable(doubleVariables.get(name));
	}

	@Override
	public Optional<Integer> intVariable(final String name) {
		return Optional.fromNullable(integerVariables.get(name));
	}

	@Override
	public RenderingContext extend(final RenderingContext childContext) {
		return new ExtendedRenderingContext(this, childContext);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	public static class ExtendedRenderingContext implements RenderingContext {
		private final RenderingContext parentContext;
		private final RenderingContext childContext;

		public ExtendedRenderingContext(
			final RenderingContext childContext,
			final RenderingContext parentContext
		) {
			this.childContext = childContext;
			this.parentContext = parentContext;
		}

		@Override
		public Optional<HsbColor> colorVariable(final String name) {
			return childContext.colorVariable(name).or(parentContext.colorVariable(name));
		}

		@Override
		public Optional<Double> doubleVariable(final String name) {
			return childContext.doubleVariable(name).or(parentContext.doubleVariable(name));
		}

		@Override
		public Optional<Integer> intVariable(final String name) {
			return childContext.intVariable(name).or(parentContext.intVariable(name));
		}

		@Override
		public RenderingContext extend(final RenderingContext renderingContext) {
			return null;
		}
	}
}
