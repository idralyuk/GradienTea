package org.hypher.gradientea.lightingmodel.shared.animation;

import org.hypher.gradientea.lightingmodel.shared.context.RenderingContext;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;

import java.util.List;

/**
 * An {@link Animation} and a {@link org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup} to which it should be applied.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class SingleAttachedAnimation implements AttachedAnimation {
	/**
	 * The animation definition
	 */
	protected Animation animation;

	/**
	 * The pixels that the animation should be rendered to
	 */
	protected PixelGroup group;

	protected RenderingContext renderingContext;

	public SingleAttachedAnimation() { }

	public SingleAttachedAnimation(final RenderingContext renderingContext, final Animation animation, final PixelGroup group) {
		this.renderingContext = renderingContext;
		this.animation = animation;
		this.group = group;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> render(
		final RenderingContext renderingContext,
		final double fraction
	) {
		return animation.render(renderingContext.extend(this.renderingContext), group, fraction);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "SingleAttachedAnimation{" +
			"group=" + group +
			", animation=" + animation +
			'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public PixelGroup getGroup() {
		return group;
	}

	public Animation getAnimation() {
		return animation;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
