package org.hypher.gradientea.lightingmodel.shared.dome;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GeodesicDomeGeometry implements Serializable {
	protected GeodesicSphereGeometry sphereGeometry;
	protected GeodesicDomeSpec spec;

	protected Set<GeoVector3> verticies;
	protected Set<GeoFace> faces;
	protected Set<GeoEdge> edges;

	protected transient Map<GeoVector3, List<List<GeoFace>>> vertexRings;

	protected GeodesicDomeGeometry() { }

	public GeodesicDomeGeometry(
		final GeodesicDomeSpec spec
	) {
		this.spec = spec;
		this.sphereGeometry = new GeodesicSphereGeometry(spec);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods
	public List<List<GeoFace>> ringsFrom(GeoVector3 vertex) {
		if (vertexRings.containsKey(vertex)) {
			return vertexRings.get(vertex);
		}

		List<List<GeoFace>> newRings =
			FluentIterable.from(sphereGeometry.vertexFaceGroupings(vertex).getRings())
				// First remove all faces that aren't part of the dome
				.transform(
					new Function<List<GeoFace>, List<GeoFace>>() {
						public List<GeoFace> apply(final List<GeoFace> input) {
							return FluentIterable.from(input)
								.filter(Predicates.in(faces))
								.toImmutableList();
						}
					}
				)
				.filter(
					new Predicate<List<GeoFace>>() {
						public boolean apply(final List<GeoFace> input) {
							return input != null && !input.isEmpty();
						}
					}
				)
				.toImmutableList();

		vertexRings.put(vertex, newRings);
		return newRings;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Building Methods
	protected void ensureBuilt() {
		if (verticies == null) {
			build();
		}
	}

	protected void build() {
		int index = 0;

		verticies = Sets.newHashSet();
		faces = Sets.newHashSet();
		edges = Sets.newHashSet();
		vertexRings = Maps.newHashMap();

		for (List<GeoFace> ring : sphereGeometry.vertexFaceGroupings(GeodesicSphereGeometry.topVertex).getRings()) {
			if (index++ >= spec.getLayers()) {
				break;
			}

			faces.addAll(ring);
			for (GeoFace face : ring) {
				faces.add(face);

				verticies.addAll(face.getVertices());
				edges.addAll(face.getEdges());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters
	public GeodesicSphereGeometry getSphereGeometry() {
		return sphereGeometry;
	}

	public GeodesicDomeSpec getSpec() {
		return spec;
	}

	public Set<GeoVector3> getVertices() {
		ensureBuilt();
		return verticies;
	}

	public Set<GeoFace> getFaces() {
		ensureBuilt();
		return faces;
	}

	public Set<GeoEdge> getEdges() {
		ensureBuilt();
		return edges;
	}
}
