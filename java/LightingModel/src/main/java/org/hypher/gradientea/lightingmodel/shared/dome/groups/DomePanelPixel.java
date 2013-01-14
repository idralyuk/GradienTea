package org.hypher.gradientea.lightingmodel.shared.dome.groups;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.hypher.gradientea.lightingmodel.shared.color.PixelColor;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoFace;
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
			return new ListPixelGroup(Collections2.transform(input, create));
		}
	};

	protected GeoFace face;

	public DomePanelPixel(final GeoFace face) {
		this.face = face;
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
		return Collections.<PixelGroup>singletonList(this);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
