package org.hypher.gradientea.artnet.player.animations;

import com.google.common.collect.Lists;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.artnet.player.io.GlobalAudioReader;
import org.hypher.gradientea.artnet.player.io.BasicAudioReader;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GeodesicSphereGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class FirstDomeAudio implements Runnable {
	public static void main(String[] args) throws SocketException, UnknownHostException {
		new FirstDomeAudio();
	}

	GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(GradienTeaDomeSpecs.GRADIENTEA_DOME);

	UdpDomeClient transport = new UdpDomeClient();
	float[] freqBuffer = new float[lightedLayers()];
	private List<Ring> rings = Lists.newArrayList();


	public FirstDomeAudio() throws SocketException, UnknownHostException {
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

			canvas.scaleBrightness(0.05);
			draw(canvas);

			transport.displayFrame(canvas.render());

			try {
				Thread.sleep(33);
			} catch (InterruptedException e) {}
		}
	}

	double intensity = 0;

	private void draw(final DomePixelCanvas canvas) {
		BasicAudioReader.LevelAverage longestAverage = GlobalAudioReader.getReader().getRMSMean(5.0f);
		double longAverage = Math.max(0, GlobalAudioReader.getReader().getRMSMean(0.5f).mean() - 0.005);
		double now = Math.max(0, GlobalAudioReader.getReader().getRMSMean(1/30f).mean()-0.005);

		if (now > longAverage) {
			intensity += 0.1;
		} else {
			intensity -= 0.03;
		}

		if (now > longAverage * 2) {
			rings.add(new Ring(
				600,
				new HsbColor(
					intensity,
					Math.log(longAverage) / Math.log(longestAverage.mean()),
					now/(longAverage*2)
				)
			));
		}

		for (Iterator<Ring> i = rings.iterator(); i.hasNext(); ) {
			Ring ring = i.next();
			ring.draw(canvas);

			if (!ring.advance()) {
				i.remove();
			}
		}
	}

	private List<GeoFace> layer(int i) {
		return geometry.getDomeGeometry().ringsFrom(GeodesicSphereGeometry.topVertex).get(i);
	}

	private int lightedLayers() {
		return geometry.getSpec().getLightedLayers();
	}

	protected class Ring {
		long start = System.currentTimeMillis();
		private final long durationMs;
		HsbColor color;

		public Ring(long durationMs, final HsbColor color) {
			this.durationMs = durationMs;
			this.color = color;
		}

		public double progress() {
			return (double)(System.currentTimeMillis() - start) / durationMs;
		}

		public void draw(DomePixelCanvas canvas) {
			double progress = progress();

			canvas.draw(
				layer((int) Math.round((lightedLayers()-1) * progress)),
				new HsbColor(
					color.getHue(),
					color.getSaturation(),
					color.getBrightness() * (1-progress)
				)
			);
		}

		public boolean advance() {
			return progress() < 1.0;
		}
	}
}
