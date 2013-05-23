package org.hypher.gradientea.artnet.player.animations;

import com.google.common.base.Optional;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.artnet.player.io.TrackballInput;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ColorDemo implements Runnable {
	public static void main(String[] args) throws SocketException, UnknownHostException {
		new ColorDemo();
	}

	GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(GradienTeaDomeSpecs.PROTOTYPE_DOME);

	UdpDomeClient transport = new UdpDomeClient();


	public ColorDemo() throws SocketException, UnknownHostException {
		transport.connect("localhost");

		new Thread(this).start();
	}

	@Override
	public void run() {
		DomePixelCanvas canvas = new DomePixelCanvas(
			org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor.ADDITIVE,
			geometry);

		while (true) {
			long start = System.currentTimeMillis();

			canvas.clear();
			draw(canvas);

			transport.displayFrame(canvas.render());

			try {
				Thread.sleep(33);
			} catch (InterruptedException e) {}
		}
	}

	double hue = 0;
	double saturation = 0;
	double brightness = 0;

	private void draw(final DomePixelCanvas canvas) {
		Optional<TrackballInput.TrackballReading> trackballReading = TrackballInput.instance().read();
		if (trackballReading.isPresent()) {
			saturation += trackballReading.get().getDeltaX() * 0.001;
			brightness += trackballReading.get().getDeltaY() * -0.001;
		}

		saturation = Math.max(0, Math.min(1, saturation));
		brightness = Math.max(0, Math.min(1, brightness));

		hue += 0.005;
		if (hue > 1.0) hue = 0;
		canvas.draw(geometry.getLightedFaces(), new HsbColor(hue, saturation, brightness));
	}

}
