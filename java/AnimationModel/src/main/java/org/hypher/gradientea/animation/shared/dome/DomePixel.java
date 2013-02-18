package org.hypher.gradientea.animation.shared.dome;

import org.hypher.cliententity.shared.entity.BaseEntityAware;
import org.hypher.cliententity.shared.entity.ClientEntityAware;
import org.hypher.gradientea.animation.shared.Pixel;

import java.util.Collections;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomePixel extends BaseEntityAware implements Pixel {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation Methods

	@Override
	protected Iterable<? extends ClientEntityAware> getEntityAwareChildren() {
		return Collections.emptySet();
	}

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Inner Classes

	//endregion
}
