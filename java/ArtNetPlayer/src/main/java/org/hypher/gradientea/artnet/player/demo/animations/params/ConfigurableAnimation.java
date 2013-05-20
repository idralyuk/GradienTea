package org.hypher.gradientea.artnet.player.demo.animations.params;

import org.hypher.gradientea.artnet.player.demo.ArtNetAnimationPlayer;

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
