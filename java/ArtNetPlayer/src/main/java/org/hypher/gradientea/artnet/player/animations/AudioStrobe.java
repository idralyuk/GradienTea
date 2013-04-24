package org.hypher.gradientea.artnet.player.animations;

import org.hypher.gradientea.artnet.player.ArtNetAnimationPlayer;
import org.hypher.gradientea.artnet.player.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.animations.canvas.PixelCompositor;
import org.hypher.gradientea.artnet.player.io.RelativeAudioLevelReader;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AudioStrobe extends BaseAnimation {
	RelativeAudioLevelReader audioReader = new RelativeAudioLevelReader(
		.2f,
		0.01f
	);

	public AudioStrobe(AnimationContext context) {
		super(context, PixelCompositor.REPLACE);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation


	@Override
	public void stop() {
		super.stop();

		audioReader.stop();
	}

	@Override
	public void play(final ArtNetAnimationPlayer player) {
		audioReader.start();

		super.play(player);
	}

	@Override
	protected void draw(final PixelCanvas canvas, double fraction) {
		canvas.clear();

		if (audioReader.getRelativeLevel() > 0.5) {
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
