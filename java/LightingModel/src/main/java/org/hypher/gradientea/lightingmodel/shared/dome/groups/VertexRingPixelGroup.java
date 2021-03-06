package org.hypher.gradientea.lightingmodel.shared.dome.groups;

import com.google.common.collect.FluentIterable;
import org.hypher.gradientea.geometry.shared.GeoVector3;
import org.hypher.gradientea.geometry.shared.GeodesicDomeGeometry;
import org.hypher.gradientea.animation.shared.pixel.ListPixelGroup;

/**
 * Represents the group of rings of faces on a dome, starting at a given vertex.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class VertexRingPixelGroup extends ListPixelGroup {
	protected VertexRingPixelGroup() {}

	public VertexRingPixelGroup(GeodesicDomeGeometry geometry, GeoVector3 isoVertex) {
		super(
			FluentIterable.from(geometry.ringsFrom(isoVertex)).
				transform(DomePanelPixel.createGroup)
				.toImmutableList()
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
