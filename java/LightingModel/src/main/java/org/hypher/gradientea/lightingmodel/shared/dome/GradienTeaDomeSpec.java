package org.hypher.gradientea.lightingmodel.shared.dome;

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
	 * The length of the side of the glowing panels, in feet
	 */
	protected double panelSideLength;

	/**
	 * The thickness of the glowing panels, in feet
	 */
	protected double panelThickness;

	protected GradienTeaDomeSpec() {}

	public GradienTeaDomeSpec(
		final int frequency,
		final int layers,
		final double radius,
		final double panelSideLength,
		final double panelThickness
	) {
		super(frequency, layers);

		this.radius = radius;
		this.panelSideLength = panelSideLength;
		this.panelThickness = panelThickness;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	public int calculateFaceCount() {
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
			"panelSideLength=" + panelSideLength +
			", radius=" + radius +
			", layers=" + layers +
			", frequency=" + frequency +
			'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public double getRadius() {
		return radius;
	}

	public double getPanelSideLength() {
		return panelSideLength;
	}

	public double getPanelThickness() {
		return panelThickness;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
