package org.hypher.gradientea.geometry.shared.math;

import static java.lang.Math.sin;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.TWO_PI;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.atanh;

/**
 * Represents a point on the dome in polar coordinates.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GeoPolarVector2 {
	/**
	 * The angle of the point on the XZ plane.
	 */
	private double theta;

	/**
	 * The angle of the point on the YZ plane.
	 */
	private double phi;

	public GeoPolarVector2(final double theta, final double phi) {
		this.theta = theta;
		this.phi = phi;
	}

	public double mercatorX(double width, double height) {
		//(mapWidth / 360.0) * correctOption.location.coordinates.longitude,

		return (width / TWO_PI) * theta;
	}

	public double mercatorY(double width, double height) {
		//mapHeight * atanh(sin(degreesToRadians(correctOption.location.coordinates.latitude))) / (2.0 * M_PI)

		return height * atanh(sin(phi)) / TWO_PI;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	@Override
	public String toString() {
		return "GeoPolarVector2{" +
			"theta=" + (theta / Math.PI) + " π" +
			", phi=" + (phi / Math.PI) + " π" +
			'}';
	}


	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	public double getTheta() {
		return theta;
	}

	public double getPhi() {
		return phi;
	}

	//endregion
}
