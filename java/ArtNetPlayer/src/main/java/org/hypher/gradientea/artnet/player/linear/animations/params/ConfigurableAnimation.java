package org.hypher.gradientea.artnet.player.linear.animations.params;

import org.hypher.gradientea.artnet.player.linear.ArtNetAnimationPlayer;

import java.util.Collection;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface ConfigurableAnimation {
	Collection<? extends AnimationParameter> getParameters();
	void play(ArtNetAnimationPlayer player);
	void init();
	void stop();
}
