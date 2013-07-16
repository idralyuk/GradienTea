package org.hypher.gradientea.artnet.player.controller.programs;

import org.hypher.gradientea.artnet.player.controller.DomeController;
import org.hypher.gradientea.artnet.player.controller.DomeFluidCanvas;

import java.awt.Graphics2D;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class BaseDomeAnimation implements DomeAnimation {
	protected DomeController controller;

	@Override
	public final void init(final DomeController controller) {
		this.controller = controller;
		initialize();
	}

	@Override
	public void drawOverlay(final Graphics2D g, final int width, final int height) {

	}

	protected abstract void initialize();

	protected DomeFluidCanvas fluidCanvas() {
		return controller.getFluidCanvas();
	}

	protected long now() {
		return System.currentTimeMillis();
	}

}
