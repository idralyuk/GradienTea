package org.hypher.gradientea.artnet.player.controller.programs;

import com.google.common.base.Optional;
import ddf.minim.AudioInput;
import ddf.minim.Sound;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import org.hypher.gradientea.artnet.player.controller.DomeFluidCanvas;
import org.hypher.gradientea.artnet.player.controller.OscConstants;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.math.DomeMath;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.hypher.gradientea.geometry.shared.math.DomeMath.*;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class MusicControlProgram extends BaseDomeProgram {
	private AudioAnalyzer audioAnalyzer;

	private double currentBandsFraction;
	private double currentFreqLowFraction;
	private double currentFreqHighFraction;

	private Emitter[] emitters;
	private BandHistogram bandHistogram;

	private OscHelper.OscDouble oscBandsFraction = OscHelper.doubleValue(
		OscConstants.Control.Music.FREQ_BANDS, 0, 1, 0.3
	);

	private OscHelper.OscDouble oscFreqLowFraction = OscHelper.doubleValue(
		OscConstants.Control.Music.FREQ_LOW, 0, 1, 0
	);

	private OscHelper.OscDouble oscFreqHighFraction = OscHelper.doubleValue(
		OscConstants.Control.Music.FREQ_HIGH, 0, 1, .8
	);

	private OscHelper.OscDouble oscSensitivity = OscHelper.doubleValue(
		OscConstants.Control.Music.SENSITIVITY, 1, 0, .4
	);

	private OscHelper.OscDouble oscVelocity = OscHelper.doubleValue(
		OscConstants.Control.Music.VELOCITY, 0, 3.0, 0.8
	);

	private OscHelper.OscDouble oscIntensity = OscHelper.doubleValue(
		OscConstants.Control.Music.INTENSITY, 0, 1.0, 0.5
	);

	private OscHelper.OscDouble oscSustain = OscHelper.doubleValue(
		OscConstants.Control.Music.SUSTAIN, 0, 1.0, 0.7
	);

	private OscHelper.OscDouble oscEmitterRadius = OscHelper.doubleValue(
		OscConstants.Control.Music.EMITTER_RADIUS, 0, 0.5, 0.05
	);

	private OscHelper.OscDouble colorRotationFraction = OscHelper.doubleValue(
		OscConstants.Control.Music.COLOR_ROTATION, 0, 1.0, 0.3
	);

	private OscHelper.OscDouble emitterRotationFraction = OscHelper.doubleValue(
		OscConstants.Control.Music.EMITTER_ROTATION, 0, 1.0, 0.1
	);


	public MusicControlProgram() {
		super(ProgramId.MUSIC);
	}

	@Override
	protected void initialize() {
		audioAnalyzer = new AudioAnalyzer();
	}

	@Override
	public void start() { }

	@Override
	public void update() {
		setBands(
			oscBandsFraction.getValue(),
			oscFreqLowFraction.getValue(),
			oscFreqHighFraction.getValue()
		);

		final AudioAnalysisInfo analysisInfo = audioAnalyzer.analyze();

		int highestChannel = analysisInfo.highestChannel();

		for (Emitter emitter : emitters) {
			emitter.update(analysisInfo.bandIntensities[emitter.freqIndex], emitter.freqIndex == highestChannel);
			emitter.draw(fluidCanvas());
		}
	}

	private void setBands(
		final double newBandsFraction,
		final double newFreqLowFraction,
		final double newFreqHighFraction
	) {
		if (emitters == null ||
			currentBandsFraction != newBandsFraction ||
			currentFreqLowFraction != newFreqLowFraction ||
			currentFreqHighFraction != newFreqHighFraction
		) {
			currentBandsFraction = newBandsFraction;
			currentFreqLowFraction = newFreqLowFraction;
			currentFreqHighFraction = newFreqHighFraction;

			audioAnalyzer.fft.logAverages(10, (int) (1 + newBandsFraction*3));

			int freqCount = audioAnalyzer.fft.avgSize();
			int emitterStart = (int) (freqCount * currentFreqLowFraction) + 1;
			int emitterEnd = (int) (freqCount * currentFreqHighFraction);

			if (emitterStart > freqCount - 2) {
				emitterStart = freqCount -2;
			}

			if (emitterEnd <= emitterStart) {
				emitterEnd = emitterStart + 1;
			}

			int emitterCount = emitterEnd - emitterStart;

			emitters = new Emitter[emitterCount];
			bandHistogram = null;

			for (int i=0; i<emitters.length; i++) {
				emitters[i] = new Emitter(emitterStart + i, (double)i/emitters.length);
			}
		}
	}

	@Override
	public void drawOverlay(final Graphics2D g, final int width, final int height) {

		// Draw each emitter
		if (emitters != null) {
			for (Emitter emitter : emitters) {
				emitter.drawOverlay(g, width, height);
			}
		}

		if (bandHistogram == null || bandHistogram.width != width) {
			bandHistogram = new BandHistogram(width, (int) (height * 0.2));
		}

		bandHistogram.update(emitters);

		bandHistogram.draw(g, width / 2, height - bandHistogram.height);

		super.drawOverlay(g, width, height);
	}

	@Override
	public boolean isFocusDesired() {
		return false;
	}

	@Override
	public void stop() { }

	protected class Emitter {
		final int freqIndex;

		double hue;
		double bandPosition;
		final double initialAngle;
		double currentAngle;
		double currentPower;

		boolean clockwiseRotation;
		int rotationFlipCounter = -1;

		public Emitter(
			final int freqIndex,
			final double bandPosition
		) {
			this.freqIndex = freqIndex;
			this.hue = bandPosition*3;
			this.bandPosition = bandPosition;
			this.currentAngle = this.initialAngle = bandPosition * TWO_PI;
			this.clockwiseRotation = true;
		}

		public void update(float intensity, boolean highest) {
			updateRotation(highest);
			updatePower(intensity, highest);
		}

		private void updatePower(final float intensity, final boolean highest) {
			if (highest) {
				currentPower = Math.max(currentPower, DomeMath.log(intensity+1, 20));
			} else {
				currentPower *= oscSustain.getValue();
			}
		}

		private void updateRotation(final boolean highest) {
			double rotationStepCount = emitterRotationFraction.getValue() * 100;

			if (--rotationFlipCounter == 0) {
				clockwiseRotation = !clockwiseRotation;
			}

			if (fractionalPower() > oscSensitivity.getValue()) {
				rotationFlipCounter =  (int) rotationStepCount;
			}

			if (currentPower > 0.01) {
				currentAngle += TWO_PI * .1 * currentPower * emitterRotationFraction.getValue() * (clockwiseRotation?1:-1);
			}
		}

		public void draw(final DomeFluidCanvas canvas) {
			if (fractionalPower() > oscSensitivity.getValue()) {
				float effectiveHue = effectiveHue();

				final float fromX = getFractionalX();
				final float fromY = getFractionalY();
				final float velocity = f(oscVelocity.getValue()*0.01 + fractionalPower() * 0.02 * oscVelocity.getValue());
				final float intensity = f(oscIntensity.getValue() + fractionalPower() * 1000 * oscIntensity.getValue());

				if (oscEmitterRadius.getValue() > 0.25) {
					// Project inwards
					canvas.emitDirectional(fromX, fromY, 0.5f, 0.5f, effectiveHue, velocity, intensity);
				} else {
					// Project outwards
					canvas.emitDirectional(fromX, fromY, (float) currentAngle, effectiveHue, velocity, intensity);
				}
			}
		}

		private float effectiveHue() {
			return f(hue + hueOffset());
		}

		private float getFractionalY() {
			return f(0.5 + Math.sin(currentAngle) * oscEmitterRadius.getValue());
		}

		private float getFractionalX() {
			return f(0.5 + Math.cos(currentAngle) * oscEmitterRadius.getValue());
		}

		public void drawOverlay(final Graphics2D g, final int width, final int height) {
			if (fractionalPower() > 0.01) {
				double radiusMultiplier = Math.min(width, height) * 0.05;

				int centerX = (int) (getFractionalX() * width);
				int centerY = (int) (getFractionalY() * height);

				int radius = (int) (fractionalPower() * radiusMultiplier);
				int maxRadius = (int) radiusMultiplier;

				int sensitivityRadius = (int) (oscSensitivity.getValue() * radiusMultiplier);

				g.setStroke(new BasicStroke(2f));
				g.setColor(Color.getHSBColor(effectiveHue(), 1f, 1f));
				g.fillOval(centerX-radius, centerY-radius, radius*2, radius*2);

				g.setStroke(new BasicStroke(1f));
				g.setColor(Color.getHSBColor(effectiveHue(), 1f, 0.5f));
				g.drawOval(centerX-sensitivityRadius, centerY-sensitivityRadius, sensitivityRadius*2, sensitivityRadius*2);
			}
		}

		private float fractionalPower() {
			return (float) DomeMath.clip(0, 2, currentPower) / 2;
		}
	}

	private double hueOffset() {
		if (colorRotationFraction.getValue() < 0.001) {
			return 0;
		} else {
			double rotationDurationMs = (1 - colorRotationFraction.getValue()) * 100000;

			return (System.currentTimeMillis() % rotationDurationMs) / rotationDurationMs;
		}
	}


	public static class AudioAnalyzer {
		final Sound sound;
		final AudioInput lineIn;
		final AccessibleFFT fft;
		final BeatDetect beatDetect;

		{
			sound = new Sound();
			final Optional<Mixer> desiredInputMixer = getDesiredInputMixer();
			if (desiredInputMixer.isPresent()) {
				System.out.println("Using Audio Mixer: " + desiredInputMixer.get().getMixerInfo());
				sound.setInputMixer(desiredInputMixer.get());
			} else {
				System.out.println("Using Default Audio Mixer");
			}

			lineIn = sound.getLineIn(Sound.MONO);
			fft = new AccessibleFFT(1024, lineIn.sampleRate());
			beatDetect = new BeatDetect(1024, lineIn.sampleRate());
			fft.logAverages(30, 1);
		}

		/**
		 * @return The desired mixer for dome audio input. {@link Optional#absent()} indicates no particular preference
		 * in mixer, allowing the implementation to use the default.
		 */
		public static Optional<Mixer> getDesiredInputMixer() {
			String[] DESIRED_MIXERS = new String[]{
				"fast", // FastTrack USB Audio
				"display audio", // Display Input
				"usb" // Any other USB audio device
			};

			for (String desiredMixerName : DESIRED_MIXERS) {
				for (Mixer.Info mixerInfo :  AudioSystem.getMixerInfo()) {
					if (mixerInfo.toString().toLowerCase().contains(desiredMixerName)) {
						return Optional.of(AudioSystem.getMixer(mixerInfo));
					}
				}
			}

			return Optional.absent();
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

		public int highestChannel() {
			int highestChannelIndex = 0;
			float highestChannelValue = 0;

			for (int i=0; i<bandIntensities.length; i++) {
				if (bandIntensities[i] > highestChannelValue) {
					highestChannelValue = bandIntensities[i];
					highestChannelIndex = i;
				}
			}

			return highestChannelIndex;
		}
	}

	public static class BandHistogram {
		int width, height;
		BufferedImage bufferImage;
		int currentPos = 0;
		int sampleWidth = 2;

		public BandHistogram(final int width, final int height) {
			this.width = width;
			this.height = height;

			bufferImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		}

		public void update(Emitter[] emitters) {
			int bandHeight = height / emitters.length;

			Graphics2D g = (Graphics2D) bufferImage.getGraphics();

			g.clearRect(currentPos, 0, sampleWidth, height);

			int y = 0;
			for (Emitter emitter : emitters) {
				g.setColor(Color.getHSBColor(emitter.effectiveHue(), 1f, emitter.fractionalPower()));
				g.fillRect(currentPos, y, sampleWidth, bandHeight);

				y += bandHeight;
			}

			currentPos += sampleWidth;
			if (currentPos >= width) {
				currentPos = 0;
			}
		}

		public void draw(Graphics2D g, int x, int y) {
			g.drawImage(bufferImage, x - currentPos, y, x, y+height, 0, 0, currentPos, height, null);

			if (currentPos < x) {
				g.drawImage(bufferImage, x - width, y, x - currentPos, y+height, currentPos, 0, width, height, null);
			}
		}
	}
}
