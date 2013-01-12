package org.hypher.gradientea.lightingmodel.shared.rendering;

import org.hypher.gradientea.lightingmodel.shared.animation.DefinedAnimation;

/**
 * A {@link org.hypher.gradientea.lightingmodel.shared.animation.DefinedAnimation} with all necessary components to be rendered.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RenderableAnimation {
	/**
	 * The animation definition
	 */
	protected DefinedAnimation animation;

	/**
	 * The suggested duration for the animation in seconds.
	 */
	protected double suggestedDurationSeconds;

	public RenderableAnimation() {
	}

	public RenderableAnimation(final DefinedAnimation animation, final double suggestedDurationSeconds) {
		this.animation = animation;
		this.suggestedDurationSeconds = suggestedDurationSeconds;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "RenderableAnimation{" +
			"animation=" + animation +
			", suggestedDurationSeconds=" + suggestedDurationSeconds +
			'}';
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public DefinedAnimation getAnimation() {
		return animation;
	}

	public double getSuggestedDurationSeconds() {
		return suggestedDurationSeconds;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}