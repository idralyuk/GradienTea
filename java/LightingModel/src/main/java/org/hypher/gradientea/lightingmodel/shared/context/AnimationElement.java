package org.hypher.gradientea.lightingmodel.shared.context;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface AnimationElement extends Serializable {
	AnimationContext getContext();
	void setContext(AnimationContext context);
	Collection<? extends AnimationElement> getChildElements();
}
