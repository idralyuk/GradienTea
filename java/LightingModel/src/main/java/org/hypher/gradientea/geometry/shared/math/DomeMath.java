package org.hypher.gradientea.geometry.shared.math;

import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeMath {
	public static final double TWO_PI = Math.PI * 2;

	public static double normalizeAngle(double radians) {
		return radians % (2 * Math.PI);
	}

	/**
	 * From org.apache.commons.math.util.FastMath
	 */
	public static double atanh(double a) {
		boolean negative = false;
		if (a < 0) {
			negative = true;
			a = -a;
		}

		double absAtanh;
		if (a > 0.15) {
			absAtanh = 0.5 * Math.log((1 + a) / (1 - a));
		} else {
			final double a2 = a * a;
			if (a > 0.087) {
				absAtanh =
					a * (1 + a2 * (1.0 / 3.0 + a2 * (1.0 / 5.0 + a2 * (1.0 / 7.0 + a2 * (1.0 / 9.0 + a2 * (1.0 / 11.0 + a2 * (1.0 / 13.0 + a2 * (1.0 / 15.0 + a2 * (1.0 / 17.0)))))))));
			} else if (a > 0.031) {
				absAtanh =
					a * (1 + a2 * (1.0 / 3.0 + a2 * (1.0 / 5.0 + a2 * (1.0 / 7.0 + a2 * (1.0 / 9.0 + a2 * (1.0 / 11.0 + a2 * (1.0 / 13.0)))))));
			} else if (a > 0.003) {
				absAtanh = a * (1 + a2 * (1.0 / 3.0 + a2 * (1.0 / 5.0 + a2 * (1.0 / 7.0 + a2 * (1.0 / 9.0)))));
			} else {
				absAtanh = a * (1 + a2 * (1.0 / 3.0 + a2 * (1.0 / 5.0)));
			}
		}

		return negative ? -absAtanh : absAtanh;

	}

	public static float[] average(final List<float[]> sampleQueue) {
		float[] result = new float[sampleQueue.get(0).length];

		for (float[] sample : sampleQueue) {
			for (int i=0; i<result.length; i++) {
				result[i] += sample[i];
			}
		}

		for (int i=0; i<result.length; i++) {
			result[i] += result[i] / sampleQueue.size();
		}

		return result;
	}

	public static float f(double n) {
		return (float) n;
	}

	public static float f(int n) {
		return (float) n;
	}

	public static int clip(final int low, final int high, final int value) {
		return Math.min(high, Math.max(low, value));
	}

	public static double clip(final double low, final double high, final double value) {
		return Math.min(high, Math.max(low, value));
	}

	public static float clip(final float low, final float high, final float value) {
		return Math.min(high, Math.max(low, value));
	}

	public static double log(final float value, final int base) {
		return Math.log(value) / Math.log(base);
	}

	public static double exponentialScale(final double value, final double max) {
		return Math.pow(max+1, value / max) - 1;
	}

	public static double exponentialScale(final double value, final double max, final double exponentialWeight) {
		return (value + exponentialWeight*(Math.pow(max+1, value / max) - 1)) / (exponentialWeight+1);
	}

	public static int exponentialScale(final int value, final int max) {
		return (int) exponentialScale((double)value, (double)max);
	}

	public static float max(final float a, final float b, final float c) {
		return Math.max(Math.max(a, b), c);
	}

	public static float max(final float a, final float b, final float c, final float d) {
		return Math.max(max(a,b,c), d);
	}
}
