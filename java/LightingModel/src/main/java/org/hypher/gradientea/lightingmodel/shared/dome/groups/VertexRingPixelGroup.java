package org.hypher.gradientea.lightingmodel.shared.dome.groups;

import com.google.common.collect.FluentIterable;
import org.hypher.gradientea.lightingmodel.shared.dome.geometry.GeoVector3;
import org.hypher.gradientea.lightingmodel.shared.pixel.AbstractPixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;

import java.util.List;

/**
 * Represents the group of rings of faces on a dome, starting at a given vertex.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class VertexRingPixelGroup extends AbstractPixelGroup implements PixelGroup {
	private GeoVector3 domeVertex;

	protected VertexRingPixelGroup() {}

	public VertexRingPixelGroup(GeoVector3 domeVertex) {
		this.domeVertex = domeVertex;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelGroup> getChildren() {
		return FluentIterable.from(domeGeometry().ringsFrom(domeVertex))
			.transform(DomePanelPixel.createGroup)
			.toImmutableList();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public GeoVector3 getDomeVertex() {
		return domeVertex;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
