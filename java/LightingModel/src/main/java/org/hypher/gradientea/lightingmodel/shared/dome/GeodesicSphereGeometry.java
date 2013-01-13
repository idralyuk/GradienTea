package org.hypher.gradientea.lightingmodel.shared.dome;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
public class GeodesicSphereGeometry implements Serializable {
	public static GeoVector3[] icosahedronVerticies = new GeoVector3[12];
	static {
		icosahedronVerticies[0] = new GeoVector3(0, 1, 0);

		for(int i = 0; i < 5; i++) {
			double alpha, beta, x, y, z;
			alpha = i * (2 * Math.PI) / 5;
			beta = 1.1072;
			y = Math.cos(beta);
			x = Math.cos(alpha) * Math.sin(beta);
			z = Math.sin(alpha) * Math.sin(beta);
			icosahedronVerticies[i + 1] = new GeoVector3(x, y, z);
		}
		for(int i = 0; i < 5; i++) {
			double alpha, beta, x, y, z;
			alpha = (i + 0.5) * (2 * Math.PI) / 5;
			beta = Math.PI - 1.1072;
			y = Math.cos(beta);
			x = Math.cos(alpha) * Math.sin(beta);
			z = Math.sin(alpha) * Math.sin(beta);
			icosahedronVerticies[i + 6] = new GeoVector3(x, y, z);
		}
		icosahedronVerticies[11] = new GeoVector3(0, -1, 0);


		DomeMatrix4 mat = DomeMatrix4.identity.rotationFromEuler(new GeoVector3(-Math.PI/2, 0, 0));

		for (int i=0; i<icosahedronVerticies.length; i++) {
			icosahedronVerticies[i] = mat.multiplyVector3(icosahedronVerticies[i]);
		}
	}
	public static GeoVector3 topVertex = icosahedronVerticies[11];
	public static GeoVector3 bottomVertex = icosahedronVerticies[0];

	protected static int[][] icosahedronFaces = new int[][] {
		{6, 11, 7}, {7, 11, 8}, {8, 11, 9}, {9, 11, 10}, {10, 11, 6}, // Top
		{6, 7, 2}, {7, 8, 3}, {8, 9, 4}, {9, 10, 5},{10, 6, 1}, // v
		{1, 6, 2}, {2, 7, 3}, {3, 8, 4}, {4, 9, 5}, {5, 10, 1}, // ^
		{0, 1, 2}, {0, 2, 3}, {0, 3, 4}, {0, 4, 5}, {0, 5, 1}, // Bottom
	};

	private GeodesicSphereSpec spec;

	private Map<GeoVector3, GeoVector3> verticies = Maps.newHashMap();

	private List<GeoFace> faces = Lists.newArrayList();

	private Set<GeoEdge> edges = Sets.newHashSet();

	protected transient Map<GeoVector3, VertexFaceGrouping> vertexFaceGroupings
		= Maps.newHashMap();

	protected GeodesicSphereGeometry() { }

