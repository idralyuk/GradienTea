package org.hypher.gradientea.artnet.player.animations;

import com.google.common.base.Optional;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.artnet.player.DomeAnimationServerMain;
import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.artnet.player.io.BasicAudioReader;
import org.hypher.gradientea.artnet.player.io.GlobalAudioReader;
import org.hypher.gradientea.artnet.player.io.TrackballInput;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;

import java.net.SocketException;
import java.net.UnknownHostException;

import static org.hypher.gradientea.artnet.player.io.osc.OscHelper.multitouch;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.normalizeAngle;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class FirstDomeSphere implements Runnable {
	public static void main(String[] args) throws SocketException, UnknownHostException {
		new FirstDomeSphere();
	}

	GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(GradienTeaDomeSpecs.GRADIENTEA_DOME);
	UdpDomeClient transport = new UdpDomeClient();

	protected double thetaOffset = 0.0;
	protected double phiOffset = 0.0;

	protected double thetaVelocity = 0.0;
	protected double phiVelocity = 0.0;

	protected double ballTheta = 0;
	protected double ballPhi = 0;
	protected double ballRadius = Math.PI*0.25;

	private OscHelper.OscMultitouch manualEmitters = multitouch("/gt/manualEmitter", 0.1);

	public FirstDomeSphere() throws SocketException, UnknownHostException {
		transport.connect("localhost", DomeAnimationServerMain.DOME_PORT);

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

	double intensity = 0.5;
	int frameCount=0;

	private void draw(final DomePixelCanvas canvas) {
		Optional<TrackballInput.TrackballReading> trackballReading = TrackballInput.instance().read();

		thetaVelocity *= 0.95;
		phiVelocity *= 0.95;

		frameCount++;

		if (trackballReading.isPresent()) {
			thetaVelocity += trackballReading.get().getDeltaX() * -0.001;
			phiVelocity += trackballReading.get().getDeltaY() * 0.001;
		}

		if (! manualEmitters.getTouches().isEmpty())
		{
			for (OscHelper.OscMultitouch.Touch touch : manualEmitters.getTouches().values()) {
				thetaVelocity += touch.getDeltaX() * 0.5;
				phiVelocity += touch.getDeltaY() * 0.5;
			}
		}

		thetaOffset += thetaVelocity;
		phiOffset += phiVelocity;

		BasicAudioReader.LevelAverage longestAverage = GlobalAudioReader.getReader().getRMSMean(5.0f);
		double longAverage = Math.max(0, GlobalAudioReader.getReader().getRMSMean(1f).mean() - 0.005);
		double now = Math.max(0, GlobalAudioReader.getReader().getRMSMean(1/30f).mean()-0.005);

		if (now > longestAverage.mean()*.9) {
			intensity += 0.5;
		} else {
			intensity *= 0.7;
		}

		intensity = Math.max(0, Math.min(1, intensity));

//		thetaVelocity += (intensity-0.3) * 0.008;
//		phiVelocity += (intensity-0.3) * 0.005;

		double effectiveBallRadiusSq = Math.pow(ballRadius*0.25 + (ballRadius*0.75) * intensity, 2);

		for (GeoFace face : geometry.getLightedFaces()) {
			final double theta = face.theta();
			final double phi = face.phi();

			HsbColor color = colorAt(theta + thetaOffset, phi + phiOffset, thetaOffset, phiOffset);

			double distanceSq = Math.pow(normalizeAngle(theta-ballTheta), 2) + Math.pow(normalizeAngle(phi-ballPhi), 2);

			// Is this part of the little circle of white?
//			if (distanceSq < effectiveBallRadiusSq) {
//				color = color.add(
//					0,
//					(distanceSq / effectiveBallRadiusSq)-1,
//					(1-(distanceSq / effectiveBallRadiusSq))
//				);
//			}

			canvas.draw(face, color);
		}
	}

	private HsbColor colorAt(double theta, double phi, double thetaOffset, double phiOffset) {
		return new HsbColor(
			Math.sin(thetaOffset) * Math.sin(phiOffset) * Math.sin(frameCount/200d),
			0.5+((Math.sin(phi)*Math.sin(theta*2)+1)/2)*0.5,
			Math.max(0, (-1*(Math.sin(phi*3)*Math.sin(theta*3)+0.5)/2))
		);
	}

	private int lightedLayers() {
		return geometry.getSpec().getLightedLayers();
	}
}
