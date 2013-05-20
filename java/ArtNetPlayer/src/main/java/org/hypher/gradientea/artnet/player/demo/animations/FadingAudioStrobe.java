package org.hypher.gradientea.artnet.player.demo.animations;

import org.hypher.gradientea.artnet.player.demo.GlobalAudioReader;
import org.hypher.gradientea.artnet.player.demo.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.demo.animations.canvas.PixelCompositor;
import org.hypher.gradientea.artnet.player.demo.io.BasicAudioReader;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class FadingAudioStrobe extends BaseAnimation {
	public FadingAudioStrobe(AnimationContext context) {
		super(context, PixelCompositor.REPLACE);

		addParameter(Params.AutoRotate, 0, 1);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation

	float intensity = 0.0f;

	@Override
	protected void draw(final PixelCanvas canvas, double fraction) {
		BasicAudioReader.LevelAverage longestAverage = GlobalAudioReader.getReader().getRMSMean(5.0f);
		float longAverage = GlobalAudioReader.getReader().getRMSMean(0.5f).mean();
		float now = GlobalAudioReader.getReader().getRMSMean(1/60f).mean();

		if (now > longAverage) {
			intensity += 0.05f;
		} else {
			intensity -= 0.02f;
		}

		if (intensity < 0) intensity = 0;
		if (intensity > 1) intensity = 1;

		canvas.fill(
			(getParamValue(Params.AutoRotate) == 1) ? rotate(fraction, intensity) : intensity,
			1.0,
			longestAverage.scale(longAverage)
		);
	}
	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Inner Classes

	enum Params implements ParameterId {
		AutoRotate
	}
	//endregion
}
