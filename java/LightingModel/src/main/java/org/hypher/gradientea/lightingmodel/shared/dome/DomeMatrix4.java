package org.hypher.gradientea.lightingmodel.shared.dome;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class implements an immutable 4x4 matrix.
 *
 * This matrix actually is array which is represented the following 
 * indexes:
 *
 * <pre>{@code
 * 0 4  8 12
 * 1 5  9 13
 * 2 6 10 14
 * 3 7 11 15
 * }</pre>
 *
 * @author Yona Appletree, thothbot
 *
 */
public class DomeMatrix4 implements Serializable {
	public final static DomeMatrix4 identity = new DomeMatrix4();

	protected double[] elements;
	
	/**
	 * Default constructor will make identity four-dimensional matrix, for internal use only. External users should
	 * use {@link #identity}.
	 *
	 * <pre>{@code
	 * 1 0 0 0
	 * 0 1 0 0
	 * 0 0 1 0
	 * 0 0 0 1
	 * }</pre>
	 */
	protected DomeMatrix4() {
		this(new double[] {
			1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1
		});
	}

	public DomeMatrix4(final double[] elements) {
		this.elements = elements;
	}

	/**
	 * This constructor will create four-dimensional matrix.
	 * This matrix uses input values n11-n44 and represented as
	 * the following:
	 *
	 * <pre>{@code
	 * n11 n12 n13 n14
	 * n21 n22 n23 n24
	 * n31 n32 n33 n34
	 * n41 n42 n43 n44
	 * }</pre>
	 */
	public DomeMatrix4(
		double n11, double n12, double n13, double n14,
		double n21, double n22, double n23, double n24,
		double n31, double n32, double n33, double n34,
		double n41, double n42, double n43, double n44
	) {
		this.elements = new double[] {
			n11, n12, n13, n14,
			n21, n22, n23, n24,
			n31, n32, n33, n34,
			n41, n42, n43, n44
		};
	}


	/**
	 * get the current Matrix which is represented
	 * by Array[16] which the following indexes:
	 *
	 * <pre>{@code
	 * 0 4  8 12
	 * 1 5  9 13
	 * 2 6 10 14
	 * 3 7 11 15
	 * }</pre>
	 *
	 * @return the Array
	 */
	public double[] getArray()
	{
		double[] copy = new double[16];
		for (int i=0; i<16; i++) {
			copy[i] = elements[i];
		}
		return copy;
	}

	/**
	 * Returns the vector of the first matrix column.
	 *
	 * @return the vector
	 */
	public GeoVector3 getColumnX()
	{
		return new GeoVector3(elements[0], elements[1], elements[2]);
	}

	/**
	 * Returns the vector of the second matrix column.
	 *
	 * @return the vector
	 */
	public GeoVector3 getColumnY()
	{
		return new GeoVector3(elements[4], elements[5], elements[6]);
	}

	/**
	 * Returns the vector of the third matrix column.
	 *
	 * @return the vector
	 */
	public GeoVector3 getColumnZ()
	{
		return new GeoVector3(elements[8], elements[9], elements[10]);
	}

	public DomeMatrix4 clone()
	{
		return new DomeMatrix4(
			elements[0], elements[4], elements[8],  elements[12],
			elements[1], elements[5], elements[9],  elements[13],
			elements[2], elements[6], elements[10], elements[14],
			elements[3], elements[7], elements[11], elements[15]
		);
	}
	
	/**
	 * Sets the value of this matrix to the scalar multiplication of the scale
	 * factor with this.
	 *
	 * @param s the scalar value
	 *
	 * @return the current matrix
	 */
	public DomeMatrix4 multiply(double s)
	{
		double[] result = new double[16];

		result[0] = (elements[0] * s);
		result[4] = (elements[4] * s);
		result[8] = (elements[8] * s);
		result[12] = (elements[12] * s);
		result[1] = (elements[1] * s);
		result[5] = (elements[5] * s);
		result[9] = (elements[9] * s);
		result[13] = (elements[13] * s);
		result[2] = (elements[2] * s);
		result[6] = (elements[6] * s);
		result[10] = (elements[10] * s);
		result[14] = (elements[14] * s);
		result[3] = (elements[3] * s);
		result[7] = (elements[7] * s);
		result[11] = (elements[11] * s);
		result[15] = (elements[15] * s);

		return new DomeMatrix4(result);
	}

