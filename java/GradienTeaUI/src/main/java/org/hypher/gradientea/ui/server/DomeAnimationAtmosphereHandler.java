package org.hypher.gradientea.ui.server;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.gwt.server.AtmosphereGwtHandler;
import org.atmosphere.gwt.server.GwtAtmosphereResource;
import org.hypher.gradientea.artnet.player.DomeAnimationServerMain;
import org.hypher.gradientea.artnet.player.HttpReceiver;
import org.hypher.gradientea.transport.shared.DomeAnimationFrame;
import org.hypher.gradientea.transport.shared.DomeAnimationTransport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeAnimationAtmosphereHandler extends AtmosphereGwtHandler {
	private static Broadcaster broadcaster;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
//		Logger.getLogger("").setLevel(Level.INFO);
//		Logger.getLogger("org.atmosphere.gwt").setLevel(Level.ALL);
//		Logger.getLogger("org.atmosphere.samples").setLevel(Level.ALL);
//		Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
//		logger.trace("Updated logging levels");

		try {
			new HttpReceiver(new DomeAnimationTransport() {
				@Override
				public void displayFrame(final DomeAnimationFrame frame) {
					if (broadcaster != null) {
						broadcaster.broadcast(frame);
					}
				}
			}).start(DomeAnimationServerMain.DOME_PORT);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int doComet(GwtAtmosphereResource resource) throws ServletException, IOException {
		resource.getBroadcaster().setID("GWT_COMET");
		HttpSession session = resource.getAtmosphereResource().getRequest().getSession(false);
		if (session != null) {
			logger.debug("Got session with id: " + session.getId());
			logger.debug("Time attribute: " + session.getAttribute("time"));
		} else {
			logger.warn("No session");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Url: " + resource.getAtmosphereResource().getRequest().getRequestURL()
				+ "?" + resource.getAtmosphereResource().getRequest().getQueryString());
		}
		String agent = resource.getRequest().getHeader("user-agent");
		logger.info(agent);

		broadcaster = resource.getBroadcaster();

		return NO_TIMEOUT;
	}

	@Override
	public void cometTerminated(GwtAtmosphereResource cometResponse, boolean serverInitiated) {
		super.cometTerminated(cometResponse, serverInitiated);
		logger.info("Comet disconnected");
	}

	@Override
	public void doPost(
		HttpServletRequest postRequest,
		HttpServletResponse postResponse,
		List<?> messages,
		GwtAtmosphereResource cometResource
	) {
		HttpSession session = postRequest.getSession(false);
		if (session != null) {
			logger.info("Post has session with id: " + session.getId());
		} else {
			logger.info("Post has no session");
		}
		super.doPost(postRequest, postResponse, messages, cometResource);
	}

}
