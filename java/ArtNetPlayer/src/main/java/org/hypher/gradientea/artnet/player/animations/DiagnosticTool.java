package org.hypher.gradientea.artnet.player.animations;

import com.google.common.collect.ImmutableList;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.artnet.player.DomeAnimationServerMain;
import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DiagnosticTool implements Runnable {

	public static final int FRAME_DURATION = 100;

	public static void main(String[] args) throws SocketException, UnknownHostException {
		new DiagnosticTool();
	}

	GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(GradienTeaDomeSpecs.GRADIENTEA_DOME);
	UdpDomeClient transport = new UdpDomeClient();

	public DiagnosticTool() throws SocketException, UnknownHostException {
		transport.connect("localhost", DomeAnimationServerMain.DOME_PORT+1);

		new Thread(this).start();
	}

	@Override
	public void run() {
		DomePixelCanvas canvas = new DomePixelCanvas(
			PixelCompositor.REPLACE,
			geometry);

		while (true) {
			long start = System.currentTimeMillis();

			canvas.clear();
			draw(canvas);

			transport.displayFrame(canvas.render());

			try {
				long duration = (System.currentTimeMillis() - start);
				Thread.sleep(Math.max(0, FRAME_DURATION - duration));
			} catch (InterruptedException e) {}
		}
	}

	int frameCount = 0;

	List<GeoFace> panels = ImmutableList.copyOf(geometry.getLightedFaces());
	private void draw(final DomePixelCanvas canvas) {
		frameCount ++;

		canvas.draw(
			panels.get(frameCount % panels.size()),
			.33,
			1,
			1
		);
	}

	private HsbColor colorAt(double theta, double phi) {
		return new HsbColor(
			((Math.sin(phi*3)*Math.sin(theta*3)+1)/2),
			1.0,
			1.0
		);
	}

	private int lightedLayers() {
		return geometry.getSpec().getLightedLayers();
	}
}
