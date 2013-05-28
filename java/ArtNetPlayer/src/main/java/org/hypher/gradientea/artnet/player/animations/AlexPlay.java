package org.hypher.gradientea.artnet.player.animations;

import com.google.common.collect.ImmutableList;
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
public class AlexPlay implements Runnable {
	public static void main(String[] args) throws SocketException, UnknownHostException {
		new AlexPlay();
	}

	GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(GradienTeaDomeSpecs.PROTOTYPE_DOME);
	UdpDomeClient transport = new UdpDomeClient();

	public AlexPlay() throws SocketException, UnknownHostException {
		transport.connect("localhost");

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
				Thread.sleep(Math.max(0, (33) - duration));
			} catch (InterruptedException e) {}
		}
	}

	int frameCount = 0;
	int index = 0;
	int frequency = 1;

	List<GeoFace> panels = ImmutableList.copyOf(geometry.getLightedFaces());

	private void draw(final DomePixelCanvas canvas) {
		canvas.draw(
			panels.get((int) (Math.random() * panels.size())),
			Math.random(),
			1.00,
			1.0
		);

		frameCount ++;
		index ++;
		if (index >= 20) index = 5;
	}

	private int lightedLayers() {
		return geometry.getSpec().getLightedLayers();
	}
}
