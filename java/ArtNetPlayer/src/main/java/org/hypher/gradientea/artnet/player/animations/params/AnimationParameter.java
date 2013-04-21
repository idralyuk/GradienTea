package org.hypher.gradientea.artnet.player.animations.params;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface AnimationParameter {
	String getName();
	int getValue();
	int getMaxValue();
	void setValue(int value);
}
