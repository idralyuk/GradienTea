package org.hypher.gradientea.artnet.player.animations;

import org.hypher.gradientea.artnet.player.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.animations.canvas.PixelCompositor;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RandomBlipsAnimation extends BaseAnimation {
	public RandomBlipsAnimation(AnimationContext context) {
		super(context, PixelCompositor.REPLACE);
		addParameter(Params.Speed, 12, 24);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation
	@Override
	protected void onParameterChanged(
		final ParameterId id, final int newValue
	) {
		super.onParameterChanged(id, newValue);


	}

	double movingDuration = 10000;

	double duration;
	double direction = 1;
	double width = 0;
	double location = 0;
	double timeLeft = 0;
	long lastFrame = System.currentTimeMillis();
	protected void draw(final PixelCanvas canvas, double fraction) {
		canvas.clear();

		long sinceLast = System.currentTimeMillis() - lastFrame;
		lastFrame = System.currentTimeMillis();

		timeLeft -= sinceLast;

		if (timeLeft <= 0) {
			duration = animationDuration * (Math.random() + 0.2);
			timeLeft = duration;
			location = Math.random();
			width = 0.05 + Math.random() * 0.2;
			direction = Math.random() < 0.5 ? -1 : 1;
		}

		double movingFraction = (System.currentTimeMillis() % movingDuration) / movingDuration;
		double remainingDurationFraction = Math.max(0, Math.sin((timeLeft / duration) * Math.PI * 1.2));

		int start = (int) (location * pixelCount() - ((width * remainingDurationFraction)/2) * pixelCount());
		int end = (int) (location * pixelCount() + ((width * remainingDurationFraction)/2) * pixelCount());

		for (int i = start; i<=end; i++) {
			canvas.pixel(
				(int) (i),
				(double)i / pixelCount(),
				1.0,
				remainingDurationFraction
			);
		}
	}

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Inner Classes

	enum Params implements ParameterId {
		Speed,
		Divisions
	}
	//endregion
}
