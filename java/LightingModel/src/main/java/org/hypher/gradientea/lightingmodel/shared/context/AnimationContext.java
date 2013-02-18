package org.hypher.gradientea.lightingmodel.shared.context;

import org.hypher.gradientea.lightingmodel.shared.dome.geometry.GradienTeaDomeGeometry;

import java.io.Serializable;

/**
 * Provides context about the environment that an animation takes place in so that individual animation elements
 * don't need to have direct references to common objects.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AnimationContext implements Serializable {
	protected GradienTeaDomeGeometry gradienTeaDomeGeometry;

	protected AnimationContext() {}

	public AnimationContext(final GradienTeaDomeGeometry gradienTeaDomeGeometry) {
		this.gradienTeaDomeGeometry = gradienTeaDomeGeometry;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public GradienTeaDomeGeometry getGradienTeaDomeGeometry() {
		return gradienTeaDomeGeometry;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
