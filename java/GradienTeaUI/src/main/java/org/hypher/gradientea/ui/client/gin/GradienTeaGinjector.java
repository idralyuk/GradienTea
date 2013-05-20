package org.hypher.gradientea.ui.client.gin;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import org.hypher.gradientea.ui.client.comet.DomeAnimationCometTransport;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
@GinModules(GradienTeaModule.class)
public interface GradienTeaGinjector extends Ginjector {
	DomeAnimationCometTransport getDomeAnimationCometTransport();
}
