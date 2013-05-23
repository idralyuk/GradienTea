package org.hypher.gradientea.artnet.player.linear.animations;

import org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor;

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
//		BasicAudioReader.compressLogWithRms(
//			GlobalAudioReader.getReader().getBuffer(0).getFreqDomainData(),
//			freqBuffer
//		);
//
//		StringBuffer sb = new StringBuffer();
//		for (int i=0; i<freqBuffer.length; i++) {
//			sb.append((int)(freqBuffer[i] * 9)).append(" ");
//			canvas.pixel(i, pixelFraction(i), 1.0, Math.max(0, freqBuffer[i] - 0.1));
//		}
		//System.out.println(sb);
	}
	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Inner Classes

	enum Params implements ParameterId {
	}
	//endregion
}
