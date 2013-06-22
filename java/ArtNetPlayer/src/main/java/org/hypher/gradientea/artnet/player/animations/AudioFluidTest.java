package org.hypher.gradientea.artnet.player.animations;

import ddf.minim.AudioInput;
import ddf.minim.Sound;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.artnet.player.DomeAnimationServerMain;
import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;
import org.msafluid.MSAFluidSolver2D;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.hypher.gradientea.artnet.player.io.osc.OscHelper.*;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.TWO_PI;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.f;

/**
 * An animation which attempts to use {@link MSAFluidSolver2D} to draw pretty things on the dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AudioFluidTest implements Runnable {
	public final static int WIDTH = 40;
	public final static int HEIGHT = 40;
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
		.1
	);
	private OscHelper.OscDouble fadeSpeedBase = doubleValue("/gt/fluid/fade", 0.00, 0.10, .08);

	private OscHelper.OscDouble velocityCoefficient = doubleValue(
		"/gt/emitter/velocity",
		0.1,
		1.0,
		0.5
	);
	private OscHelper.OscDouble intensityCoefficient = doubleValue(
		"/gt/emitter/intensity",
		0,
		10,
		2
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
		new AudioFluidTest();
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
				final long sleepMillis = (1000 / FPS) - (System.currentTimeMillis() - frameStart);
				Thread.sleep(Math.max(0, sleepMillis));
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
					g2.fillRect(0, 0, getWidth(), getHeight());

					g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON
					);

					prototypeMapper.drawMask(g2, 0, 0, getWidth()/2, getHeight(), false, false);

					final int[] pixelRgb = new int[3];
					double circleWidth = (getWidth()/2) / image.getWidth();
					double circleHeight = getHeight() / image.getHeight();

					for (int x=0; x<image.getWidth(); x++) {
						for (int y=0; y<image.getHeight(); y++) {
							image.getData().getPixel(x, y, pixelRgb);

							if (pixelRgb[0] > 0 || pixelRgb[1] > 0 || pixelRgb[2] > 0) {
								g2.setColor(new Color(
									pixelRgb[0] / 255f,
									pixelRgb[1] / 255f,
									pixelRgb[2] / 255f,
									((pixelRgb[0] + pixelRgb[1] + pixelRgb[2])/3f) / 255f
								));
								g2.fillRect(
									(int) (x*circleWidth),
									(int) (y*circleHeight),
									(int) circleWidth,
									(int) circleHeight
								);
							}
						}
					}

					//graphics.drawImage(image, 0, 0, getWidth()/2, getHeight(), null);

					miniDomeMapper.drawMask(g2, getWidth()/2, 0, getWidth()/2, getHeight(), false, false);
					graphics.drawImage(image, getWidth()/2, 0, getWidth()/2, getHeight(), null);
				}
			}
		};

		frame.setSize(480*2, 480);
		frame.setLocation(50, 50);
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

		frame.repaint();
	}


	float[] intensities;
	float[] angleOffsets;
	float angleOffset = 0;
	float overallBeatLevel = 0;
	float rotationDirectionCounter = 100;

	private void updateFluid() {
		final AudioAnalysisInfo analysisInfo = audioAnalyzer.analyze();
		if (intensities == null) {
			intensities = new float[analysisInfo.bandIntensities.length];
			angleOffsets = new float[analysisInfo.bandIntensities.length];
		}

		if (Float.isNaN(angleOffset)) angleOffset = 0;

		if (analysisInfo.isAnyBeat()) {
			int highestChannelIndex = -1;
			double maxIntensity = 0;
			for (int i=0; i<analysisInfo.bandIntensities.length; i++) {
				if (analysisInfo.bandIntensities[i] > maxIntensity) {
					maxIntensity = analysisInfo.bandIntensities[i];
					highestChannelIndex = i;
				}
			}

			intensities[highestChannelIndex] +=
				Math.log(1+Math.abs(analysisInfo.bandIntensities[highestChannelIndex]) * velocityCoefficient.floatValue());
			//intensities[highestChannelIndex] = Math.max(0, Math.min(intensities[highestChannelIndex], 0.9f));

			overallBeatLevel += 0.3;
		}

		if (enableSoundEmitters.value()) {
			for (int i=0; i<intensities.length; i++) {
				float intensity = intensities[i];

				angleOffsets[i] += (float) (intensities[i] * 0.03 * (rotationDirectionCounter<0?-1:1));
				float angle = f((f(i)/intensities.length) * TWO_PI) + angleOffsets[i];

				float timeHueOffset = (frameCount % 1200) / 1200f;

				emitDirectional(
					f(0.5 + Math.cos(angle)*0.45 * (1- overallBeatLevel *0.25)),
					f(0.5 + Math.sin(angle)*0.45 * (1- overallBeatLevel *0.25)),

					(float)circleEmitterTarget.getX(), (float)circleEmitterTarget.getY(),

					(f(i)/intensities.length + timeHueOffset) * 3,

					intensity*0.008f,
					intensity * intensityCoefficient.floatValue()
				);

				intensities[i] *= intensityDecayRate.floatValue();
			}
		}

		overallBeatLevel *= 0.9;
		angleOffset += overallBeatLevel*.2 * (rotationDirectionCounter<0?-1:1);

		if (overallBeatLevel < 0.1) {
			rotationDirectionCounter *= -1;
		}

		fluidSolver.setFadeSpeed(f(fadeSpeedBase.floatValue() - overallBeatLevel * 0.2 * fadeSpeedCoefficient.floatValue()));

		// Add manual touches
		for (OscHelper.OscMultitouch.Touch touch : manualEmitters.getTouches().values()) {
			emitDirectional(
				(float) touch.getCurrentX(), (float) touch.getCurrentY(),
				(float) touch.getAngle(),
				(float) touch.getInitialY(),
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
		final Sound sound = new Sound();
		final AudioInput lineIn = sound.getLineIn();
		final AccessibleFFT fft = new AccessibleFFT(1024, lineIn.sampleRate());
		final BeatDetect beatDetect = new BeatDetect(1024, lineIn.sampleRate());
		{
			fft.logAverages(30, 1);
		}

		public AudioAnalysisInfo analyze() {
			beatDetect.detect(lineIn.mix);
			fft.forward(lineIn.mix);

			return new AudioAnalysisInfo(
				beatDetect.isHat(),
				beatDetect.isSnare(),
				beatDetect.isKick(),

				fft.getAverages()
			);
		}

		private class AccessibleFFT extends FFT {
			public AccessibleFFT(final int timeSize, final float sampleRate) {
				super(timeSize, sampleRate);
			}

			public float[] getAverages() {
				return averages;
			}
		}
	}

	public static class AudioAnalysisInfo {
		boolean hat;
		boolean snare;
		boolean kick;

		float[] bandIntensities;

		public AudioAnalysisInfo(
			final boolean hat,
			final boolean snare,
			final boolean kick,
			final float[] bandIntensities
		) {
			this.hat = hat;
			this.snare = snare;
			this.kick = kick;
			this.bandIntensities = bandIntensities;
		}

		public boolean isAnyBeat() {
			return hat || snare || kick;
		}

		public boolean isAllBeats() {
			return hat && snare && kick;
		}
	}
}
