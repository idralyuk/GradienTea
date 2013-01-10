package org.hypher.gradientea.ui.client.simulator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.blimster.gwt.threejs.core.Vector3;
import org.hypher.gradientea.lightingmodel.shared.dome.DomeSpecification;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class DomeGeometry {
	protected static double t = ( 1 + Math.sqrt( 5 ) ) / 2;

	protected static Vector3[] icosahedronVerticies = new Vector3[] {
		Vector3.create( -1,  t,  0 ), Vector3.create(  1, t, 0 ), Vector3.create( -1, -t,  0 ), Vector3.create(  1, -t,  0 ),
		Vector3.create(  0, -1,  t ), Vector3.create(  0, 1, t ), Vector3.create(  0, -1, -t ), Vector3.create(  0,  1, -t ),
		Vector3.create(  t,  0, -1 ), Vector3.create(  t, 0, 1 ), Vector3.create( -t,  0, -1 ), Vector3.create( -t,  0,  1 )
	};

	protected static int[][] icosahedronFaces = new int[][] {
		new int[] { 0, 11,  5 }, new int[] { 0,  5,  1 }, new int[] {  0,  1,  7 }, new int[] {  0,  7, 10 }, new int[] {  0, 10, 11 },
		new int[] { 1,  5,  9 }, new int[] { 5, 11,  4 }, new int[] { 11, 10,  2 }, new int[] { 10,  7,  6 }, new int[] {  7,  1,  8 },
		new int[] { 3,  9,  4 }, new int[] { 3,  4,  2 }, new int[] {  3,  2,  6 }, new int[] {  3,  6,  8 }, new int[] {  3,  8,  9 },
		new int[] { 4,  9,  5 }, new int[] { 2,  4, 11 }, new int[] {  6,  2, 10 }, new int[] {  8,  6,  7 }, new int[] {  9,  8,  1 }
	};

	protected Set<DomeEdge> edges = Sets.newHashSet();
	protected Map<VectorKey, Vector3> verticies = Maps.newHashMap();
	protected List<DomeFace> faces = Lists.newArrayList();
	protected List<List<DomeFace>> rings = Lists.newArrayList();

	private final DomeSpecification specification;

	public DomeGeometry(
		DomeSpecification specification
	) {
		this.specification = specification;
		build();
	}

	private void build() {
		for (int[] faceIndicies : icosahedronFaces) {
			DomeFace face = new DomeFace(
				include(icosahedronVerticies[faceIndicies[0]]),
				include(icosahedronVerticies[faceIndicies[1]]),
				include(icosahedronVerticies[faceIndicies[2]])
			);

			if (specification.getFrequency() > 1) {
				faces.addAll(splitFace(face, specification.getFrequency()));
			} else {
				faces.add(face);
			}
		}

		for (Vector3 v : verticies.values()) {
			v.normalize();
			v.multiplyScalar(specification.getRadius());
		}

		for (DomeFace face : faces) {
			edges.addAll(face.getEdges());
		}
	}


	protected Collection<Vector3> getVertices() {
		return verticies.values();
	}

	protected Collection<DomeFace> getFaces() {
		return faces;
	}

	protected Collection<DomeEdge> getEdges() {
		return edges;
	}

	protected List<DomeFace> splitFace( DomeFace face, int frequency ) {
		List<DomeFace> faces = Lists.newArrayList();
		doSplit(faces, include(face.v1), include(face.v2), include(face.v3), frequency);
		return faces;
	}

	/**
	 *
	 *
	 */
	protected void doSplit(List<DomeFace> faces, Vector3 a, Vector3 b, Vector3 c, int divisons) {
		if (divisons == 1) {
			faces.add(new DomeFace(a, b, c));
		} else {
			//    b
			//  d   e
			// a     c

			Vector3 d = midpoint(a, b, divisons, 1);
			Vector3 e = midpoint(c, b, divisons, 1);

			List<Vector3> top = divide(d, e, divisons-1);
			List<Vector3> bottom = divide(a, c, divisons);

			for (int i=0; i<top.size(); i++) {

				//  1
				// 0 1
				faces.add(new DomeFace(
					bottom.get(i),
					top.get(i),
					bottom.get(i+1)
				));

				//  0 1
				// - 1
				if (i > 0) {
					faces.add(new DomeFace(
						bottom.get(i),
						top.get(i-1),
						top.get(i)
					));
				}
			}

			doSplit(faces, d, b, e, divisons - 1);
		}
	}


	protected Vector3 midpoint(Vector3 v1, Vector3 v2, int divisons, int index) {
		return include(
			Vector3.create()
				.add(v1, Vector3.create().sub(v2, v1).divideScalar(divisons).multiplyScalar(index))
		);
	}

	protected List<Vector3> divide( Vector3 v1, Vector3 v2, int divisons) {
		List<Vector3> points = Lists.newArrayList();
		points.add(include(v1));

		if (divisons > 1) {
			for (int i=1; i<divisons; i++) {
				points.add(midpoint(v1, v2, divisons, i));
			}
		}

		points.add(include(v2));

		return points;
	}

	/**
	 * Angle around the Y axis, counter-clockwise when looking from above.
	 *
	 * @param vector
	 * @return
	 */
	protected static double azimuth( Vector3 vector ) {
		return Math.atan2( vector.getZ(), -vector.getX() );
	}

	/**
	 * Angle above the XZ plane.
	 *
	 * @param vector
	 * @return
	 */
	protected static double inclination(  Vector3 vector ) {
		return Math.atan2( -vector.getY(), Math.sqrt( ( vector.getX() * vector.getX() ) + ( vector.getZ() * vector.getZ() ) ) );
	}

	protected Vector3 include(Vector3 v) {
		VectorKey key = new VectorKey(v);
		if (verticies.containsKey(key)) {
			return verticies.get(key);
		}

		verticies.put(key, v);

		return v;
	}

	public DomeSpecification getSpec() {
		return specification;
	}

	protected class VectorKey {
		Vector3 v;

		public VectorKey(Vector3 v) {
			this.v = v;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final VectorKey vectorKey = (VectorKey) o;

			return TestModelWidget.equal(v, vectorKey.v);
		}

		@Override
		public int hashCode() {
			int result;
			long temp;
			temp = v.getX() != +0.0d ? Double.doubleToLongBits(v.getX()) : 0L;
			result = (int) (temp ^ (temp >>> 32));
			temp = v.getY() != +0.0d ? Double.doubleToLongBits(v.getY()) : 0L;
			result = 31 * result + (int) (temp ^ (temp >>> 32));
			temp = v.getZ() != +0.0d ? Double.doubleToLongBits(v.getZ()) : 0L;
			result = 31 * result + (int) (temp ^ (temp >>> 32));
			return result;
		}
	}

	protected class DomeFace {
		protected Vector3 v1;
		protected Vector3 v2;
		protected Vector3 v3;

		public DomeFace(final Vector3 v1, final Vector3 v2, final Vector3 v3) {
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
		}

		public Vector3 getA() {
			return v1;
		}

		public Vector3 getB() {
			return v2;
		}

		public Vector3 getC() {
			return v3;
		}

		public Collection<? extends DomeEdge> getEdges() {
			return Arrays.asList(
				new DomeEdge(v1, v2),
				new DomeEdge(v2, v3),
				new DomeEdge(v3, v1)
			);
		}
	}

	protected class DomeEdge {
		protected Vector3 v1;
		protected Vector3 v2;

		public DomeEdge(final Vector3 v1, final Vector3 v2) {
			this.v1 = v1;
			this.v2 = v2;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final DomeEdge domeEdge = (DomeEdge) o;

			if (TestModelWidget.equal(v1, domeEdge.v1) && TestModelWidget.equal(v2, domeEdge.v2))
				return true;

			if (TestModelWidget.equal(v2, domeEdge.v1) && TestModelWidget.equal(v1, domeEdge.v2))
				return true;

			return false;
		}

		@Override
		public int hashCode() {
			int result;
			if (lessThan(v1, v2)) {
				result = TestModelWidget.vectorHash(v1);
				result = 31 * result + TestModelWidget.vectorHash(v2);
			} else {
				result = TestModelWidget.vectorHash(v2);
				result = 31 * result + TestModelWidget.vectorHash(v1);
			}

			return result;
		}

		protected boolean lessThan(Vector3 a, Vector3 b) {
			return ComparisonChain.start()
				.compare(a.getX(), b.getX())
				.compare(a.getY(), b.getY())
				.compare(a.getZ(), b.getZ())
				.result() < 0;
		}

		@Override
		public String toString() {
			return "DomeEdge{" +
				"v1=" + TestModelWidget.vectorString(v1) +
				", v2=" + TestModelWidget.vectorString(v2) +
				'}';
		}

		public Vector3 getV1() {
			return v1;
		}

		public Vector3 getV2() {
			return v2;
		}
	}

}
