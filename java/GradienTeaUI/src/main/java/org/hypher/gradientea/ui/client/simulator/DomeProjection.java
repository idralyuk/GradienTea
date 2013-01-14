package org.hypher.gradientea.ui.client.simulator;

import com.google.common.collect.Maps;
import net.blimster.gwt.threejs.core.Vector3;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoEdge;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoFace;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoVector3;
import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeGeometry;

import java.util.Collection;
import java.util.Map;

/**
 * Translates between dome geometry and ThreeJS coordinates, projecting the coordinates to the radius
 * of the dome in the process.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeProjection {
	protected Map<GeoVector3, Vector3> vertexMap = Maps.newHashMap();

	protected final GradienTeaDomeGeometry geometry;

	public DomeProjection(GradienTeaDomeGeometry geometry) {
		this.geometry = geometry;

		build();
	}

	public Vector3 vertex(GeoVector3 geoVertex) {
		return vertexMap.get(geoVertex);
	}

	public Vector3[] face(GeoFace face) {
		return new Vector3[]{
			vertex(face.getA()),
			vertex(face.getB()),
			vertex(face.getC())
		};
	}

	public Vector3[] edge(GeoEdge edge) {
		return new Vector3[]{
			vertex(edge.getV1()),
			vertex(edge.getV2())
		};
	}

	public Collection<Vector3> vertices() {
		return vertexMap.values();
	}

	public double bottomZ() {
		return vertex(geometry.getDomeGeometry().getLowestVertex()).getZ();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Construction methods

	protected void build() {
		for (GeoVector3 geoVertex : geometry.getDomeGeometry().getVertices()) {
			vertexMap.put(geoVertex, project(geoVertex));
		}
	}

	protected Vector3 project(GeoVector3 vertex) {
		return Vector3.create(vertex.getX(), vertex.getY(), vertex.getZ()).normalize().multiplyScalar(
			geometry.getSpec()
				.getRadius()
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters

	public GradienTeaDomeGeometry getGeometry() {
		return geometry;
	}
}
