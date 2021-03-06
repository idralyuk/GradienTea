package org.hypher.gradientea.geometry.shared;

import com.google.common.collect.ComparisonChain;
import org.hypher.gradientea.geometry.shared.math.GeoPolarVector2;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Representation of a 3 vector for use in calculation dome geometry.
 * This is based on GeoVector3 from the Parallax library, which is a java port of ThreeJs.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GeoVector3 implements Serializable {
	public final static double equalityTolerance = 0.0000001;
	public final static double equalityPower = 1/equalityTolerance;
	public final static GeoVector3 origin = new GeoVector3();

	public final transient static Comparator<GeoVector3> xyzComparator = new Comparator<GeoVector3>() {
		public int compare(final GeoVector3 o1, final GeoVector3 o2) {
			return ComparisonChain.start()
				.compare(o1.getX(), o2.getX(), tolerantDoubleComparator)
				.compare(o1.getY(), o2.getY(), tolerantDoubleComparator)
				.compare(o1.getZ(), o2.getZ(), tolerantDoubleComparator)
				.result();
		}
	};

	public final transient static Comparator<GeoVector3> zxyComparator = new Comparator<GeoVector3>() {
		public int compare(final GeoVector3 o1, final GeoVector3 o2) {
			return ComparisonChain.start()
				.compare(o1.getZ(), o2.getZ(), tolerantDoubleComparator)
				.compare(o1.getX(), o2.getX(), tolerantDoubleComparator)
				.compare(o1.getY(), o2.getY(), tolerantDoubleComparator)
				.result();
		}
	};

	public final transient static Comparator<GeoVector3> yzxComparator = new Comparator<GeoVector3>() {
		public int compare(final GeoVector3 o1, final GeoVector3 o2) {
			return ComparisonChain.start()
				.compare(o1.getY(), o2.getY(), tolerantDoubleComparator)
				.compare(o1.getZ(), o2.getZ(), tolerantDoubleComparator)
				.compare(o1.getX(), o2.getX(), tolerantDoubleComparator)
				.result();
		}
	};

	public final transient static Comparator<GeoVector3> lengthComparator = new Comparator<GeoVector3>() {
		public int compare(final GeoVector3 o1, final GeoVector3 o2) {
			return ComparisonChain.start()
				.compare(o1.length(), o2.length(), tolerantDoubleComparator)
				.compare(o1, o2, xyzComparator)
				.result();
		}
	};

	public final transient static Comparator<Double> tolerantDoubleComparator = new Comparator<Double>() {
		@Override
		public int compare(final Double o1, final Double o2) {
			if (o1 == o2) return 0;
			if (o1 == null) return -1;
			if (o2 == null) return 1;
			if (Math.abs(o1 - o2) < equalityTolerance) return 0;

			return Double.compare(o1, o2);
		}
	};


	/**
	 * The X-coordinate
	 */
	protected double x;
	
	/**
	 * The Y-coordinate
	 */
	protected double y;
	
	/**
	 * The Y-coordinate
	 */
	protected double z;

	/**
	 * This default constructor will initialize vector (0, 0, 0), for internal use only. External users should
	 * use the {@link #origin} constant to refer to the origin.
	 */
	protected GeoVector3()
	{
		this(0, 0, 0);
	}

	/**
	 * This constructor will initialize vector (X, Y, Z) from the specified 
	 * X, Y, Z coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param z the Z coordinate
	 */
	public GeoVector3(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ()
	{
		return this.z;
	}

	public GeoVector3 withX(final double x) {
		return new GeoVector3(x, y, z);
	}

	public GeoVector3 withY(final double y) {
		return new GeoVector3(x, y, z);
	}

	public GeoVector3 withZ(final double z) {
		return new GeoVector3(x, y, z);
	}

	public GeoVector3 add(GeoVector3 v)
	{
		return new GeoVector3(
			x + v.x,
			y + v.y,
			z + v.z
		);
	}

	
	public GeoVector3 add(double s)
	{
		return new GeoVector3(
			x + s,
			y + s,
			z + s
		);
	}

	
	public GeoVector3 sub(GeoVector3 v)
	{
		return new GeoVector3(
			x - v.x,
			y - v.y,
			z - v.z
		);
	}

	
	public GeoVector3 multiply(GeoVector3 v)
	{
		return new GeoVector3(
			x * v.x,
			y * v.y,
			z * v.z
		);
	}

	
	public GeoVector3 multiply(double s)
	{
		return new GeoVector3(
			x * s,
			y * s,
			z * s
		);
	}


	public GeoVector3 divide(GeoVector3 v)
	{
		return new GeoVector3(
			x / v.x,
			y / v.y,
			z / v.z
		);
	}
	
	public GeoVector3 divide(double s)
	{
		return new GeoVector3(
			x / s,
			y / s,
			z / s
		);
	}

	
	public GeoVector3 negate()
	{
		return this.multiply(-1);
	}

	public GeoVector3 midpointBetween(GeoVector3 other, double fraction) {
		return add(other.sub(this).multiply(fraction));
	}

	public GeoVector3 midpointBetween(GeoVector3 other) {
		return midpointBetween(other, 0.5);
	}

	/**
	 * Computes the dot product of this vector and vector v1.
	 *
	 * @param v1
	 *            the other vector
	 * @return the dot product of this vector and v1
	 */
	public double dot(GeoVector3 v1)
	{
		return (this.x * v1.x + this.y * v1.y + this.z * v1.z);
	}

	/**
	 * Returns the squared length of this vector.
	 *
	 * @return the squared length of this vector
	 */
	public double lengthSq()
	{
		return dot(this);
	}

	/**
	 * Returns the length of this vector.
	 *
	 * @return the length of this vector
	 */
	public double length()
	{
		return Math.sqrt(lengthSq());
	}

	public double lengthManhattan()
	{
		return Math.abs(this.x) + Math.abs(this.y) + Math.abs(this.z);
	}

	/**
	 * Normalizes this vector in place.
	 */
	
	public GeoVector3 normalize()
	{
		double len = this.length();
		if (len > 0)
		{
			return this.multiply(1.0 / len);
		}
		else
		{
			return origin;
		}
	}

	public GeoVector3 withLength(double l)
	{
		normalize();
		return multiply(l);
	}

	public GeoVector3 lerp(GeoVector3 v1, double alpha)
	{
		return add(new GeoVector3(
			(v1.x - this.x) * alpha,
			(v1.y - this.y) * alpha,
			(v1.z - this.z) * alpha
		));
	}

	/**
	 * Sets this vector to be the vector cross product of vectors v1 and v2.
	 *
	 * @param v1
	 *            the first vector
	 * @param v2
	 *            the second vector
	 */
	public GeoVector3 cross(GeoVector3 v1, GeoVector3 v2)
	{
		double x = v1.y * v2.z - v1.z * v2.y;
		double y = v1.z * v2.x - v1.x * v2.z;
		double z = v1.x * v2.y - v1.y * v2.x;
		
		return new GeoVector3(x, y, z);
	}

	public GeoVector3 cross(GeoVector3 v)
	{
		return cross(this, v);
	}

	public double distanceToSquared(GeoVector3 v1)
	{
		double dx = this.getX() - v1.getX();
		double dy = this.getY() - v1.getY();
		double dz = this.getZ() - v1.getZ();
		return (dx * dx + dy * dy + dz * dz);
	}

	
	public double distanceTo(GeoVector3 v1)
	{
		return Math.sqrt(distanceToSquared(v1));
	}

	public double angleTo( GeoVector3 v )
	{
		return Math.acos( this.dot( v ) / this.length() / v.length() );
	}

	/**
	 * Returns true if all of the data members of v1 are equal to the
	 * corresponding data members in this GeoVector3.
	 *
	 * @param obj
	 *            the vector with which the comparison is made
	 * @return true or false
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof GeoVector3) {
			GeoVector3 other = (GeoVector3) obj;
			return Math.abs(this.x - other.x) < equalityTolerance
				&& Math.abs(this.y - other.y) < equalityTolerance
				&& Math.abs(this.z - other.z) < equalityTolerance;
		} else {
			return false;
		}
	}

	public boolean isZero()
	{
		return (this.lengthSq() < 0.0001 /* almostZero */);
	}

	
	public GeoVector3 clone()
	{
		return new GeoVector3(this.getX(), this.getY(), this.getZ());
	}

	
	public String toString()
	{
		return "(" +
			(Math.abs(x) < 0.000001 ? 0 : x) + ", " +
			(Math.abs(y) < 0.000001 ? 0 : y) + ", " +
			(Math.abs(z) < 0.000001 ? 0 : z) +
		")";
	}

	@Override
	public int hashCode() {
		return ((int) (this.x * equalityPower))*31 + ((int) (this.y * equalityPower))*31 + ((int) (this.z * equalityPower))*31;
	}

	/**
	 * Normalize the values of <em>this</em> vector, without making a copy. This should only be used when the vector
	 * needs to be normalized, but there are other references to it which need to refer to the update value.
	 */
	protected void normalizeInPlace() {
		GeoVector3 normal = normalize();

		this.x = normal.x;
		this.y = normal.y;
		this.z = normal.z;
	}

	public GeoPolarVector2 toPolar() {
		return new GeoPolarVector2(
			Math.atan2(y, x),
			Math.asin(z / length())
		);
	}

	public static class DistanceComparator implements Comparator<GeoVector3> {
		final GeoVector3 reference;

		public DistanceComparator(final GeoVector3 reference) {
			this.reference = reference;
		}

		@Override
		public int compare(
			final GeoVector3 o1, final GeoVector3 o2
		) {
			return Double.compare(
				o1.distanceTo(reference),
				o2.distanceTo(reference)
			);
		}
	}
}
