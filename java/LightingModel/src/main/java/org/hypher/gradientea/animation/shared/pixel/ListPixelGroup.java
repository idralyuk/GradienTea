package org.hypher.gradientea.animation.shared.pixel;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.hypher.gradientea.animation.shared.color.PixelColor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * A simple implementation of a {@link PixelGroup} which consists of a pre-defined list of other pixel groups. Colors
 * are applied without any modification to all children.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ListPixelGroup implements PixelGroup {
	protected ImmutableList<PixelGroup> children;

	protected ListPixelGroup() {}

	public ListPixelGroup(final Collection<? extends PixelGroup> children) {
		this.children = ImmutableList.copyOf(children);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> applyColor(final PixelColor color) {
		return FluentIterable.from(children)
			.transformAndConcat(new Function<PixelGroup, Collection<PixelValue>>() {
				@Override
				public Collection<PixelValue> apply(@Nullable final PixelGroup input) {
					return input.applyColor(color);
				}
			}).toImmutableList();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public ImmutableList<PixelGroup> getChildren() {
		return children;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
