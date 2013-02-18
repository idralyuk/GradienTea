package org.hypher.gradientea.animation.shared;

import org.hypher.cliententity.shared.entity.ClientEntityAware;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface AdjustmentAnimation extends ClientEntityAware {
	ColorAdjustment render(double fractionalTime);
}
