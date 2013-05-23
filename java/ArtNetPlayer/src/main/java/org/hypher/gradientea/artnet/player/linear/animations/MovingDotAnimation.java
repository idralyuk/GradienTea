package org.hypher.gradientea.artnet.player.linear.animations;

import org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class MovingDotAnimation extends BaseAnimation {
	public MovingDotAnimation(
		final AnimationContext context
	) {
		super(context, PixelCompositor.REPLACE);

		addParameter(Params.Width, 4, 24);
	}

	@Override
	protected void onParameterChanged(
		final ParameterId id, final int newValue
	) {
		super.onParameterChanged(id, newValue);
	}

	@Override
	protected void draw(final PixelCanvas canvas, final double fraction) {
		canvas.clear();

		int width = (int) (getFractionalParamValue(Params.Width) * pixelCount());
		int center = (int) (fraction*pixelCount());

		for (int i = center - width/2; i<center + width/2; i++) {
			canvas.pixel(
				i,
				(double) i / pixelCount(),
				1.0,
				1 - (double) Math.abs(i - center) / (width/2)
			);
		}
	}

	enum Params implements ParameterId {
		Width;
	}
}
