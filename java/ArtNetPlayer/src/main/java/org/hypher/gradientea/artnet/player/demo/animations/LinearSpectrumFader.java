package org.hypher.gradientea.artnet.player.demo.animations;

import org.hypher.gradientea.artnet.player.demo.GlobalAudioReader;
import org.hypher.gradientea.artnet.player.demo.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.demo.animations.canvas.PixelCompositor;
import org.hypher.gradientea.artnet.player.demo.io.BasicAudioReader;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class LinearSpectrumFader extends BaseAnimation {
	public LinearSpectrumFader(AnimationContext context) {
		super(context, PixelCompositor.REPLACE);

		freqBuffer = new float[context.getPixelCount()];
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation

	float[] freqBuffer;

	@Override
	protected void draw(final PixelCanvas canvas, double fraction) {
		BasicAudioReader.compressWithRms(GlobalAudioReader.getReader().getBuffer(0).getFreqDomainData(), freqBuffer);

		for (int i=0; i<freqBuffer.length; i++) {
			canvas.pixel(i, pixelFraction(i), 1.0, freqBuffer[i]);
		}
	}
	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Inner Classes

	enum Params implements ParameterId {
	}
	//endregion
}
