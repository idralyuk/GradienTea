package org.hypher.gradientea.lightingmodel.shared.pixel;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.hypher.gradientea.lightingmodel.shared.AbstractAnimationElement;
import org.hypher.gradientea.lightingmodel.shared.context.AnimationElement;
import org.hypher.gradientea.lightingmodel.shared.color.PixelColor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class AbstractPixelGroup extends AbstractAnimationElement implements PixelGroup {
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> applyColor(final PixelColor color) {
		return FluentIterable.from(getChildren())
			.transformAndConcat(new Function<PixelGroup, Collection<PixelValue>>() {
				@Override
				public Collection<PixelValue> apply(@Nullable final PixelGroup input) {
					return input.applyColor(color);
				}
			}).toImmutableList();
	}

	@Override
	public Collection<? extends AnimationElement> getChildElements() {
		return getChildren();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
