package org.hypher.gradientea.transport.shared;

/**
 * Interface for an animation transport, that is, something which can carry animation data to the dome to be displayed.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface DomeAnimationTransport {
	void displayFrame(DomeAnimationFrame frame);
}
