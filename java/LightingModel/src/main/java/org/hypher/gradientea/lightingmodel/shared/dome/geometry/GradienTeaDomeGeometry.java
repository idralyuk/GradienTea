package org.hypher.gradientea.lightingmodel.shared.dome.geometry;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hypher.gradientea.lightingmodel.shared.dome.geometry.GeoVector3;
import org.hypher.gradientea.lightingmodel.shared.dome.geometry.GeoEdge;
import org.hypher.gradientea.lightingmodel.shared.dome.geometry.GeoFace;
import org.hypher.gradientea.lightingmodel.shared.dome.geometry.GeodesicDomeGeometry;
import org.hypher.gradientea.lightingmodel.shared.dome.geometry.GeodesicSphereGeometry;
import org.hypher.gradientea.lightingmodel.shared.dome.spec.GradienTeaDomeSpec;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaDomeGeometry implements Serializable {

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
				Iterables.concat(
					domeGeometry
						.ringsFrom(GeodesicSphereGeometry.topVertex)
						.subList(0, spec.getLightedLayers())
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
