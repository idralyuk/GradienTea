package org.hypher.gradientea.lightingmodel.shared;

import com.google.common.base.Preconditions;
import org.hypher.gradientea.lightingmodel.shared.context.AnimationContext;
import org.hypher.gradientea.lightingmodel.shared.context.AnimationElement;
import org.hypher.gradientea.lightingmodel.shared.dome.geometry.GeodesicDomeGeometry;

/**
 * Base class for all animation elements.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class AbstractAnimationElement implements AnimationElement {
	private transient AnimationContext context;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// External Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Internal Methods

	protected GeodesicDomeGeometry domeGeometry() {
		return context.getGradienTeaDomeGeometry().getDomeGeometry();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public AnimationContext ensureContext() {
		Preconditions.checkState(isContextSet(), "AnimationContext has not been set for this element");

		return context;
	}

	public boolean isContextSet() {
		return context != null;
	}

	@Override
	public void setContext(final AnimationContext context) {
		Preconditions.checkState(this.context == null, "AnimationContext already set (%s) for this animation element (%s)", this.context, this);
		this.context = context;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
