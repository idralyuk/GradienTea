package org.hypher.gradientea.lightingmodel.shared.dome;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GeodesicDomeSpec extends GeodesicSphereSpec {
	protected int layers;

	public GeodesicDomeSpec() { }

	public GeodesicDomeSpec(final int frequency, final int layers) {
		super(frequency);

		this.layers = Math.min(layers, frequency*3);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public int getLayers() {
		return layers;
	}
}
