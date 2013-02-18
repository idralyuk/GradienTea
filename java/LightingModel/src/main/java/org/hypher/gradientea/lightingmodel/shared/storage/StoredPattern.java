package org.hypher.gradientea.lightingmodel.shared.storage;

import org.hypher.gradientea.lightingmodel.shared.animation.AttachedAnimation;
import org.hypher.gradientea.lightingmodel.shared.context.RenderingContext;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;

import java.io.Serializable;
import java.util.List;

/**
 * A named animation meant to be reused as a pattern.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class StoredPattern implements Serializable, AttachedAnimation {
	private String name;
	private String description;
	private AttachedAnimation animation;

	protected StoredPattern() {}

	public StoredPattern(final String name, final String description, final AttachedAnimation animation) {
		this.name = name;
		this.description = description;
		this.animation = animation;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public AttachedAnimation getAnimation() {
		return animation;
	}

	@Override
	public List<PixelValue> render(
		final RenderingContext renderingContext,
		final double fraction
	) {
		return animation.render(renderingContext, fraction);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
