package org.hypher.gradientea.artnet.player.demo.animations;

import org.hypher.gradientea.artnet.player.demo.OmniColor;
import org.hypher.gradientea.artnet.player.demo.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.demo.animations.canvas.PixelCompositor;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class OmniRainbowAnimation extends BaseAnimation {
	public OmniRainbowAnimation(AnimationContext context) {
		super(context, PixelCompositor.REPLACE);
		addParameter(Params.Divisions, 0, 8);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation

	@Override
	protected void draw(final PixelCanvas canvas, double fraction) {
		canvas.clear();

		int divisions = getParamValue(Params.Divisions);

		if (divisions == 0) {
			for (int i=0; i<pixelCount(); i++) {
				canvas.pixel(i, OmniColor.instance.mapHue(fraction), 1.0, 1.0);
			}
		} else {
			for (int i=0; i<pixelCount(); i++) {
				canvas.pixel(
					i,
					OmniColor.instance.mapHue(rotate(compress((double) i / pixelCount(), divisions), fraction)),
					1.0,
					1.0
				);
			}
		}
	}
	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Inner Classes

	enum Params implements ParameterId {
		Divisions
	}
	//endregion
}
