package org.hypher.gradientea.transport.shared;

import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;

import java.io.Serializable;

/**
 * Holds the pixel information for a single frame of animation of the dome in a raw form. This data can be sent via an
 * {@link DomeAnimationTransport} for display on the dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeAnimationFrame implements Serializable {
	/**
	 * The data to display. The order is assumed to be RGB in the order of the faces of the dome as defined by
	 * {@link GradienTeaDomeGeometry#getLightedFaces()}.
	 */
	private /*final*/ byte[] pixelData;

	protected DomeAnimationFrame() { /* For Serialization Only */ }

	public DomeAnimationFrame(final byte[] pixelData) {
		this.pixelData = pixelData;
	}

	public byte[] getPixelData() {
		return pixelData;
	}
}