	/**
	 * Sets the value of input vector to the matrix-vector multiplication of itself and
	 * vector v.
	 * {@code (this = this * v)}
	 *
	 * @param v the input vector
	 *
	 * @return the multiplication input vector
	 */
	public GeoVector3 multiplyVector3(GeoVector3 v)
	{
		double vx = v.getX();
		double vy = v.getY();
		double vz = v.getZ();
		double d = 1.0 / ( elements[3] * vx + elements[7] * vy + elements[11] * vz + elements[15] );

		return new GeoVector3(
			( elements[0] * vx + elements[4] * vy + elements[8]  * vz + elements[12] ) * d ,
			( elements[1] * vx + elements[5] * vy + elements[9]  * vz + elements[13] ) * d ,
			( elements[2] * vx + elements[6] * vy + elements[10] * vz + elements[14] ) * d 
		);
	}

	/**
	 * get the current matrix determinant.
	 *
	 * @return the matrix determinant
	 */
	public double determinant()
	{
		double n11 = elements[0], n12 = elements[4], n13 = elements[8],  n14 = elements[12];
		double n21 = elements[1], n22 = elements[5], n23 = elements[9],  n24 = elements[13];
		double n31 = elements[2], n32 = elements[6], n33 = elements[10], n34 = elements[14];
		double n41 = elements[3], n42 = elements[7], n43 = elements[11], n44 = elements[15];

		// cofactor exapaimsiom along first row

		return (
			n14 * n23 * n32 * n41-
				n13 * n24 * n32 * n41-
				n14 * n22 * n33 * n41+
				n12 * n24 * n33 * n41+

				n13 * n22 * n34 * n41-
				n12 * n23 * n34 * n41-
				n14 * n23 * n31 * n42+
				n13 * n24 * n31 * n42+

				n14 * n21 * n33 * n42-
				n11 * n24 * n33 * n42-
				n13 * n21 * n34 * n42+
				n11 * n23 * n34 * n42+

				n14 * n22 * n31 * n43-
				n12 * n24 * n31 * n43-
				n14 * n21 * n32 * n43+
				n11 * n24 * n32 * n43+

				n12 * n21 * n34 * n43-
				n11 * n22 * n34 * n43-
				n13 * n22 * n31 * n44+
				n12 * n23 * n31 * n44+

				n13 * n21 * n32 * n44-
				n11 * n23 * n32 * n44-
				n12 * n21 * n33 * n44+
				n11 * n22 * n33 * n44
		);
	}

	/**
	 * get position vector from the current matrix.
	 *
	 * @return the position vector
	 */
	public GeoVector3 getPosition()
	{
		return new GeoVector3(elements[12], elements[13], elements[14]);
	}
	
	public enum Euler {
		XYZ,
		YXZ,
		ZXY,
		ZYX,
		YZX,
		XZY,
		;
	}

	public DomeMatrix4 rotationFromEuler(GeoVector3 v)
	{
		return rotationFromEuler(v, Euler.XYZ);
	}

