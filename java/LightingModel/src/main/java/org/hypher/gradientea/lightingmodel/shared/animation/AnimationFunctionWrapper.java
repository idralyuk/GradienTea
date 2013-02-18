package org.hypher.gradientea.lightingmodel.shared.animation;

import org.hypher.gradientea.lightingmodel.shared.context.RenderingContext;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AnimationFunctionWrapper implements Animation {
	protected Animation wrappedAnimation;
	protected AnimationFunction function;

	protected AnimationFunctionWrapper() {}

	public AnimationFunctionWrapper(
		final Animation wrappedAnimation,
		final AnimationFunction timeFunction
	) {
		this.wrappedAnimation = wrappedAnimation;
		this.function = timeFunction;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> render(
		final RenderingContext renderingContext,
		final PixelGroup group,
		final double fraction
	) {
		return wrappedAnimation.render(renderingContext, group, function.apply(renderingContext, fraction));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "AnimationFunctionWrapper{" +
			"wrappedAnimation=" + wrappedAnimation +
			", function=" + function +
			'}';
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public Animation getWrappedAnimation() {
		return wrappedAnimation;
	}

	public AnimationFunction getFunction() {
		return function;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	public interface AnimationFunction extends Serializable {
		double apply(RenderingContext renderingContext, double input);
	}

	public final static AnimationFunction SIN = new AnimationFunction() {
		@Override
		public double apply(RenderingContext renderingContext, final double input) {
			return (Math.sin(input * Math.PI*2 ) + 1)/2;
		}
	};

	public final static AnimationFunction TRIANGLE = new AnimationFunction() {
		@Override
		public double apply(RenderingContext renderingContext, final double input) {
			if (input < 0.5) {
				return input / 0.5;
			} else {
				return (1.0 - input) / 0.5;
			}
		}
	};
}
