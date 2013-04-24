package org.hypher.gradientea.lightingmodel.shared.dmx;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.hypher.gradientea.animation.shared.function.DefinedAnimation;
import org.hypher.gradientea.animation.shared.pixel.Pixel;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;

import java.util.List;

/**
 * Holds utility methods for rendering {@link org.hypher.gradientea.animation.shared.RenderableAnimation}s which are composed of {@link DmxPixel}s.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DmxRendering {
	protected DmxRendering() {}
	
	
	public static int[][] render(DefinedAnimation animation, double percentage) {
		return composite(animation.render(percentage));
	}

	public static int[][] composite(final List<PixelValue> pixelValues) {
		Multimap<Pixel, PixelValue> pixelValueMap = ArrayListMultimap.create();

		for (PixelValue pixelValue : pixelValues) {
			pixelValueMap.put(pixelValue.getPixel(), pixelValue);
		}

		int[][] universes = new int[4][512];

		for (Pixel pixel : pixelValueMap.keySet()) {
			if (pixel instanceof DmxPixel) {
				DmxPixel dmxPixel = (DmxPixel) pixel;

				int redSum = 0;
				int greenSum = 0;
				int blueSum = 0;
				int count = pixelValueMap.get(pixel).size();

				for (PixelValue value : pixelValueMap.get(pixel)) {
					int[] rgb = value.getColor().asRgb();

					redSum += rgb[0];
					greenSum += rgb[1];
					blueSum += rgb[2];
				}

				int[] universe = universes[dmxPixel.getUniverse() - 1];
				int startChannel = dmxPixel.getFirstChannel()-1;

				universe[startChannel+0] = (int) Math.round((double)redSum/count);
				universe[startChannel+1] = (int) Math.round((double)greenSum/count);
				universe[startChannel+2] = (int) Math.round((double)blueSum/count);
			}
		}

		return universes;
	}

}
