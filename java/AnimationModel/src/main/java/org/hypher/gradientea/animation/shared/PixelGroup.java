package org.hypher.gradientea.animation.shared;

import com.google.common.base.Function;

import java.util.Set;

/**
 * Something which can have a color adjustment applied to it.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface PixelGroup extends AnimationObject  {
	Set<? extends Pixel> getPixels();

	Function<PixelGroup, Set<Pixel>> getPixels = new Function<PixelGroup, Set<Pixel>>() {
		@Override
		public Set<Pixel> apply(final PixelGroup input) {
			return input.getPixels();
		}
	};
}
