package org.hypher.gradientea.artnet.player.animations.params;

import org.hypher.gradientea.artnet.player.ArtNetAnimationPlayer;

import java.util.Collection;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface ConfigurableAnimation {
	Collection<? extends AnimationParameter> getParameters();
	void play(ArtNetAnimationPlayer player);
	void init();
}
