package org.hypher.gradientea.artnet.player.animations.canvas;

import org.hypher.gradientea.animation.shared.color.PixelColor;
import org.hypher.gradientea.animation.shared.color.RgbColor;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface PixelCompositor {
	PixelColor composite(PixelColor a, PixelColor b);

	PixelCompositor ADDITIVE = new PixelCompositor() {
		@Override
		public PixelColor composite(final PixelColor a, final PixelColor b) {
			int[] aRgb = a.asRgb();
			int[] bRgb = b.asRgb();

			return new RgbColor(
				aRgb[0] + bRgb[0],
				aRgb[1] + bRgb[1],
				aRgb[2] + bRgb[2]
			);
		}
	};

	PixelCompositor REPLACE = new PixelCompositor() {
		@Override
		public PixelColor composite(final PixelColor a, final PixelColor b) {
			return b;
		}
	};
}