	public DomeMatrix4 rotationFromEuler(GeoVector3 v, Euler order)
	{
		double result[] = new double[16];
		for (int i=0; i<16; i++) {
			result[i] = elements[i];
		}
		
		double x = v.x, y = v.y, z = v.z;
		double a = Math.cos( x ), b = Math.sin( x );
		double c = Math.cos( y ), d = Math.sin( y );
		double e = Math.cos( z ), f = Math.sin( z );

		if ( order == Euler.XYZ )
		{
			double ae = a * e, af = a * f, be = b * e, bf = b * f;

			result[0] = c * e;
			result[4] = - c * f;
			result[8] = d;

			result[1] = af + be * d;
			result[5] = ae - bf * d;
			result[9] = - b * c;

			result[2] = bf - ae * d;
			result[6] = be + af * d;
			result[10] = a * c;
		}
		else if ( order == Euler.YXZ )
		{
			double ce = c * e, cf = c * f, de = d * e, df = d * f;

			result[0] = ce + df * b;
			result[4] = de * b - cf;
			result[8] = a * d;

			result[1] = a * f;
			result[5] = a * e;
			result[9] = - b;

			result[2] = cf * b - de;
			result[6] = df + ce * b;
			result[10] = a * c;

		}
		else if ( order == Euler.ZXY )
		{
			double ce = c * e, cf = c * f, de = d * e, df = d * f;

			result[0] = ce - df * b;
			result[4] = - a * f;
			result[8] = de + cf * b;

			result[1] = cf + de * b;
			result[5] = a * e;
			result[9] = df - ce * b;

			result[2] = - a * d;
			result[6] = b;
			result[10] = a * c;
		}
		else if ( order == Euler.ZYX )
		{
			double ae = a * e, af = a * f, be = b * e, bf = b * f;

			result[0] = c * e;
			result[4] = be * d - af;
			result[8] = ae * d + bf;

			result[1] = c * f;
			result[5] = bf * d + ae;
			result[9] = af * d - be;

			result[2] = - d;
			result[6] = b * c;
			result[10] = a * c;
		}
		else if ( order == Euler.YZX )
		{
			double ac = a * c, ad = a * d, bc = b * c, bd = b * d;

			result[0] = c * e;
			result[4] = bd - ac * f;
			result[8] = bc * f + ad;

			result[1] = f;
			result[5] = a * e;
			result[9] = - b * e;

			result[2] = - d * e;
			result[6] = ad * f + bc;
			result[10] = ac - bd * f;
		}
		else if ( order == Euler.XZY )
		{
			double ac = a * c, ad = a * d, bc = b * c, bd = b * d;

			result[0] = c * e;
			result[4] = - f;
			result[8] = d * e;

			result[1] = ac * f + bd;
			result[5] = a * e;
			result[9] = ad * f - bc;

			result[2] = bc * f - ad;
			result[6] = b * e;
			result[10] = bd * f + ac;
		}

		return new DomeMatrix4(result);
	}


	/**
	 * Matrix translation: moves every point a constant distance
	 * in a specified direction.
	 *
	 * @param v the vector which define direction
	 */
	public DomeMatrix4 translate(GeoVector3 v)
	{
		double[] result = getArray();
		
		double x = v.x, y = v.y, z = v.z;

		result[12] = elements[0] * x + elements[4] * y + elements[8] * z + elements[12];
		result[13] = elements[1] * x + elements[5] * y + elements[9] * z + elements[13];
		result[14] = elements[2] * x + elements[6] * y + elements[10] * z + elements[14];
		result[15] = elements[3] * x + elements[7] * y + elements[11] * z + elements[15];
		
		return new DomeMatrix4(result);
	}

	/**
	 * Rotate the current matrix on X axis by defined angle.
	 *
	 * @param angle the angle value
	 */
	public DomeMatrix4 rotateX(double angle)
	{
		double[] result = getArray();
		
		double m12 = elements[4];
		double m22 = elements[5];
		double m32 = elements[6];
		double m42 = elements[7];
		double m13 = elements[8];
		double m23 = elements[9];
		double m33 = elements[10];
		double m43 = elements[11];

		double c = Math.cos(angle);
		double s = Math.sin(angle);

		result[4] = c * m12 + s * m13;
		result[5] = c * m22 + s * m23;
		result[6] = c * m32 + s * m33;
		result[7] = c * m42 + s * m43;

		result[8] = c * m13 - s * m12;
		result[9] = c * m23 - s * m22;
		result[10] = c * m33 - s * m32;
		result[11] = c * m43 - s * m42;
		
		return new DomeMatrix4(result);
	}

