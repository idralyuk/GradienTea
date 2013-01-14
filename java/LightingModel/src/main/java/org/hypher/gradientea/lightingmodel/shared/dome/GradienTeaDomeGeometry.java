package org.hypher.gradientea.lightingmodel.shared.dome;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.io.Serializable;
import java.util.Set;

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
