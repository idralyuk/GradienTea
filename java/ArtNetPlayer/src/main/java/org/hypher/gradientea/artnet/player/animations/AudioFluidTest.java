package org.hypher.gradientea.artnet.player.animations;

import com.google.common.base.Optional;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.artnet.player.DomeAnimationServerMain;
import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.artnet.player.io.BasicAudioReader;
import org.hypher.gradientea.artnet.player.io.GlobalAudioReader;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;
import org.hypher.gradientea.geometry.shared.math.DomeMath;
import org.msafluid.MSAFluidSolver2D;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import static org.hypher.gradientea.artnet.player.io.osc.OscHelper.*;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.TWO_PI;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.f;

/**
 * An animation which attempts to use {@link MSAFluidSolver2D} to draw pretty things on the dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AudioFluidTest implements Runnable {
	public final static int WIDTH = 30;
	public final static int HEIGHT = 30;
	public final static float ASPECT_RATIO = (float)WIDTH/HEIGHT;
	public static final int FREQUENCY_BANDS = 7;
	public static final int LOW_FREQ_BUCKET = 4;
	public static final int HIGH_FREQ_BUCKET = 128;
	private static final int FPS = 30;
	private OscHelper.OscXY circleEmitterTarget = xyValue("/gt/emitter/pad", .5, .5);

	private OscHelper.OscDouble intensityDecayRate = doubleValue(
		"/gt/emitter/intensityDecay",
		0,
		1.0,
		0.85
	);
	private OscHelper.OscDouble fadeSpeedCoefficient = doubleValue(
		"/gt/fluid/fadeBeatCoefficient",
		0,
		1.0,
		.2
	);
	private OscHelper.OscDouble fadeSpeedBase = doubleValue("/gt/fluid/fade", 0.00, 0.10, .04);

	private OscHelper.OscDouble velocityCoefficient = doubleValue(
		"/gt/emitter/velocity",
		0,
		0.10,
		0.02
	);
	private OscHelper.OscDouble intensityCoefficient = doubleValue(
		"/gt/emitter/intensity",
		0,
		10,
		4
	);

	private OscHelper.OscMultitouch manualEmitters = multitouch("/gt/manualEmitter");

	private OscHelper.OscBoolean enableSoundEmitters = booleanValue("/gt/enableSoundEmitters", true);

	private OscHelper.OscBoolean ballEnabled = booleanValue(
		"/gt/vuBall/enabled",
		true
	);

	private OscHelper.OscDouble ballRadius = doubleValue(
		"/gt/vuBall/radius",
		0,
		Math.PI,
		Math.PI / 4
	);

	private OscHelper.OscXY ballPosition = xyValue(
		"/gt/vuBall/position",
		0, 0
	);

	public static void main(String[] args) throws IOException {
		OscHelper.instance();
		new AudioFluidTest();
		AudioVisualizationTest.main(args);
	}
	private UdpDomeClient prototypeDomeTransport = new UdpDomeClient();
	private UdpDomeClient miniDomeTransport = new UdpDomeClient();

	private final GradienTeaDomeGeometry prototypeGeometry =
		new GradienTeaDomeGeometry(GradienTeaDomeSpecs.PROTOTYPE_DOME);

	private final GradienTeaDomeGeometry miniDomeGeometry =
		new GradienTeaDomeGeometry(GradienTeaDomeSpecs.GRADIENTEA_DOME);

	private DomeImageMapper prototypeMapper = new DomeImageMapper(prototypeGeometry);


	private DomeImageMapper miniDomeMapper =  new DomeImageMapper(miniDomeGeometry);

	DomePixelCanvas miniDomeCanvas = new DomePixelCanvas(
		org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor.ADDITIVE,
		miniDomeGeometry
	);

	DomePixelCanvas prototypeDomeCanvas = new DomePixelCanvas(
		org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor.ADDITIVE,
		prototypeGeometry
	);


	private BufferedImage image;

	private MSAFluidSolver2D fluidSolver;

	private AudioAnalyzer audioAnalyzer = new AudioAnalyzer();

	public AudioFluidTest() throws IOException {
		prototypeDomeTransport.connect("localhost", DomeAnimationServerMain.DOME_PORT);
		miniDomeTransport.connect("localhost", DomeAnimationServerMain.DOME_PORT + 1);

		fluidSolver = new MSAFluidSolver2D(WIDTH, HEIGHT);
		fluidSolver.enableRGB(true).setFadeSpeed(0.015f).setDeltaT(0.8f).setVisc(0.00023f);

		image = new BufferedImage(fluidSolver.getWidth(), fluidSolver.getHeight(), BufferedImage.TYPE_INT_RGB);

		OscHelper.instance().mapValue(
			"/gt/fluid/viscosity", new OscDouble("/gt/fluid/viscosity", 0.00001, 0.00100, fluidSolver.getVisc()) {
			@Override
			public void applyDouble(final double value) {
				fluidSolver.setVisc(f(value));
			}

			@Override
			public double getValue() {
				return fluidSolver.getVisc();
			}
		}
		);

		OscHelper.instance().mapValue(
			"/gt/fluid/dt", new OscHelper.OscDouble("/gt/fluid/dt", 0.1, 2.0, fluidSolver.getVisc()) {
			@Override
			public void applyDouble(final double value) {
				fluidSolver.setDeltaT(f(value));
			}

			@Override
			public double getValue() {
				return fluidSolver.getDeltaT();
			}
		}
		);

		new Thread(this).start();
	}


	private int frameCount = 0;
	@Override
	public void run() {
		long lastFrame = System.currentTimeMillis();
		while (true) {

			long frameStart = System.currentTimeMillis();
			prototypeDomeCanvas.clear();
			miniDomeCanvas.clear();
			frameCount ++;
			draw(prototypeDomeCanvas, frameStart - lastFrame);
			lastFrame = frameStart;

			prototypeDomeTransport.displayFrame(prototypeDomeCanvas.render());
			miniDomeTransport.displayFrame(miniDomeCanvas.render());

			try {
				Thread.sleep(Math.max(0, (1000/FPS) - (System.currentTimeMillis() - lastFrame)));
			} catch (InterruptedException e) {}
		}
	}

	private JFrame frame;
	{
		frame = new JFrame("Fluid Simulation Mapping"){
			@Override
			public void paint(final Graphics graphics) {
				if (image != null) {
					Graphics2D g2 = (Graphics2D) graphics;
					g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON
					);

					graphics.drawImage(image, 0, 0, getWidth()/2, getHeight(), null);
					prototypeMapper.drawMask(g2, 0, 0, getWidth()/2, getHeight());

					graphics.drawImage(image, getWidth()/2, 0, getWidth()/2, getHeight(), null);
					miniDomeMapper.drawMask(g2, getWidth()/2, 0, getWidth()/2, getHeight());
				}
			}
		};
		frame.setSize(480*2, 480);
		frame.setLocation(0,520);
		frame.setVisible(true);
	}

	private void draw(final DomePixelCanvas canvas, final long elapsedMs) {
		updateFluid();
		fluidSolver.update();

		for(int i=0; i<fluidSolver.getNumCells(); i++) {
			int y = i/image.getWidth();
			int x = i - y*image.getWidth();

			fluidSolver.r[i] = fluidSolver.r[i] < 0.0001 ? 0.000f : fluidSolver.r[i];
			fluidSolver.g[i] = fluidSolver.g[i] < 0.0001 ? 0.000f : fluidSolver.g[i];
			fluidSolver.b[i] = fluidSolver.b[i] < 0.0001 ? 0.000f : fluidSolver.b[i];

			try {
			image.setRGB(x, y, new Color(
				(float) Math.min(1.0, fluidSolver.r[i]*2.5),
				(float) Math.min(1.0, fluidSolver.g[i]*2.5),
				(float) Math.min(1.0, fluidSolver.b[i]*2.5)
			).getRGB());
			} catch (Exception e) {
				System.err.println(i + " " + x + " " + y);
				break;
			}
		}

		prototypeMapper.drawImage(
			image,
			prototypeDomeCanvas
		);

		miniDomeMapper.drawImage(
			image,
			miniDomeCanvas
		);

//
//		double effectiveBallRadiusSq = Math.pow((ballRadius.doubleValue()+Math.PI/8) * (1-Math.abs(overallBeatLevel)*10), 2);
//
//		double ballTheta = (ballPosition.getX()-0.5) * Math.PI;
//		double ballPhi = (ballPosition.getY()-0.5) * Math.PI;
//
//		for (GeoFace face : geometry.getLightedFaces()) {
//			final double theta = face.theta();
//			final double phi = face.phi();
//
//
//			double distanceSq = Math.pow(normalizeAngle(theta-ballTheta), 2) + Math.pow(normalizeAngle(phi-ballPhi), 2);
//
//			// Is this part of the little circle of white?
//			if (distanceSq < effectiveBallRadiusSq) {
//				double factor = distanceSq/effectiveBallRadiusSq;
//				canvas.draw(face, new RgbColor(factor*128, factor*128, factor*128));
//			}
//		}

		frame.repaint();
	}


	float[] intensities = new float[FREQUENCY_BANDS];
	float angleOffset = 0;
	float overallBeatLevel = 0;

	private void updateFluid() {
		Optional<Beat> beat = audioAnalyzer.findBeat();

		if (Float.isNaN(angleOffset)) angleOffset = 0;
		float longRms = GlobalAudioReader.getReader().getRMSMean(1f).mean();
		float nowRms = GlobalAudioReader.getReader().getBuffer(0).getRms();

		if (nowRms > longRms*1.3) {
			overallBeatLevel = .1f;
		} else if (nowRms < longRms*.7) {
			overallBeatLevel = -.1f;
		}

		if (beat.isPresent()) {
			intensities[beat.get().freq] += beat.get().intensity;
		}

		if (enableSoundEmitters.value()) {
			for (int i=0; i<intensities.length; i++) {
				float intensity = intensities[i];

				float angle = f((f(i)/intensities.length) * TWO_PI) + angleOffset;

				emitDirectional(
					f(0.5 + Math.cos(angle)*0.45 * (1- overallBeatLevel *0.25)),
					f(0.5 + Math.sin(angle)*0.45 * (1- overallBeatLevel *0.25)),

					(float)circleEmitterTarget.getX(), (float)circleEmitterTarget.getY(),

					(f(i)/intensities.length) + (frameCount % 300) / 300f,

					intensity* velocityCoefficient.floatValue(),
					intensity* intensityCoefficient.floatValue()
				);


				intensities[i] *= intensityDecayRate.floatValue();
			}
		}

		overallBeatLevel *= 0.9;
		angleOffset += overallBeatLevel*.3;

		fluidSolver.setFadeSpeed(f(fadeSpeedBase.floatValue() -overallBeatLevel* fadeSpeedCoefficient.floatValue()));

		// Add manual touches
		for (OscHelper.OscMultitouch.Touch touch : manualEmitters.getTouches().values()) {
			emitDirectional(
				(float) touch.getCurrentX(), (float) touch.getCurrentY(),
				(float) touch.getAngle(),
				f(1 + Math.sin(touch.getInitialX()*TWO_PI)*Math.sin(touch.getInitialY()*TWO_PI))/2f,
				(float) touch.getVelocity()*0.5f,
				(float) touch.getVelocity()*1000
			);
		}
	}

	private void emitDirectional(
		float fromX,
		float fromY,

		float toX,
		float toY,

		float hue,
		float velocity,
		float intensity
	) {
		emitDirectional(
			fromX, fromY,
			f(Math.atan2(toY - fromY, toX - fromX)),
			hue, velocity, intensity
		);
	}


	private void emitDirectional(
		float fromX,
		float fromY,

		float angle,

		float hue,
		float velocity,
		float intensity
	) {
		int[] drawColor = new HsbColor(hue, 1.0, 1.0).asRgb();

		fluidSolver.addColorAtPos(
			fromX,
			fromY,
			(drawColor[0]/255f) * intensity,
			(drawColor[1]/255f) * intensity,
			(drawColor[2]/255f) * intensity
		);

		fluidSolver.addForceAtPos(
			fromX,
			fromY,
			(float) (Math.cos(angle) * velocity),
			(float) (Math.sin(angle) * velocity)
		);
	}

	private void emitRing(
		float fromX,
		float fromY,

		float radius,
		float duty,

		float hue,
		float velocity,
		float intensity
	) {
		int[] drawColor = new HsbColor(hue, 1.0, 1.0).asRgb();

		double circumference = 2*Math.PI*radius*WIDTH;
		for (double i=0; i<circumference; i+=1/duty) {
			double angle = (i/circumference) * TWO_PI;

			final float x = fromX + f(Math.cos(angle) * radius);
			final float y = fromY + f(Math.sin(angle) * radius);
			fluidSolver.addColorAtPos(
				x,
				y,

				(drawColor[0]/255f) * intensity,
				(drawColor[1]/255f) * intensity,
				(drawColor[2]/255f) * intensity
			);

			fluidSolver.addForceAtPos(
				x,
				y,
				f(Math.cos(angle) * velocity),
				f(Math.sin(angle) * velocity)
			);
		}
	}

	public static class AudioAnalyzer {
		private int averageQueueSize = 20;
		private int sampleQueueSize = 3;

		Deque<float[]> averageQueue = new ArrayDeque<float[]>(averageQueueSize);
		List<float[]> sampleQueue = new ArrayList<float[]>(sampleQueueSize);

		public Optional<Beat> findBeat() {
			if (averageQueue.size() >= 20) averageQueue.remove();

			float[] compressedFft = new float[FREQUENCY_BANDS];
			BasicAudioReader.compressLogWithRms(
				GlobalAudioReader.getReader().getBuffer(0).getFreqDomainData(),
				LOW_FREQ_BUCKET,
				HIGH_FREQ_BUCKET,
				compressedFft
			);
			sampleQueue.add(compressedFft);

			if (sampleQueue.size() == sampleQueueSize) {
				averageQueue.add(DomeMath.average(sampleQueue));
				sampleQueue.clear();

				if (averageQueue.size() > 2) {

					int highestIndex = highestIndex(compressedFft);

					for (int i=0; i<compressedFft.length; i++) {
						Iterator<float[]> historyIter = averageQueue.descendingIterator();

						float sample_0 = historyIter.next()[i];
						float sample_1 = historyIter.next()[i];
						float sample_2 = historyIter.next()[i];

						boolean beat = sample_1 > sample_0 && sample_1 > sample_2;

						float value = (float) Math.max(0, Math.min(1, sample_1*0.25));

						if (i == highestIndex && beat) {
							return Optional.of(new Beat(
								i,
								value
							));
						}
					}
				}
			}

			return Optional.absent();
		}

		private int highestIndex(final float[] data) {
			float highestValue = 0;
			int highestIndex = 0;

			for (int i=0; i<data.length; i++) {
				if (data[i] > highestValue) {
					highestValue = data[i];
					highestIndex = i;
				}
			}

			return highestIndex;
		}
	}

	public static class Beat {
		int freq;
		float intensity;

		public Beat(final int freq, final float intensity) {
			this.freq = freq;
			this.intensity = intensity;
		}
	}
}
