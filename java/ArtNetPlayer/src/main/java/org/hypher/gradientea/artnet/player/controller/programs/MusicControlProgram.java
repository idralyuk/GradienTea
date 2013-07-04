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

			for (int i=0; i<emitters.length; i++) {
				emitters[i] = new Emitter(emitterStart + i, (double)i/emitters.length);
			}
		}
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
		double rotationFlipCounter = -1;

		public Emitter(
			final int freqIndex,
			final double bandPosition
		) {
			this.freqIndex = freqIndex;
			this.hue = bandPosition;
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

			if (highest) {
				rotationFlipCounter = rotationStepCount;
			}

			if (currentPower > 0.01) {
				currentAngle += TWO_PI * .1 * currentPower * emitterRotationFraction.getValue();
			}
		}

		public void draw(final DomeFluidCanvas canvas) {
			if (currentPower > oscSensitivity.getValue()) {
				float effectiveHue = f(hue + hueOffset());

				double emitterRadius = oscEmitterRadius.getValue();

				final float fromX = f(0.5 + Math.cos(currentAngle) * emitterRadius);
				final float fromY = f(0.5 + Math.sin(currentAngle) * emitterRadius);
				final float velocity = f(oscVelocity.getValue()*0.01 + clip(0, 2, currentPower) * 0.02 * oscVelocity.getValue());
				final float intensity = f(oscIntensity.getValue() + clip(0, 2, currentPower) * 1000 * oscIntensity.getValue());

				if (emitterRadius > 0.25) {
					// Project inwards
					canvas.emitDirectional(fromX, fromY, 0.5f, 0.5f, effectiveHue, velocity, intensity);
				} else {
					// Project outwards
					canvas.emitDirectional(fromX, fromY, (float) currentAngle, effectiveHue, velocity, intensity);
				}
			}
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
}
