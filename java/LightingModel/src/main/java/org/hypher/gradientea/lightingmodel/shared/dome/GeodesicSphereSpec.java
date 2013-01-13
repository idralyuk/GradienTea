package org.hypher.gradientea.lightingmodel.shared.dome;

import java.io.Serializable;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GeodesicSphereSpec implements Serializable {

	protected int frequency;

	public GeodesicSphereSpec() { }

	public GeodesicSphereSpec(final int frequency) {
		this.frequency = frequency;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public int getFrequency() {
		return frequency;
	}
}
