package org.hypher.gradientea.artnet.player.controller;

import org.hypher.gradientea.artnet.player.DomeAnimationServerMain;
import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.artnet.player.animations.DomeImageMapper;
import org.hypher.gradientea.artnet.player.animations.DomePixelCanvas;
import org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpec;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class DomeOutput {
	private UdpDomeClient transport;
	private GradienTeaDomeGeometry geometry;
	private DomePixelCanvas canvas;
	private DomeImageMapper imageMapper;

	private final int portOffset;

	public DomeOutput(
		final GradienTeaDomeSpec spec,
		int portOffset
	) {
		this.geometry = new GradienTeaDomeGeometry(spec);
		this.transport = new UdpDomeClient();
		this.canvas = new DomePixelCanvas(PixelCompositor.REPLACE, geometry);
		this.imageMapper = new DomeImageMapper(geometry);

		this.portOffset = portOffset;
	}

	public void start(String host) {
		final int port = DomeAnimationServerMain.DOME_PORT + portOffset;

		try {
			this.transport.connect(host, port);
		} catch (Exception e) {
			System.err.println("Failed to connect to dome controller at " + host + ":" + port);
			e.printStackTrace();
		}
	}


	public UdpDomeClient getTransport() {
		return transport;
	}

	public GradienTeaDomeGeometry getGeometry() {
		return geometry;
	}

	public DomePixelCanvas getCanvas() {
		return canvas;
	}

	public DomeImageMapper getImageMapper() {
		return imageMapper;
	}

	public void send() {
		transport.displayFrame(canvas.render());
	}
}
