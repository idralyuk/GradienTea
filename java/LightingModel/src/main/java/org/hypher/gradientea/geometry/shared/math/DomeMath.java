package org.hypher.gradientea.geometry.shared.math;

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
}
