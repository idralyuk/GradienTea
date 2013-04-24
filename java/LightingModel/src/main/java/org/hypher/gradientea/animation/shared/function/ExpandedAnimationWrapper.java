package org.hypher.gradientea.animation.shared.function;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.hypher.gradientea.animation.shared.pixel.PixelGroup;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;

import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ExpandedAnimationWrapper extends AnimationFunctionWrapper {
	protected ExpandedAnimationWrapper() {}

	protected double compression = 0.05;

	public ExpandedAnimationWrapper(
		final PixelGroupAnimation wrappedAnimation,
		final Function<Double, Double> timeFunction,
		final double compression
	) {
		this.wrappedAnimation = wrappedAnimation;
		this.function = timeFunction;
		this.compression = compression;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> render(
		final PixelGroup outerGroup,
		final double time
	) {
		final ImmutableList.Builder<PixelValue> builder = ImmutableList.builder();
		final List<PixelGroup> innerGroups = outerGroup.getChildren();

		for (int i=0; i<innerGroups.size(); i++) {
			double wrappedFractionPosition = (((double) i / innerGroups.size()) * compression + time) % 1.0;

//			System.out.printf("%03d ", Math.round(function.apply(wrappedFractionPosition)*100));
//			for (int j=0; j<function.apply(wrappedFractionPosition)*30; j++) {
//				System.out.print("=");
//			}
//			System.out.println();

			builder.addAll(
				wrappedAnimation.render(innerGroups.get(i), function.apply(wrappedFractionPosition))
			);
		}
//		System.out.println();

		return builder.build();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "ExpandedAnimationWrapper{" +
			"wrappedAnimation=" + wrappedAnimation +
			'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public PixelGroupAnimation getWrappedAnimation() {
		return wrappedAnimation;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