	/**
	 * Rotate the current matrix on Y axis by defined angle.
	 *
	 * @param angle the angle value
	 */
	public DomeMatrix4 rotateY(double angle)
	{
		double[] result = getArray();
		
		double m11 = elements[0];
		double m21 = elements[1];
		double m31 = elements[2];
		double m41 = elements[3];
		double m13 = elements[8];
		double m23 = elements[9];
		double m33 = elements[10];
		double m43 = elements[11];

		double c = Math.cos(angle);
		double s = Math.sin(angle);

		result[0] = c * m11 + s * m13;
		result[1] = c * m21 + s * m23;
		result[2] = c * m31 + s * m33;
		result[3] = c * m41 + s * m43;

		result[8] = c * m13 - s * m11;
		result[9] = c * m23 - s * m21;
		result[10] = c * m33 - s * m31;
		result[11] = c * m43 - s * m41;
		
		return new DomeMatrix4(result);
	}

	/**
	 * Rotate the current matrix on Z axis by defined angle.
	 *
	 * @param angle the angle value
	 */
	public DomeMatrix4 rotateZ(double angle)
	{
		double[] result = getArray();
		
		double m11 = elements[0];
		double m21 = elements[1];
		double m31 = elements[2];
		double m41 = elements[3];
		double m12 = elements[4];
		double m22 = elements[5];
		double m32 = elements[6];
		double m42 = elements[7];

		double c = Math.cos(angle);
		double s = Math.sin(angle);

		result[0] = c * m11 + s * m12;
		result[1] = c * m21 + s * m22;
		result[2] = c * m31 + s * m32;
		result[3] = c * m41 + s * m42;

		result[4] = c * m12 - s * m11;
		result[5] = c * m22 - s * m21;
		result[6] = c * m32 - s * m31;
		result[7] = c * m42 - s * m41;
		
		return new DomeMatrix4(result);
	}

	/**
	 * Rotate the current matrix on axis by defined angle.
	 *
	 * @param axis the axis on which rotate the matrix
	 * @param angle the angle value
	 */
	public DomeMatrix4 rotateByAxis(GeoVector3 axis, double angle)
	{
		double x = axis.getX(), y = axis.getY(), z = axis.getZ();

		// optimize by checking axis
		if (x == 1 && y == 0 && z == 0)
		{
			return this.rotateX(angle);
		}
		else if (x == 0 && y == 1 && z == 0)
		{
			return this.rotateY(angle);
		}
		else if (x == 0 && y == 0 && z == 1)
		{
			return this.rotateZ(angle);
		}

		double n = Math.sqrt(x * x + y * y + z * z);

		x /= n;
		y /= n;
		z /= n;

		double xx = x * x, yy = y * y, zz = z * z;
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		double oneMinusCosine = 1 - c;
		double xy = x * y * oneMinusCosine;
		double xz = x * z * oneMinusCosine;
		double yz = y * z * oneMinusCosine;
		double xs = x * s;
		double ys = y * s;
		double zs = z * s;

		double r11 = xx + (1 - xx) * c;
		double r21 = xy + zs;
		double r31 = xz - ys;
		double r12 = xy - zs;
		double r22 = yy + (1 - yy) * c;
		double r32 = yz + xs;
		double r13 = xz + ys;
		double r23 = yz - xs;
		double r33 = zz + (1 - zz) * c;

		double m11 = elements[0], m21 = elements[1], m31 = elements[2], m41 = elements[3];
		double m12 = elements[4], m22 = elements[5], m32 = elements[6], m42 = elements[7];
		double m13 = elements[8], m23 = elements[9], m33 = elements[10], m43 = elements[11];

		double[] result = getArray();
		
		result[0] = r11 * m11 + r21 * m12 + r31 * m13;
		result[1] = r11 * m21 + r21 * m22 + r31 * m23;
		result[2] = r11 * m31 + r21 * m32 + r31 * m33;
		result[3] = r11 * m41 + r21 * m42 + r31 * m43;

		result[4] = r12 * m11 + r22 * m12 + r32 * m13;
		result[5] = r12 * m21 + r22 * m22 + r32 * m23;
		result[6] = r12 * m31 + r22 * m32 + r32 * m33;
		result[7] = r12 * m41 + r22 * m42 + r32 * m43;

		result[8] = r13 * m11 + r23 * m12 + r33 * m13;
		result[9] = r13 * m21 + r23 * m22 + r33 * m23;
		result[10] = r13 * m31 + r23 * m32 + r33 * m33;
		result[11] = r13 * m41 + r23 * m42 + r33 * m43;
		
		return new DomeMatrix4(result);
	}

