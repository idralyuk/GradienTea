package org.hypher.gradientea.lightingmodel.shared.dome.groups;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoFace;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoVector3;
import org.hypher.gradientea.lightingmodel.shared.dome.GeodesicDomeGeometry;
import org.hypher.gradientea.lightingmodel.shared.pixel.ListPixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;

import javax.annotation.Nullable;
import java.util.List;

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
				transform(new Function<List<GeoFace>, PixelGroup>() {
					@Nullable
					@Override
					public PixelGroup apply(@Nullable final List<GeoFace> input) {
						return null;
					}
				})
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
