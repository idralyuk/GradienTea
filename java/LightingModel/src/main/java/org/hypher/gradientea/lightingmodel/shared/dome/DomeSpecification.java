package org.hypher.gradientea.lightingmodel.shared.dome;

/**
 * Specifies the parameters that are needed to build a dome model.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeSpecification {
	/**
	 * The frequency of the dome, this describes how many "cuts" are made in the icosahedron to create the dome.
	 */
	protected int frequency;

	/**
	 * The number of layers in the dome.
	 */
	protected int layers;

	/**
	 * The radius of the dome, in arbitrary units.
	 */
	protected double radius;

	/**
	 * The length of the side of the glowing panels.
	 */
	protected double panelSideLength;

	/**
	 * The thickness of the glowing panels
	 */
	protected double panelThickness;

	public DomeSpecification(
		final int frequency,
		final int layers,
		final double radius,
		final double panelSideLength,
		final double panelThickness
	) {
		this.frequency = frequency;
		this.layers = layers;
		this.radius = radius;
		this.panelSideLength = panelSideLength;
		this.panelThickness = panelThickness;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "DomeSpecification{" +
			"panelSideLength=" + panelSideLength +
			", radius=" + radius +
			", layers=" + layers +
			", frequency=" + frequency +
			'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public int getFrequency() {
		return frequency;
	}

	public int getLayers() {
		return layers;
	}

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
