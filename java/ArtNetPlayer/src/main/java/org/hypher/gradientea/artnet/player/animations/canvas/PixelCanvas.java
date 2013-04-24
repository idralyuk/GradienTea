package org.hypher.gradientea.artnet.player.animations.canvas;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.animation.shared.color.PixelColor;
import org.hypher.gradientea.animation.shared.color.RgbColor;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxPixel;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;

import java.util.List;
import java.util.Map;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class PixelCanvas {
	private static final PixelColor black = new RgbColor(0,0,0);

	private int pixelCount;
	private Map<Integer, PixelColor> colorMap = Maps.newHashMap();
	private PixelCompositor compositor;

	public PixelCanvas(final int pixelCount, final PixelCompositor compositor) {
		this.pixelCount = pixelCount;
		this.compositor = compositor;
	}

	public PixelCanvas pixel(int index, double hue, double saturation, double brightness) {
		return pixel(index, new HsbColor(hue, saturation, brightness));
	}

	private PixelCanvas pixel(int index, final PixelColor color) {
		return pixel(index, color, compositor);
	}

	private PixelCanvas pixel(int index, final PixelColor color, PixelCompositor compositor) {
		index = (pixelCount + index % pixelCount) % pixelCount;

		if (colorMap.containsKey(index)) {
			colorMap.put(index, compositor.composite(colorMap.get(index), color));
		} else {
			colorMap.put(index, color);
		}

		return this;
	}

	public PixelCanvas pixelRgb(int index, int red, int green, int blue) {
		return pixel(index, new RgbColor(red, green, blue));
	}

	public void clear() {
		colorMap.clear();
	}

	public List<PixelValue> render() {
		List<PixelValue> values = Lists.newArrayList();

		for (int i=0; i<pixelCount; i++) {
			if (colorMap.containsKey(i)) {
				values.add(new PixelValue(new DmxPixel(1, i * 3 + 1), colorMap.get(i)));
			} else {
				values.add(new PixelValue(new DmxPixel(1, i*3+1), black));
			}
		}

		return values;
	}

	public void fill(int start, int end, double hue, double saturation, double brightness) {
		for (int i=start; i<end; i++) {
			pixel(i, hue, saturation, brightness);
		}
	}

	public void fill(double hue, double saturation, double brightness) {
		fill(0, pixelCount, hue, saturation, brightness);
	}

	public void fill(int start, int end, final RgbColor rgbColor, final PixelCompositor additive) {
		for (int i=start; i<end; i++) {
			pixel(i, rgbColor, additive);
		}
	}
}
