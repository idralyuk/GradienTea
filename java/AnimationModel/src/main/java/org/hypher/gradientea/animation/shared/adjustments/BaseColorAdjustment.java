package org.hypher.gradientea.animation.shared.adjustments;

import org.hypher.cliententity.shared.entity.BaseEntityAware;
import org.hypher.cliententity.shared.entity.ClientEntityAware;
import org.hypher.gradientea.animation.shared.ColorAdjustment;

import java.util.Collections;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class BaseColorAdjustment extends BaseEntityAware implements ColorAdjustment {
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Interface Implementation
	@Override
	protected Iterable<? extends ClientEntityAware> getEntityAwareChildren() {
		return Collections.emptySet();
	}
}
