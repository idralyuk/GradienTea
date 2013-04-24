package org.hypher.gradientea.artnet.player.animations;

import org.hypher.gradientea.artnet.player.ArtNetAnimationPlayer;
import org.hypher.gradientea.artnet.player.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.animations.canvas.PixelCompositor;
import org.hypher.gradientea.artnet.player.io.RelativeAudioLevelReader;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RelativeVuMeter extends BaseAnimation {
	RelativeAudioLevelReader audioReader = new RelativeAudioLevelReader(
		2.0f,
		0.01f
	);

	public RelativeVuMeter(AnimationContext context) {
		super(context, PixelCompositor.REPLACE);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation


	@Override
	public void play(final ArtNetAnimationPlayer player) {
		audioReader.start();

		super.play(player);
	}

	@Override
	public void stop() {
		audioReader.stop();
		super.stop();
	}

	@Override
	protected void draw(final PixelCanvas canvas, double fraction) {
		canvas.clear();

		double level = Math.max(audioReader.getRelativeLevel()-0.1, 0);

		for (int i=0; i<pixelCount(); i++) {
			canvas.pixel(i, rotate(pixelFraction(i), fraction), 1.0, level);
		}
	}
	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Inner Classes

	enum Params implements ParameterId {
	}
	//endregion
}
