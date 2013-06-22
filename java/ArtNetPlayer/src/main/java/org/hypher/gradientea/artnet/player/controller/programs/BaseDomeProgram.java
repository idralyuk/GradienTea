package org.hypher.gradientea.artnet.player.controller.programs;

import org.hypher.gradientea.artnet.player.controller.DomeController;
import org.hypher.gradientea.artnet.player.controller.DomeFluidCanvas;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class BaseDomeProgram implements DomeAnimationProgram {
	protected final ProgramId programId;
	protected DomeController controller;

	public BaseDomeProgram(final ProgramId programId) {
		this.programId = programId;
	}

	@Override
	public ProgramId getProgramId() {
		return programId;
	}

	@Override
	public void init(final DomeController controller) {
		this.controller = controller;
		initialize();
	}

	protected abstract void initialize();

	protected DomeFluidCanvas fluidCanvas() {
		return controller.getFluidCanvas();
	}

	protected void selectThisProgram() {
		controller.selectProgram(getProgramId());
	}

	protected long now() {
		return System.currentTimeMillis();
	}
}
