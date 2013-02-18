package org.hypher.gradientea.lightingmodel.shared.dome.groups;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import org.hypher.gradientea.lightingmodel.shared.color.PixelColor;
import org.hypher.gradientea.lightingmodel.shared.dome.geometry.GeoFace;
import org.hypher.gradientea.lightingmodel.shared.pixel.AbstractPixel;
import org.hypher.gradientea.lightingmodel.shared.pixel.ListPixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A pixel defined by a {@link GeoFace}.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomePanelPixel extends AbstractPixel {
	public final static transient Function<GeoFace, DomePanelPixel> create = new Function<GeoFace, DomePanelPixel>() {
		public DomePanelPixel apply(final GeoFace input) {
			return new DomePanelPixel(input);
		}
	};

	public final static transient Function<Collection<? extends GeoFace>, PixelGroup> createGroup = new Function<Collection<? extends GeoFace>, PixelGroup>() {
		public PixelGroup apply(final Collection<? extends GeoFace> input) {
			return group(input);
		}
	};

	protected GeoFace face;

	public DomePanelPixel(final GeoFace face) {
		this.face = face;
	}

	public static List<DomePanelPixel> pixels(Iterable<? extends GeoFace> faces) {
		return FluentIterable.from(faces).transform(create).toImmutableList();
	}


	public static PixelGroup group(Iterable<? extends GeoFace> faces) {
		return new ListPixelGroup(pixels(faces));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> applyColor(final PixelColor color) {
		return Collections.singletonList(
			new PixelValue(
				this,
				color
			)
		);
	}

	@Override
	public List<PixelGroup> getChildren() {
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
