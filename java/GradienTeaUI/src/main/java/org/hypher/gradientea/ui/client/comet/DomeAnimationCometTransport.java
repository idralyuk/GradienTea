package org.hypher.gradientea.ui.client.comet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.inject.Singleton;
import org.atmosphere.gwt.client.AtmosphereClient;
import org.atmosphere.gwt.client.AtmosphereGWTSerializer;
import org.atmosphere.gwt.client.AtmosphereListener;
import org.hypher.gradientea.transport.shared.DomeAnimationFrame;
import org.hypher.gradientea.transport.shared.DomeAnimationTransport;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
@Singleton
public class DomeAnimationCometTransport implements AtmosphereListener {
	private final Logger logger = Logger.getLogger(getClass().getName());

	private AtmosphereClient client;
	private DomeAnimationTransport destinationTransport;

	public DomeAnimationCometTransport() {}

	public void start(DomeAnimationTransport destinationTransport) {
		this.destinationTransport = destinationTransport;
		AtmosphereGWTSerializer serializer = GWT.create(CometSerializer.class);
		// set a small length parameter to force refreshes
		// normally you should remove the length parameter
		client = new AtmosphereClient(GWT.getModuleBaseURL() + "gwtComet", serializer, this, true);
		client.start();
	}

	@Override
	public void onConnected(final int heartbeat, final String connectionUUID) {
		logger.info("comet.connected [" + heartbeat + ", " + connectionUUID + "]");
	}

	@Override
	public void onBeforeDisconnected() {
		logger.log(Level.INFO, "comet.beforeDisconnected");
	}

	@Override
	public void onDisconnected() {
		logger.info("comet.disconnected");
	}

	@Override
	public void onError(final Throwable exception, final boolean connected) {
		int statuscode = -1;
		if (exception instanceof StatusCodeException) {
			statuscode = ((StatusCodeException) exception).getStatusCode();
		}
		logger.log(Level.SEVERE, "comet.error [connected=" + connected + "] (" + statuscode + ")", exception);
	}

	@Override
	public void onHeartbeat() {
		logger.info("comet.heartbeat [" + client.getConnectionUUID() + "]");
	}

	@Override
	public void onRefresh() {
		logger.info("comet.refresh [" + client.getConnectionUUID() + "]");
	}

	@Override
	public void onAfterRefresh(final String connectionUUID) {
		logger.info("comet.afterRefresh [" + connectionUUID + "]");
	}

	@Override
	public void onMessage(final List<?> messages) {
		logger.fine("Received Messages: " + messages);

		for (Object message : messages) {
			if (message instanceof DomeAnimationFrame) {
				destinationTransport.displayFrame((DomeAnimationFrame) message);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
