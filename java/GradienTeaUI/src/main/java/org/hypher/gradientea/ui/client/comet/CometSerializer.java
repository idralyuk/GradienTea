package org.hypher.gradientea.ui.client.comet;

import org.atmosphere.gwt.client.AtmosphereGWTSerializer;
import org.atmosphere.gwt.client.SerialTypes;
import org.hypher.gradientea.transport.shared.DomeAnimationFrame;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
@SerialTypes(DomeAnimationFrame.class)
public abstract class CometSerializer extends AtmosphereGWTSerializer {}
