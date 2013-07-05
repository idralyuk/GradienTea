package org.hypher.gradientea.artnet.player.controller.programs;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class BaseDomeProgram extends BaseDomeAnimation implements DomeAnimationProgram {
	protected final ProgramId programId;

	public BaseDomeProgram(final ProgramId programId) {
		this.programId = programId;
	}

	@Override
	public ProgramId getProgramId() {
		return programId;
	}

	protected void selectThisProgram() {
		controller.selectProgram(getProgramId());
	}
}
