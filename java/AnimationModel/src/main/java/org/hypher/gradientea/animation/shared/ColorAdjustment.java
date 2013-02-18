package org.hypher.gradientea.animation.shared;

/**
 * Something which can adjust a color.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface ColorAdjustment extends AnimationObject  {
	HsbColor adjust(HsbColor color);
}
