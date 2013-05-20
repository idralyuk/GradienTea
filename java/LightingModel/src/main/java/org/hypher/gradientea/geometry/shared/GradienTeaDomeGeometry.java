package org.hypher.gradientea.geometry.shared;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaDomeGeometry implements Serializable {
	public static transient final Comparator<GeoFace> ROTATION_ABOUT_Z_COMPARATOR = new Comparator<GeoFace>() {
		@Override
		public int compare(
			final GeoFace face1, final GeoFace face2
		) {
			GeoVector3 vertex1 = face1.center();
			GeoVector3 vertex2 = face2.center();

			// Calculate angle around z axis
			final double angle1 = Math.atan2(vertex1.getY(), vertex1.getX());
			final double angle2 = Math.atan2(vertex2.getY(), vertex2.getX());

			return GeoVector3.tolerantDoubleComparator.compare(angle2, angle1);
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
			lightedFaces = new LinkedHashSet<GeoFace>();

			for(List<GeoFace> ring : domeGeometry.ringsFrom(GeodesicSphereGeometry.topVertex)) {
				ring = Ordering.from(ROTATION_ABOUT_Z_COMPARATOR).sortedCopy(ring);
				lightedFaces.addAll(ring);
			}
		}

		return lightedFaces;
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
