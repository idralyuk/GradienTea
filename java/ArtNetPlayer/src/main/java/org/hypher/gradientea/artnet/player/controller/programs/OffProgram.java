package org.hypher.gradientea.artnet.player.controller.programs;

/**
 * Program that does nothing, just a blank dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class OffProgram extends BaseDomeProgram {

	public OffProgram() {
		super(ProgramId.OFF);
	}

	@Override
	protected void initialize() {
	}

	@Override
	public void start() {
	}

	@Override
	public void update() {
		fluidCanvas().fade(0.8f);
	}

	@Override
	public boolean isFocusDesired() {
		return false;
	}


	@Override
	public void stop() {
	}
}
