package org.hypher.gradientea.artnet.player.animations;

import com.google.common.collect.Maps;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.animation.shared.color.PixelColor;
import org.hypher.gradientea.animation.shared.color.RgbColor;
import org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.transport.shared.DomeAnimationFrame;

import java.util.Map;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomePixelCanvas {
	private PixelCompositor compositor;
	private GradienTeaDomeGeometry geometry;
	private Map<GeoFace, PixelColor> faceColorMap = Maps.newHashMap();

	public DomePixelCanvas(final PixelCompositor compositor, final GradienTeaDomeGeometry geometry) {
		this.compositor = compositor;
		this.geometry = geometry;
	}

	public void scaleBrightness(double percentage) {
		for (Map.Entry<GeoFace, PixelColor> entry : faceColorMap.entrySet()) {
			if (entry.getValue() instanceof RgbColor) {
				RgbColor value = (RgbColor) entry.getValue();
				entry.setValue(new RgbColor(
					value.getRed() * percentage,
					value.getGreen() * percentage,
					value.getBlue() * percentage
				));
			} else {
				HsbColor value = (HsbColor) entry.getValue();
				entry.setValue(new HsbColor(
					value.getHue(),
					value.getSaturation(),
					value.getBrightness() * percentage
				));
			}
		}
	}

	public void draw(GeoFace face, PixelColor color) {
		if (faceColorMap.containsKey(face)) {
			faceColorMap.put(
				face,
				compositor.composite(
					faceColorMap.get(face),
					color
				)
			);
		} else {
			faceColorMap.put(face, color);
		}
	}

	public void draw(GeoFace face, double hue, double saturation, double brightness) {
		draw(face, new HsbColor(hue, saturation, brightness));
	}

	public void draw(Iterable<GeoFace> faces, double hue, double saturation, double brightness) {
		for (GeoFace face : faces) {
			draw(face, hue, saturation, brightness);
		}
	}

	public void draw(Iterable<GeoFace> faces, PixelColor color) {
		for (GeoFace face : faces) {
			draw(face, color);
		}
	}

	public void clear() {
		faceColorMap.clear();
	}

	public DomeAnimationFrame render() {
		byte[] data = new byte[geometry.getLightedFaces().size()*3];

		int dataIndex = 0;
		for (GeoFace face : geometry.getLightedFaces()) {
			if (faceColorMap.containsKey(face)) {
				int[] rgb = faceColorMap.get(face).asRgb();

				data[dataIndex+0] = (byte) rgb[0];
				data[dataIndex+1] = (byte) rgb[1];
				data[dataIndex+2] = (byte) rgb[2];
			}

			dataIndex += 3;
		}

		return new DomeAnimationFrame(data);
	}

}
