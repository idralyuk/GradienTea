package org.hypher.gradientea.artnet.player.controller.programs;

import org.hypher.gradientea.artnet.player.io.osc.OscHelper;

import static org.hypher.gradientea.artnet.player.io.osc.OscHelper.multitouch;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.TWO_PI;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ManualControlProgram extends BaseDomeProgram {
	private OscHelper.OscMultitouch oscManualEmitters = multitouch("/gt/control/fluid/manualPad");
	long lastTouchAt = 0;

	public ManualControlProgram() {
		super(ProgramId.MANUAL);
	}

	@Override
	protected void initialize() {
	}

	@Override
	public void start() {
	}

	@Override
	public void update() {
		// Add manual touches
		for (OscHelper.OscMultitouch.Touch touch : oscManualEmitters.getTouches().values()) {
			fluidCanvas().emitDirectional(
				(float) touch.getCurrentX(), (float) touch.getCurrentY(),
				(float) touch.getAngle(),
				(float) (
					Math.sin(touch.getInitialX() * TWO_PI) *
						Math.sin(touch.getInitialY() * TWO_PI)
				),
				(float) touch.getVelocity() * 0.5f,
				(float) touch.getVelocity() * 100
			);
		}
	}

	@Override
	public boolean isFocusDesired() {
		if (! oscManualEmitters.getTouches().isEmpty()) {
			lastTouchAt = System.currentTimeMillis();
		}

		return (System.currentTimeMillis() - lastTouchAt) < 5000;
	}

	@Override
	public void stop() {
	}
}
