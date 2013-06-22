package org.hypher.gradientea.artnet.player.controller;

import org.hypher.gradientea.artnet.player.controller.programs.DomeAnimationProgram;

import java.util.Collection;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface DomeController {
	public Collection<DomeOutput> getOutputs();
	public DomeFluidCanvas getFluidCanvas();

	void selectProgram(DomeAnimationProgram.ProgramId newProgramId);
}
