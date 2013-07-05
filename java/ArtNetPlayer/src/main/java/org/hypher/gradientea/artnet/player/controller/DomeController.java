package org.hypher.gradientea.artnet.player.controller;

import org.hypher.gradientea.artnet.player.controller.programs.DomeAnimationProgram;

import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface DomeController {
	public List<DomeOutput> getOutputs();
	public DomeFluidCanvas getFluidCanvas();

	void selectProgram(DomeAnimationProgram.ProgramId newProgramId);
}
