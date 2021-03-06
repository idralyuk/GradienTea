package org.hypher.gradientea.geometry.shared;

import java.util.Arrays;

/**
 * Specifies the parameters that are needed to build a dome model.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaDomeSpec extends GeodesicDomeSpec {
	/**
	 * The radius of the dome, in feet
	 */
	protected double radius;

	/**
	 * The maximum height of the glowing panels, in feet.
	 */
	protected double maxPanelHeight;

	/**
	 * The thickness of the glowing panels, in feet
	 */
	protected double panelThickness;

	protected int lightedLayers;

	protected int[] emptyFaces;
	protected int[] emptyVertices;

	protected GradienTeaDomeSpec() {}


	public GradienTeaDomeSpec(
		final int frequency,
		final int layers,
		final int lightedLayers,
		final double radius,
		final double maxPanelHeight,
		final double panelThickness,
		final int[] emptyFaces,
		final int[] emptyVertices
	) {
		super(frequency, layers);

		this.lightedLayers = lightedLayers;
		this.radius = radius;
		this.maxPanelHeight = maxPanelHeight;
		this.panelThickness = panelThickness;
		this.emptyFaces = Arrays.copyOf(emptyFaces, emptyFaces.length);
		this.emptyVertices = Arrays.copyOf(emptyVertices, emptyVertices.length);
	}

	public GradienTeaDomeSpec(
		final int frequency,
		final int layers,
		final int lightedLayers,
		final double radius,
		final double maxPanelHeight,
		final double panelThickness
	) {
		this(
			frequency,
			layers,
			lightedLayers,
			radius,
			maxPanelHeight,
			panelThickness,
			new int[0],
			new int[0]
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	public int faceCount() {
		return calculateFaceCount(layers);
	}

	public int lightedFaceCount() {
		return calculateFaceCount(lightedLayers);
	}

	protected int calculateFaceCount(int layers) {
		final int top;
		{
			final int topLayerCount = Math.min(layers, frequency);
			top = topLayerCount*topLayerCount*5;
		}

		final int middle;
		if (layers > frequency) {
			int midLayerCount = Math.min(layers - frequency, frequency);

			middle = midLayerCount*midLayerCount*5 + // Up facing
					 ((frequency*frequency) - ((frequency-midLayerCount)*(frequency-midLayerCount)))*5; // Down facing
		} else {
			middle = 0;
		}

		final int bottom;
		if (layers > frequency*2) {
			int bottomLayerCount = Math.min(layers - frequency*2, frequency);
			bottom = ((frequency*frequency) - ((frequency-bottomLayerCount)*(frequency-bottomLayerCount)))*5;
		} else {
			bottom = 0;
		}

		return top + middle + bottom;
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "GradienTeaDomeSpec{" +
			"maxPanelHeight=" + maxPanelHeight +
			", radius=" + radius +
			", layers=" + layers +
			", frequency=" + frequency +
		'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public int getLightedLayers() {
		return lightedLayers;
	}

	public double getRadius() {
		return radius;
	}

	public double getMaxPanelHeight() {
		return maxPanelHeight;
	}

	public double getPanelSideLength() {
		return (2.0/Math.sqrt(3)) * maxPanelHeight;
	}

	public double getPanelThickness() {
		return panelThickness;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
