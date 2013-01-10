package org.hypher.gradientea.ui.client.player;

/**
 * Something which can render dmx channel values
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface DmxInterface {
	/**
	 * The DMX Channel data to send.
	 *
	 * @param dmxChannelValues
	 */
	public void display(int[][] dmxChannelValues);
}
