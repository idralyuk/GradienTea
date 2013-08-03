package org.hypher.gradientea.artnet.player.controller.programs;

import org.hypher.gradientea.artnet.player.io.osc.OscHelper;

import static org.hypher.gradientea.artnet.player.io.osc.OscHelper.multitouch;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.f;

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

			for (int x=0; x<=1; x++) {
				for (int y=0; y<=1; y++) {
					fluidCanvas().emitDirectional(
						(float) touch.getCurrentX() + x*.03f,
						(float) touch.getCurrentY() + y*.03f,
						(float) touch.getAngle(),
//				(float) (
//					Math.sin(touch.getInitialX() * TWO_PI) *
//						Math.sin(touch.getInitialY() * TWO_PI)
//				),
						controller.getColor(f(touch.getInitialY())),
						(float) touch.getVelocity() * 0.2f,
						(float) touch.getVelocity() * 10
					);
				}
			}
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
