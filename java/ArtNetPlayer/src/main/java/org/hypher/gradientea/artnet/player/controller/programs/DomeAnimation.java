package org.hypher.gradientea.artnet.player.controller.programs;

import org.hypher.gradientea.artnet.player.controller.DomeController;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface DomeAnimation {
	/**
	 * Called to initialize the program. Will be called once.
	 */
	void init(DomeController controller);

	/**
	 * Called when this program has been selected to run.
	 */
	void start();

	/**
	 * Called on each frame. Program logic should be implemented here.
	 */
	void update();

	/**
	 * Called when another program has been selected to run. This should stop any threads or other processing
	 * to allow other programs to use the resources.
	 */
	void stop();
}
