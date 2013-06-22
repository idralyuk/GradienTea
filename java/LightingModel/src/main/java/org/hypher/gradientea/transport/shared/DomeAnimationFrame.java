package org.hypher.gradientea.transport.shared;

import com.google.common.base.Preconditions;
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
	 * The data to display on the faces of the dome. The order is assumed to be RGB in the order of the faces of the
	 * dome as defined by {@link GradienTeaDomeGeometry#getLightedFaces()}.
	 */
	private /*final*/ byte[] facePixelData;

	/**
	 * The data to display on the vertices of the dome. The order is assumed to be RGB in the order of the verticies
	 * of the dome as defined by {@link GradienTeaDomeGeometry#getLightedVerticies()}.
	 */
	private /*final*/ byte[] vertexPixelData;

	protected DomeAnimationFrame() { /* For Serialization Only */ }

	public DomeAnimationFrame(final byte[] facePixelData, final byte[] vertexPixelData) {
		Preconditions.checkNotNull(facePixelData, "facePixelData cannot be null. Pass an empty array instead.");
		Preconditions.checkNotNull(vertexPixelData, "vertexPixelData cannot be null. Pass an empty array instead.");

		this.facePixelData = facePixelData;
		this.vertexPixelData = vertexPixelData;
	}

	public DomeAnimationFrame(final byte[] facePixelData) {
		this(facePixelData, new byte[0]);
	}

	public byte[] getFacePixelData() {
		return facePixelData;
	}

	public byte[] getVertexPixelData() {
		return vertexPixelData;
	}
}
