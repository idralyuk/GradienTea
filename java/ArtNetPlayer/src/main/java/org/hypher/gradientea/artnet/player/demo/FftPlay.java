package org.hypher.gradientea.artnet.player.demo;

import java.util.Map;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class FftPlay {
	public static void main(String[] args) {
		new FftPlay();
	}

	private int sampleRate = 48000;

	public FftPlay() {
		double buffer[] = new double[(int) Math.pow(2, 14)];
		sin(buffer, 10, 1.0);
		sin(buffer, 2000, 0.5);
		sin(buffer, 12007, 0.5);
		sin(buffer, 18000, 0.5);
		sin(buffer, 8000, 0.5);

		System.out.println("Maxima: ");
		FFTSample sample = new FFTSample(sampleRate, buffer);
		for (Map.Entry<Integer, Double> e : sample.findMaxima().entrySet()) {
			if (e.getKey() < sampleRate/2) {
				System.out.printf("%02.2f: %02.2f\n", e.getKey(), e.getValue());
			}
		}

		System.out.println("Compressed to 40:");
		for (double v : sample.compressLogFreqNormalAmp(40, 0, 20000, 0.1, 1.0)) {
			System.out.printf("%02.2f ", v);
		}
	}

	private double computeFrequency(int fftOutWindowSize, int arrayIndex) {
		return (((double) sampleRate) / ((double) fftOutWindowSize)) * arrayIndex;
	}

	private void sin(double[] buffer, double frequency, double amplitude) {
		for (int i=0; i<buffer.length; i++) {
			buffer[i] += amplitude * Math.sin(i * (frequency/sampleRate) * Math.PI*2);
		}
	}

}
