package org.hypher.gradientea.animation.shared.pixel;

import org.hypher.gradientea.animation.shared.color.PixelColor;

import java.util.Collections;
import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AbstractPixel implements Pixel {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> applyColor(final PixelColor color) {
		return Collections.singletonList(
			new PixelValue(
				this,
				color
			)
		);
	}

	@Override
	public List<PixelGroup> getChildren() {
		return Collections.<PixelGroup>singletonList(this);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
