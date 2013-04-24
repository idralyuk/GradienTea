package org.hypher.gradientea.geometry.shared;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaDomeGeometry implements Serializable {

	/**
	 * Comparator for GeoFaces giving them the addressing order used by the GradienTea animation system. This ordering
	 * is arbitrary but consistent. The algorithm is as follows:
	 *
	 * - Find vertex of each face which is closest to the top vertex of the dome; use this as the "comparison vertex"
	 * - Faces with comparison vertices with smaller Y coordinates are considered "smaller" in addressing order
	 * - If the comparison vertices have the same Y coordinate, compare their angle of rotation around the Y axis
	 *
	 */
	public static transient final Comparator<GeoFace> FACE_ADDRESSING_COMPARATOR = new Comparator<GeoFace>() {
		Comparator<GeoVector3> distanceToTopVertexComparator = new GeoVector3.DistanceComparator(GeodesicSphereGeometry.topVertex);

		@Override
		public int compare(
			final GeoFace face1, final GeoFace face2
		) {
			GeoVector3 vertex1 = Collections.min(face1.getVertices(), distanceToTopVertexComparator);
			GeoVector3 vertex2 = Collections.min(face2.getVertices(), distanceToTopVertexComparator);

			final int yDelta = GeoVector3.tolerantDoubleComparator.compare(vertex1.getY(), vertex2.getY());
			if (yDelta != 0) return yDelta;

			// Calculate angle around y axis
			final double angle1 = Math.atan2(Math.sqrt(vertex1.getX()*vertex1.getX()+vertex1.getY()*vertex1.getY()), vertex1.getZ());
			final double angle2 = Math.atan2(Math.sqrt(vertex2.getX()*vertex2.getX()+vertex2.getY()*vertex2.getY()), vertex2.getZ());

			return GeoVector3.tolerantDoubleComparator.compare(angle1, angle2);
		}
	};

	protected GeodesicDomeGeometry domeGeometry;
	protected GradienTeaDomeSpec spec;

	private Set<GeoFace> lightedFaces;

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
			lightedFaces = ImmutableSet.copyOf(
				Ordering.from(FACE_ADDRESSING_COMPARATOR).immutableSortedCopy(
					Iterables.concat(
						domeGeometry
							.ringsFrom(GeodesicSphereGeometry.topVertex)
							.subList(0, spec.getLightedLayers())
					)
				)
			);
		}

		return lightedFaces;
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
