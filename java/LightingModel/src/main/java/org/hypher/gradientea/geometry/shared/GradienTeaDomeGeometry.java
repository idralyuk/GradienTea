package org.hypher.gradientea.geometry.shared;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaDomeGeometry implements Serializable {
	public static transient final Comparator<GeoFace> FACE_ROTATION_ABOUT_Z_COMPARATOR = new Comparator<GeoFace>() {
		@Override
		public int compare(
			final GeoFace face1, final GeoFace face2
		) {
			return VERTEX_ROTATION_ABOUT_Z_COMPARATOR.compare(face1.center(), face2.center());
		}
	};


	public static transient final Comparator<GeoVector3> VERTEX_ROTATION_ABOUT_Z_COMPARATOR = new Comparator<GeoVector3>() {
		@Override
		public int compare(
			final GeoVector3 vertex1,
			final GeoVector3 vertex2
		) {
			// Calculate angle around z axis
			final double angle1 = Math.atan2(vertex1.getY(), vertex1.getX());
			final double angle2 = Math.atan2(vertex2.getY(), vertex2.getX());

			return GeoVector3.tolerantDoubleComparator.compare(angle2, angle1);
		}
	};

	protected GeodesicDomeGeometry domeGeometry;
	protected GradienTeaDomeSpec spec;

	private transient Set<GeoFace> lightedFaces;
	private transient LinkedHashSet<GeoVector3> lightedVertices;
	private transient Double lightedPanelArc;
	private transient Double vertexRadius;

	protected GradienTeaDomeGeometry() {}

	public GradienTeaDomeGeometry(
		final GradienTeaDomeSpec spec
	) {
		this.domeGeometry = new GeodesicDomeGeometry(spec);
		this.spec = spec;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	public Set<GeoFace> getLightedFaces() {
		if (lightedFaces == null) {
			lightedFaces = new LinkedHashSet<GeoFace>();

			for(List<GeoFace> ring : domeGeometry.ringsFrom(GeodesicSphereGeometry.topVertex).subList(0, spec.getLightedLayers())) {
				ring = Ordering.from(FACE_ROTATION_ABOUT_Z_COMPARATOR).sortedCopy(ring);
				lightedFaces.addAll(ring);
			}
		}

		return lightedFaces;
	}

	public Set<GeoVector3> getLightedVertices() {
		if (lightedVertices == null) {
			lightedVertices = new LinkedHashSet<GeoVector3>();

			lightedVertices.add(domeGeometry.getHighestVertex());
			Set<GeoFace> remainingFaces = Sets.newLinkedHashSet(lightedFaces);

			Collection<GeoVector3> previousRingBottomVertices = Collections.singleton(domeGeometry.getHighestVertex());
			while (! remainingFaces.isEmpty()) {
				// Get the faces containing the current vertices
				Collection<GeoFace> faceRing = FluentIterable.from(remainingFaces)
					.filter(new GeoFace.ContainsVertex(previousRingBottomVertices))
					.toImmutableSet();

				// And remove them from the remaining faces...
				remainingFaces.removeAll(faceRing);

				// We want all the vertices in the ring that aren't also part of the last ring, sorted by angle
				final Collection<GeoVector3> currentRingBottomVertices =
					Ordering.from(VERTEX_ROTATION_ABOUT_Z_COMPARATOR).sortedCopy(
						FluentIterable.from(faceRing)
							.transformAndConcat(GeoFace.getVertices)
							.filter(Predicates.not(Predicates.in(previousRingBottomVertices)))
							.toImmutableSet()
					);

				// These are the next ring of vertices in the correct order. Add them to the set
				lightedVertices.addAll(currentRingBottomVertices);

				// And swap out the old list of bottom vertices
				previousRingBottomVertices = currentRingBottomVertices;
			}
		}

		return lightedVertices;
	}

	private static <T> void rotateList(List<T> list, boolean forward) {
		if (forward) {
			list.add(0, list.remove(list.size()-1));
		} else {
			list.add(list.size()-1, list.remove(0));
		}
	}

	public double getHeight() {
		return (GeodesicSphereGeometry.topVertex.getZ()*spec.getRadius()) - (domeGeometry.getLowestVertex().getZ()*spec.getRadius());
	}

	public double getAveragePanelArea() {
		return (spec.getMaxPanelHeight() * spec.getPanelSideLength()) / 2;
	}

	public double getTotalStrutLength() {
		double total = 0.0;
		double radius = spec.getRadius();

		for (GeoEdge edge : domeGeometry.getEdges()) {
			total += edge.getV1().multiply(radius).distanceTo(edge.getV2().multiply(radius));
		}

		return total;
	}

	public double getFloorRadius() {
		final GeoVector3 lowestVertex = domeGeometry.getLowestVertex();

		return lowestVertex.distanceTo(GeoVector3.origin.withZ(lowestVertex.getZ())) * spec.getRadius();
	}

	public SortedSet<Double> getStrutLengths() {
		SortedSet<Double> lengths = Sets.newTreeSet(new Comparator <Double>() {
			@Override
			public int compare(final Double o1, final Double o2) {
				// Equal if less than one eighth of an inch different
				if (Math.abs(o1 - o2) < (1d/(12*8))) return 0;

				return Double.compare(o1, o2);
			}
		});
		double radius = spec.getRadius();

		for (GeoEdge edge : domeGeometry.getEdges()) {
			lengths.add(
				edge.getV1().withLength(radius).distanceTo(edge.getV2().withLength(radius))
			);
		}

		return lengths;
	}

	public double getFloorArea() {
		return Math.PI * getFloorRadius()*getFloorRadius();
	}


	public double getLightedPanelArc() {
		if (lightedPanelArc == null) {
			GeoVector3 lowestPoint = Collections.min(
				FluentIterable.from(getLightedFaces()).transformAndConcat(new Function<GeoFace, Iterable<GeoVector3>>() {
					public Iterable<GeoVector3> apply(final GeoFace input) {
						return input.getVertices();
					}
				}).toImmutableList(),
				GeoVector3.yzxComparator
			);

			lightedPanelArc = lowestPoint.angleTo(GeodesicSphereGeometry.topVertex);
		}
		return lightedPanelArc;
	}

	public double getVertexRadius() {
		if (vertexRadius == null) {
			double sum = 0;
			for (GeoEdge edge : domeGeometry.getEdges()) {
				sum += Math.abs(edge.length());
			}

			vertexRadius = (sum / domeGeometry.getEdges().size()) * 0.3;
		}
		return vertexRadius;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public GeodesicDomeGeometry getDomeGeometry() {
		return domeGeometry;
	}

	public GradienTeaDomeSpec getSpec() {
		return spec;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