	/**
	 * Scale the current matrix.
	 *
	 * @param v the vector to scale the current matrix
	 */
	public DomeMatrix4 scale(GeoVector3 v)
	{
		double x = v.x, y = v.y, z = v.z;

		double[] result = getArray();

		result[0] =  (elements[0]  * x);
		result[1] =  (elements[1]  * x);
		result[2] =  (elements[2]  * x);
		result[3] =  (elements[3]  * x);

		result[4] =  (elements[4]  * y);
		result[5] =  (elements[5]  * y);
		result[6] =  (elements[6]  * y);
		result[7] =  (elements[7]  * y);

		result[8] =  (elements[8]  * z);
		result[9] =  (elements[9]  * z);
		result[10] = (elements[10] * z);
		result[11] = (elements[11] * z);

		return new DomeMatrix4(result);
	}

	public double getMaxScaleOnAxis()
	{
		double scaleXSq = elements[0] * elements[0] + elements[1] * elements[1] + elements[2] * elements[2];
		double scaleYSq = elements[4] * elements[4] + elements[5] * elements[5] + elements[6] * elements[6];
		double scaleZSq = elements[8] * elements[8] + elements[9]	* elements[9] + elements[10] * elements[10];

		return Math.sqrt( Math.max( scaleXSq, Math.max( scaleYSq, scaleZSq ) ) );
	}

	/**
	 * This method will make translation matrix using X, Y,Z coordinates
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param z the Z coordinate
	 *
	 * @return the current matrix
	 */
	public static DomeMatrix4 makeTranslation(double x, double y, double z)
	{
		return new DomeMatrix4(
			1, 0, 0, x,
			0, 1, 0, y,
			0, 0, 1, z,
			0, 0, 0, 1
		);
	}

	/**
	 * The method will make rotation matrix on X-axis using defining angle theta.
	 *
	 * @param theta the angle to make rotation matrix
	 *
	 * @return the current matrix
	 */
	public static DomeMatrix4 makeRotationX(double theta)
	{
		double c = Math.cos(theta), s = Math.sin(theta);

		return new DomeMatrix4(
			1, 0, 0, 0,
			0, c,-s, 0,
			0, s, c, 0,
			0, 0, 0, 1
		);
	}

	/**
	 * The method will make rotation matrix on Y-axis using defining angle theta.
	 *
	 * @param theta the angle to make rotation matrix
	 *
	 * @return the current matrix
	 */
	public static DomeMatrix4 makeRotationY(double theta)
	{
		double c = Math.cos(theta), s = Math.sin(theta);

		return new DomeMatrix4(
			c,  0, s, 0,
			0,  1, 0, 0,
			-s, 0, c, 0,
			0, 0, 0, 1
		);
	}

	/**
	 * The method will make rotation matrix on Z-axis using defining angle theta.
	 *
	 * @param theta the angle to make rotation matrix
	 *
	 * @return the current matrix
	 */
	public static DomeMatrix4 makeRotationZ(double theta)
	{
		double c = Math.cos(theta), s = Math.sin(theta);

		return new DomeMatrix4(
			c, -s, 0, 0,
			s,  c, 0, 0,
			0,  0, 1, 0,
			0,  0, 0, 1
		);
	}

	/**
	 * The method will make rotation matrix on XYZ-axis using defining angle theta.
	 *
	 * @param axis  the axis on which rotate the matrix
	 * @param angle the angle to make rotation matrix
	 *
	 * @return the current matrix
	 */
	public static DomeMatrix4 makeRotationAxis(GeoVector3 axis, double angle)
	{
		// Based on http://www.gamedev.net/reference/articles/article1199.asp

		double c = Math.cos(angle);
		double s = Math.sin(angle);
		double t = 1.0 - c;
		double x = axis.getX(), y = axis.getY(), z = axis.getZ();
		double tx = t * x, ty = t * y;

		return new DomeMatrix4(
			(tx * x + c),     (tx * y - s * z), (tx * z + s * y), 0,
			(tx * y + s * z),     (ty * y + c), (ty * z - s * x), 0,
			(tx * z - s * y), (ty * z + s * x),	(t * z * z + c),  0,
			0,                0,               0,  1
		);
	}

