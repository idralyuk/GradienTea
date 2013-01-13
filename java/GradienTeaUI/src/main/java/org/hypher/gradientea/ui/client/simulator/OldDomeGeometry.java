package org.hypher.gradientea.ui.client.simulator;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.blimster.gwt.threejs.core.Matrix4;
import net.blimster.gwt.threejs.core.Vector3;
import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class OldDomeGeometry {
//	protected static double t = ( 1 + Math.sqrt( 5 ) ) / 2;
//
//	protected static Vector3[] icosahedronVerticies = Vector3.create[] {
//		Vector3.create( -1,  t,  0 ), Vector3.create(  1, t, 0 ), Vector3.create( -1, -t,  0 ), Vector3.create(  1, -t,  0 ),
//		Vector3.create(  0, -1,  t ), Vector3.create(  0, 1, t ), Vector3.create(  0, -1, -t ), Vector3.create(  0,  1, -t ),
//		Vector3.create(  t,  0, -1 ), Vector3.create(  t, 0, 1 ), Vector3.create( -t,  0, -1 ), Vector3.create( -t,  0,  1 )
//	};
//
//	protected static int[][] icosahedronFaces = new int[][] {
//		new int[] { 0, 11,  5 }, new int[] { 0,  5,  1 }, new int[] {  0,  1,  7 }, new int[] {  0,  7, 10 }, new int[] {  0, 10, 11 },
//		new int[] { 1,  5,  9 }, new int[] { 5, 11,  4 }, new int[] { 11, 10,  2 }, new int[] { 10,  7,  6 }, new int[] {  7,  1,  8 },
//		new int[] { 4,  9,  5 }, new int[] { 2,  4, 11 }, new int[] {  6,  2, 10 }, new int[] {  8,  6,  7 }, new int[] {  9,  8,  1 },
//		new int[] { 3,  9,  4 }, new int[] { 3,  4,  2 }, new int[] {  3,  2,  6 }, new int[] {  3,  6,  8 }, new int[] {  3,  8,  9 },
//	};


	protected static Vector3[] icosahedronVerticies = new Vector3[12];
	static {
		icosahedronVerticies[0] = Vector3.create(0, 1, 0);

		for(int i = 0; i < 5; i++) {
			double alpha, beta, x, y, z;
			alpha = i * (2 * Math.PI) / 5;
			beta = 1.1072;
			y = Math.cos(beta);
			x = Math.cos(alpha) * Math.sin(beta);
			z = Math.sin(alpha) * Math.sin(beta);
			icosahedronVerticies[i + 1] = Vector3.create(x, y, z);
		}
		for(int i = 0; i < 5; i++) {
			double alpha, beta, x, y, z;
			alpha = (i + 0.5) * (2 * Math.PI) / 5;
			beta = Math.PI - 1.1072;
			y = Math.cos(beta);
			x = Math.cos(alpha) * Math.sin(beta);
			z = Math.sin(alpha) * Math.sin(beta);
			icosahedronVerticies[i + 6] = Vector3.create(x, y, z);
		}
		icosahedronVerticies[11] = Vector3.create(0, -1, 0);


		Matrix4 mat = Matrix4.create();
		mat.setRotationFromEuler(Vector3.create(-Math.PI/2, 0, 0));
		for (Vector3 v : icosahedronVerticies) {
			mat.multiplyVector3(v);
		}
	}
//
//	protected static Vector3[] icosahedronVerticies = Vector3.create[] {
//		Vector3.create( -1,  t,  0 ), Vector3.create(  1, t, 0 ), Vector3.create( -1, -t,  0 ), Vector3.create(  1, -t,  0 ),
//		Vector3.create(  0, -1,  t ), Vector3.create(  0, 1, t ), Vector3.create(  0, -1, -t ), Vector3.create(  0,  1, -t ),
//		Vector3.create(  t,  0, -1 ), Vector3.create(  t, 0, 1 ), Vector3.create( -t,  0, -1 ), Vector3.create( -t,  0,  1 )
//	};

	protected static int[][] icosahedronFaces = new int[][] {
		{6, 11, 7}, {7, 11, 8}, {8, 11, 9}, {9, 11, 10}, {10, 11, 6}, // Top
		{6, 7, 2}, {7, 8, 3}, {8, 9, 4}, {9, 10, 5},{10, 6, 1}, // v
		{1, 6, 2}, {2, 7, 3}, {3, 8, 4}, {4, 9, 5}, {5, 10, 1}, // ^
		{0, 1, 2}, {0, 2, 3}, {0, 3, 4}, {0, 4, 5}, {0, 5, 1}, // Bottom
	};


	protected Set<Vector3> verticies = Sets.newTreeSet(vector3Comparator);
	protected Set<Vector3> usedVerticies = Sets.newHashSet();

	protected List<DomeFace> faces = Lists.newArrayList();
	protected Map<Vector3, VertexFaceGrouping> vertexFaceGroupings = Maps.newTreeMap(vector3Comparator);

	protected Set<DomeEdge> edges = Sets.newHashSet();

	private final GradienTeaDomeSpec specification;

	public OldDomeGeometry(
		GradienTeaDomeSpec specification
	) {
		this.specification = specification;
		build();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Geometry Building Methods

	protected void build() {
		int faceIndex = 0;
		for (int[] faceIndicies : icosahedronFaces) {
			DomeFace face = new DomeFace(
				include(icosahedronVerticies[faceIndicies[0]]),
				include(icosahedronVerticies[faceIndicies[1]]),
				include(icosahedronVerticies[faceIndicies[2]])
			);

			int icoLayer = (faceIndex < 5)
				? 0
				: (faceIndex < 15
					? 1
					: 2
				);
			faceIndex ++;

			if (specification.getFrequency() > 1) {
				faces.addAll(splitFace(face, specification.getFrequency()));
			} else {
				faces.add(face);
			}
		}
//
//		for (Vector3 v : icosahedronVerticies) {
//			vertexFaceGroupings.put(v, new VertexFaceGrouping(v));
//		}

		// Trim the dome
		trim();

		for (Vector3 v : verticies) {
			v.normalize();
			v.multiplyScalar(specification.getRadius());
		}

		for (DomeFace face : faces) {
			edges.addAll(face.getEdges());

			usedVerticies.add(face.getA());
			usedVerticies.add(face.getB());
			usedVerticies.add(face.getC());
		}
	}

	private void trim() {
		List<List<DomeFace>> primaryRings = new VertexFaceGrouping(icosahedronVerticies[11]).getRings();

		for (int i=specification.getLayers(); i<primaryRings.size(); i++) {
			faces.removeAll(primaryRings.get(i));
		}
	}

	protected List<DomeFace> splitFace( DomeFace face, int frequency ) {
		List<DomeFace> faces = Lists.newArrayList();
		doSplit(faces, include(face.v1), include(face.v2), include(face.v3), frequency);
		return faces;
	}

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
				faces.add(
					new DomeFace(
						bottom.get(i),
						top.get(i),
						bottom.get(i + 1)
					)
				);

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
	 *
	 * @param v
	 * @return
	 */
	protected Vector3 include(final Vector3 v) {
		if (verticies.contains(v)) {
			for (Vector3 existing : verticies) {
				if (vector3Comparator.compare(existing, v) == 0) {
					return existing;
				}
			}
		}

		verticies.add(v);
		return v;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utility Methods


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

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public VertexFaceGrouping vertexFaceGroupings(int vertexIndex) {
		Preconditions.checkArgument(vertexIndex >= 0 && vertexIndex < 12, "vertexIndex must be between 0 and 11 (inclusive)");

		Vector3 vertex = icosahedronVerticies[vertexIndex];

		VertexFaceGrouping grouping = vertexFaceGroupings.get(vertex);
		if (grouping == null) {
			vertexFaceGroupings.put(vertex, grouping = new VertexFaceGrouping(vertex));
		}

		return grouping;
	}

	public GradienTeaDomeSpec getSpec() {
		return specification;
	}

	public Collection<Vector3> getVertices() {
		return usedVerticies;
	}

	public List<DomeFace> getFaces() {
		return faces;
	}

	public Collection<DomeEdge> getEdges() {
		return edges;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	/**
	 * Used internally to keep track of the verticies
	 */

	public final static Comparator<Vector3> vector3Comparator = new Comparator<Vector3>() {
		public int compare(final Vector3 o1, final Vector3 o2) {
			return ComparisonChain.start()
				.compare(o1.getX(), o2.getX())
				.compare(o1.getY(), o2.getY())
				.compare(o1.getZ(), o2.getZ())
				.result();
		}
	};

	/**
	 * Represents one face of the dome
	 */
	public static class DomeFace {
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

		public Collection<Vector3> getVertices() {
			return Arrays.asList(v1, v2, v3);
		}

		public static Function<? super DomeFace, Collection<Vector3>> getVertices = new Function<DomeFace, Collection<Vector3>>() {
			@Override
			public Collection<Vector3> apply(final DomeFace input) {
				return input.getVertices();
			}
		};


		public Collection<? extends DomeEdge> getEdges() {
			return Arrays.asList(
				new DomeEdge(v1, v2),
				new DomeEdge(v2, v3),
				new DomeEdge(v3, v1)
			);
		}

		public static class ContainsVertex implements Predicate<DomeFace> {
			protected Collection<Vector3> verticies;

			public ContainsVertex(final Collection<Vector3> verticies) {
				this.verticies = verticies;
			}

			@Override
			public boolean apply(@Nullable final DomeFace face) {
				return ! Collections2.filter(verticies, new Predicate<Vector3>() {
					@Override
					public boolean apply(@Nullable final Vector3 input) {
						return vector3Comparator.compare(input, face.getA()) == 0
							|| vector3Comparator.compare(input, face.getB()) == 0
							|| vector3Comparator.compare(input, face.getC()) == 0;
					}
				}).isEmpty();
			}
		}
	}

	/**
	 * Represents one edge of the dome
	 */
	public static class DomeEdge {
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

			if (v1.equals(domeEdge.v1) && v2.equals(domeEdge.v2))
				return true;

			if (v2.equals(domeEdge.v1) && v1.equals(domeEdge.v2))
				return true;

			return false;
		}

		@Override
		public int hashCode() {
			int result;
			if (lessThan(v1, v2)) {
				result = v1.hashCode();
				result = 31 * result + v2.hashCode();
			} else {
				result = v2.hashCode();
				result = 31 * result + v1.hashCode();
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
			return "GeoEdge{" +
				"v1=" + v1 +
				", v2=" + v2 +
				'}';
		}

		public Vector3 getV1() {
			return v1;
		}

		public Vector3 getV2() {
			return v2;
		}
	}

	/**
	 * Holds various groups of faces from the perspective of a single vertex
	 */
	public class VertexFaceGrouping {
		/**
		 * Rings of faces starting at the vertex (5 faces) and moving away towards the other end of the sphere
		 */
		protected List<List<DomeFace>> rings = Lists.newArrayList();

		public VertexFaceGrouping(Vector3 vertex) {
			addRing(Lists.newArrayList(faces), Arrays.asList(vertex));
		}

		private void addRing(final ArrayList<DomeFace> remainingFaces, final List<Vector3> verticies) {
			List<DomeFace> ringFaces = FluentIterable.from(remainingFaces)
				.filter(new DomeFace.ContainsVertex(verticies))
				.toImmutableList();

			if (ringFaces.isEmpty()) {
				throw new IllegalArgumentException("No faces contain any vertices in " + verticies);
			}

			rings.add(ringFaces);
			remainingFaces.removeAll(ringFaces);

			if (! remainingFaces.isEmpty()) {
				addRing(remainingFaces, FluentIterable.from(ringFaces).transformAndConcat(DomeFace.getVertices).toImmutableList());
			}
		}

		public List<List<DomeFace>> getRings() {
			return rings;
		}
	}
}
