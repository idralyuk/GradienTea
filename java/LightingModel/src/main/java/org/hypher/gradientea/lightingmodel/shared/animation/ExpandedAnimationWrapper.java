package org.hypher.gradientea.lightingmodel.shared.animation;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;

import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ExpandedAnimationWrapper extends AnimationFunctionWrapper {
	protected ExpandedAnimationWrapper() {}

	public ExpandedAnimationWrapper(
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
		final PixelGroup outerGroup,
		final double time
	) {
		final ImmutableList.Builder<PixelValue> builder = ImmutableList.builder();
		final List<PixelGroup> innerGroups = outerGroup.getChildren();

		for (int i=0; i<innerGroups.size(); i++) {
			double wrappedFractionPosition = ((double) i / innerGroups.size() + time) % 1.0;

			builder.addAll(
				wrappedAnimation.render(innerGroups.get(i), function.apply(wrappedFractionPosition))
			);
		}

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
