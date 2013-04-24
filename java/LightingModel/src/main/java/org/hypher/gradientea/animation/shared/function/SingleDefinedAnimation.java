package org.hypher.gradientea.animation.shared.function;

import org.hypher.gradientea.animation.shared.pixel.PixelGroup;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;

import java.util.List;

/**
 * An {@link PixelGroupAnimation} and a {@link org.hypher.gradientea.animation.shared.pixel.PixelGroup} to which it should be applied.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class SingleDefinedAnimation implements DefinedAnimation {
	/**
	 * The animation definition
	 */
	protected PixelGroupAnimation animation;

	/**
	 * The pixels that the animation should be rendered to
	 */
	protected PixelGroup group;

	public SingleDefinedAnimation() { }

	public SingleDefinedAnimation(final PixelGroupAnimation animation, final PixelGroup group) {
		this.animation = animation;
		this.group = group;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> render(final double fraction) {
		return animation.render(group, fraction);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "SingleDefinedAnimation{" +
			"group=" + group +
			", animation=" + animation +
			'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public PixelGroup getGroup() {
		return group;
	}

	public PixelGroupAnimation getAnimation() {
		return animation;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
