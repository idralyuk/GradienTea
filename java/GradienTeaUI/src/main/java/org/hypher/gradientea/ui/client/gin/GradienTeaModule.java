package org.hypher.gradientea.ui.client.gin;

import com.google.gwt.inject.client.AbstractGinModule;
import org.hypher.gradientea.ui.client.comet.DomeAnimationCometTransport;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaModule extends AbstractGinModule {
	@Override
	protected void configure() {
		bind(DomeAnimationCometTransport.class).asEagerSingleton();
	}
}