	/**
	 * Make a scaled matrix on the X, Y, Z coordinates.
	 *
	 * @param x the X-coordinate
	 * @param y the Y-coordinate
	 * @param z the Z-coordinate
	 *
	 * @return the current matrix
	 */
	public static DomeMatrix4 makeScale(double x, double y, double z)
	{
		return new DomeMatrix4(
			x, 0, 0, 0,
			0, y, 0, 0,
			0, 0, z, 0,
			0, 0, 0, 1
		);
	}

	/**
	 * Creates a frustum matrix.
	 */
	public static DomeMatrix4 makeFrustum(double left, double right, double bottom, double top, double near, double far)
	{
		double[] result = new double[16];

		double x = 2.0 * near / ( right - left );
		double y = 2.0 * near / ( top - bottom );

		double a = ( right + left ) / ( right - left );
		double b = ( top + bottom ) / ( top - bottom );
		double c = - ( far + near ) / ( far - near );
		double d = - 2.0 * far * near / ( far - near );

		result[0] = x;  result[4] = 0;  result[8] = a;    result[12] = 0;
		result[1] = 0;  result[5] = y;   result[9] = b;   result[13] = 0;
		result[2] = 0;  result[6] = 0;  result[10] = c;   result[14] = d;
		result[3] = 0;  result[7] = 0;  result[11] = - 1; result[15] = 0;

		return new DomeMatrix4(result);
	}

	/**
	 * Making Perspective Projection Matrix
	 *
	 * @param fov    the field Of View
	 * @param aspect the aspect ration
	 * @param near   the near value
	 * @param far    the far value
	 *
	 * @return the current Projection Matrix
	 */
	public static DomeMatrix4 makePerspective(double fov, double aspect, double near, double far)
	{
		double ymax = near * Math.tan( fov * Math.PI / 360.0 );
		double ymin = - ymax;
		double xmin = ymin * aspect;
		double xmax = ymax * aspect;

		return makeFrustum( xmin, xmax, ymin, ymax, near, far );
	}

	/**
	 * Making Orthographic Projection Matrix
	 *
	 * @return the current Projection Matrix
	 */
	public static DomeMatrix4 makeOrthographic(double left, double right, double top, double bottom, double near, double far)
	{
		double[] result = new double[16];
		
		double w = right - left;
		double h = top - bottom;
		double p = far - near;

		double x = ( right + left ) / w;
		double y = ( top + bottom ) / h;
		double z = ( far + near )   / p;

		result[0] = 2.0 / w; result[4] = 0.0;     result[8] = 0.0;       result[12] = -x;
		result[1] = 0.0;     result[5] = 2.0 / h; result[9] = 0.0;       result[13] = -y;
		result[2] = 0.0;     result[6] = 0.0;     result[10] = -2.0 / p; result[14] = -z;
		result[3] = 0.0;     result[7] = 0.0;     result[11] = 0.0;      result[15] = 1.0;

		return new DomeMatrix4(result);
	}

	/**
	 * get matrix information by printing list of matrix's elements.
	 */
	public String toString()
	{
		return "[" +
			elements[0] + "," + elements[4] + "," + elements[8]  + "," + elements[12] + "," +
			elements[1] + "," + elements[5] + "," + elements[9]  + "," + elements[13] + "," +
			elements[2] + "," + elements[6] + "," + elements[10] + "," + elements[14] + "," +
			elements[3] + "," + elements[7] + "," + elements[11] + "," + elements[15]
			+ "]";
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final DomeMatrix4 that = (DomeMatrix4) o;

		if (!Arrays.equals(elements, that.elements)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(elements);
	}
}
