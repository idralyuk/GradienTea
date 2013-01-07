package org.hypher.gradientea.lightingmodel.shared.animation;

import com.google.common.base.Function;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;

import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AnimationFunctionWrapper implements PixelGroupAnimation {
	protected PixelGroupAnimation wrappedAnimation;
	protected Function<Double, Double> function;

	protected AnimationFunctionWrapper() {}

	public AnimationFunctionWrapper(
		final PixelGroupAnimation wrappedAnimation,
		final Function<Double, Double> timeFunction
	) {
		this.wrappedAnimation = wrappedAnimation;
		this.function = timeFunction;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> render(
		final PixelGroup group,
		final double fraction
	) {
		return wrappedAnimation.render(group, function.apply(fraction));
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

	public PixelGroupAnimation getWrappedAnimation() {
		return wrappedAnimation;
	}

	public Function<Double, Double> getFunction() {
		return function;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	public final static Function<Double, Double> SIN = new Function<Double, Double>() {
		@Nullable
		@Override
		public Double apply(final Double input) {
			return (Math.sin(input * Math.PI*2 ) + 1)/2;
		}
	};

	public final static Function<Double, Double> TRIANGLE = new Function<Double, Double>() {
		@Nullable
		@Override
		public Double apply(final Double input) {
			if (input < 0.5) {
				return input / 0.5;
			} else {
				return (1.0 - input) / 0.5;
			}
		}
	};
}
