package org.hypher.gradientea.animation.shared;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface PixelGroupContainer extends PixelGroup {
	public Collection<? extends PixelGroupContainer> getChildGroups();

	Function<PixelGroupContainer, Collection<? extends PixelGroupContainer>> getChildGroups = new Function<PixelGroupContainer, Collection<? extends PixelGroupContainer>>() {
		@Override
		public Collection<? extends PixelGroupContainer> apply(final PixelGroupContainer input) {
			return input.getChildGroups();
		}
	};
}
