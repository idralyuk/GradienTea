package org.hypher.gradientea.artnet.player.linear.animations;

import org.hypher.gradientea.artnet.player.io.GlobalAudioReader;
import org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor;
import org.hypher.gradientea.artnet.player.io.BasicAudioReader;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class FlashyAudioStrobe extends BaseAnimation {
	public FlashyAudioStrobe(AnimationContext context) {
		super(context, PixelCompositor.REPLACE);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation

	@Override
	protected void draw(final PixelCanvas canvas, double fraction) {
		canvas.clear();

		final BasicAudioReader reader = GlobalAudioReader.getReader();

		float v0 = reader.getBufferRMS(0);
		float v1 = reader.getBufferRMS(1);
		float v2 = reader.getBufferRMS(2);

		if (v1 > v0 && v1 < v2) {
			canvas.fill(0, 0, 1.0);
		}
	}
	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Inner Classes

	enum Params implements ParameterId {
	}
	//endregion
}