	public GeodesicSphereGeometry(
		GeodesicSphereSpec spec
	) {
		this.spec = spec;
		build();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Geometry Building Methods

	protected void build() {
		for (int[] faceIndicies : icosahedronFaces) {
			GeoFace face = new GeoFace(
				include(icosahedronVerticies[faceIndicies[0]]),
				include(icosahedronVerticies[faceIndicies[1]]),
				include(icosahedronVerticies[faceIndicies[2]])
			);

			if (spec.getFrequency() > 1) {
				faces.addAll(splitFace(face, spec.getFrequency()));
			} else {
				faces.add(face);
			}
		}

		for (GeoFace face : faces) {
			edges.addAll(face.getEdges());
		}
	}

	protected List<GeoFace> splitFace( GeoFace face, int frequency ) {
		List<GeoFace> faces = Lists.newArrayList();
		doSplit(faces, include(face.v1), include(face.v2), include(face.v3), frequency);
		return faces;
	}

	protected void doSplit(List<GeoFace> faces, GeoVector3 a, GeoVector3 b, GeoVector3 c, int divisons) {
		if (divisons == 1) {
			faces.add(new GeoFace(a, b, c));
		} else {
			//    b
			//  d   e
			// a     c

			GeoVector3 d = midpoint(a, b, divisons, 1);
			GeoVector3 e = midpoint(c, b, divisons, 1);

			List<GeoVector3> top = divide(d, e, divisons-1);
			List<GeoVector3> bottom = divide(a, c, divisons);

			for (int i=0; i<top.size(); i++) {
				//  1
				// 0 1
				faces.add(
					new GeoFace(
						bottom.get(i),
						top.get(i),
						bottom.get(i + 1)
					)
				);

				//  0 1
				// - 1
				if (i > 0) {
					faces.add(new GeoFace(
						bottom.get(i),
						top.get(i-1),
						top.get(i)
					));
				}
			}

			doSplit(faces, d, b, e, divisons - 1);
		}
	}

	protected GeoVector3 midpoint(GeoVector3 v1, GeoVector3 v2, int divisons, int index) {
		return include(v1.add(v2.sub(v1).divide(divisons).multiply(index)));
	}

	protected List<GeoVector3> divide( GeoVector3 v1, GeoVector3 v2, int divisons) {
		List<GeoVector3> points = Lists.newArrayList();
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
	protected GeoVector3 include(final GeoVector3 v) {
		final GeoVector3 existing = verticies.get(v);
		if (existing == null) {
			verticies.put(v, v);
			return v;
		} else {
			return existing;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utility Methods


	/**
	 * Angle around the Y axis, counter-clockwise when looking from above.
	 *
	 * @param vector
	 * @return
	 */
	protected static double azimuth( GeoVector3 vector ) {
		return Math.atan2( vector.getZ(), -vector.getX() );
	}

	/**
	 * Angle above the XZ plane.
	 *
	 * @param vector
	 * @return
	 */
	protected static double inclination(  GeoVector3 vector ) {
		return Math.atan2( -vector.getY(), Math.sqrt( ( vector.getX() * vector.getX() ) + ( vector.getZ() * vector.getZ() ) ) );
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public VertexFaceGrouping vertexFaceGroupings(GeoVector3 vertex) {
		VertexFaceGrouping grouping = vertexFaceGroupings.get(vertex);
		if (grouping == null) {
			vertexFaceGroupings.put(vertex, grouping = new VertexFaceGrouping(vertex));
		}

		return grouping;
	}

	public GeodesicSphereSpec getSpec() {
		return spec;
	}

	public Collection<GeoVector3> getVertices() {
		return verticies.keySet();
	}

	public List<GeoFace> getFaces() {
		return faces;
	}

	public Collection<GeoEdge> getEdges() {
		return edges;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	/**
	 * Holds various groups of faces from the perspective of a single vertex
	 */
	public class VertexFaceGrouping {
		/**
		 * Rings of faces starting at the vertex (5 faces) and moving away towards the other end of the sphere
		 */
		protected List<List<GeoFace>> rings = Lists.newArrayList();

		public VertexFaceGrouping(GeoVector3 vertex) {
			addRing(Lists.newArrayList(faces), Arrays.asList(vertex));
		}

		private void addRing(final ArrayList<GeoFace> remainingFaces, final List<GeoVector3> verticies) {
			List<GeoFace> ringFaces = FluentIterable.from(remainingFaces)
				.filter(new GeoFace.ContainsVertex(verticies))
				.toImmutableList();

			if (ringFaces.isEmpty()) {
				throw new IllegalArgumentException("No faces contain any vertices in " + verticies);
			}

			rings.add(ringFaces);
			remainingFaces.removeAll(ringFaces);

			if (! remainingFaces.isEmpty()) {
				addRing(remainingFaces, FluentIterable.from(ringFaces).transformAndConcat(GeoFace.getVertices).toImmutableList());
			}
		}

		public List<List<GeoFace>> getRings() {
			return rings;
		}
	}
}
