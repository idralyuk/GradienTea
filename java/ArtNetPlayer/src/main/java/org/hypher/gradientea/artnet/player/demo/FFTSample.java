package org.hypher.gradientea.artnet.player.demo;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import java.util.Comparator;
import java.util.Map;

/**
 * Helper class for dealing with transformed samples.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class FFTSample {
	private final double sampleRate;
	private final double[] transformed;

	public FFTSample(double sampleRate, double[] data) {
		this.sampleRate = sampleRate;
		this.transformed = new double[data.length];

		int i=0;
		for (Complex complex : new FastFourierTransformer().transform(data)) {
			transformed[i++] = complex.abs();
		}
	}

	public double frequencyFor(int index) {
		return (((double) sampleRate) / ((double) transformed.length)) * index;
	}

	protected double[] compressLogFreqNormalAmp(int count, double startFreq, double endFreq, double lowCutoff, double highCutoff) {
		double[] normalized = linearNormalization(transformed);

		double[] sums = new double[count];
		int[] counts = new int[count];

		for (int i=1; i<normalized.length-1; i++) {
			double freq = frequencyFor(i);

			if (freq >= startFreq && freq <= endFreq) {
				int index = (int) ((Math.log(freq-startFreq) / Math.log(endFreq-startFreq)) * count);
				double amp = normalized[i];

				if (amp > lowCutoff && amp < highCutoff) {
					sums[index] += amp;
					counts[index] ++;
				}
			}
		}

		double[] averages = new double[count];
		for (int i=0; i<count; i++) {
			if (counts[i] > 0) {
				double avg = sums[i] / counts[i];
				averages[i] = avg;
			}
		}

		return linearNormalization(averages);
	}

	private double[] linearNormalization(double[] data) {
		double min = Double.MAX_VALUE, max = 0;
		for (int i=0; i<data.length; i++) {
			if (data[i] < min) min = data[i];
			if (data[i] > max) max = data[i];
		}

		double[] normalized = new double[data.length];
		for (int i=0; i<data.length; i++) {
			normalized[i] = (data[i] - min) / (max - min);
		}

		return normalized;
	}

	public Map<Integer, Double> findMaxima() {
		Map<Integer, Double> maxima = Maps.newLinkedHashMap();

		for (int i=1; i<transformed.length-1; i++) {
			if (transformed[i] > transformed[i-1] && transformed[i] > transformed[i+1]) {
				maxima.put(
					i,
					transformed[i]
				);
			}
		}

		return maxima;
	}

	public Map<Integer, Double> findMaxima(int limit) {
		Map<Integer, Double> ordered = Maps.newLinkedHashMap();

		for (Map.Entry<Integer, Double> entry : Ordering.from(new Comparator<Map.Entry<Integer, Double>>() {
				@Override
				public int compare(
					final Map.Entry<Integer, Double> o1, final Map.Entry<Integer, Double> o2
				) {
					return Double.compare(o1.getValue(), o2.getValue());
				}
			}).reverse().sortedCopy(findMaxima().entrySet()))
		{
			ordered.put(entry.getKey(), entry.getValue());
		}

		return ordered;
	}
}
