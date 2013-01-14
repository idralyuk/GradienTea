package org.hypher.gradientea.lightingmodel.shared.versions;

import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeSpec;

/**
 * Various prebuilt dome specifications.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaDomeSpecs {
	public final static transient GradienTeaDomeSpec FULL_DOME = new GradienTeaDomeSpec(
		4,
		7,
		6,
		20,
		4,
		1d/24
	);

	public final static transient GradienTeaDomeSpec DEMO_DOME = new GradienTeaDomeSpec(
		2,
		3,
		3,
		18/2.0,
		4,
		1d/24
	);


	public final static transient GradienTeaDomeSpec FULL_DOME_SMALL_PANELS = new GradienTeaDomeSpec(
		4,
		7,
		6,
		20,
		2,
		1d/24
	);

	public final static transient GradienTeaDomeSpec LARGER_DOME_SMALL_PANELS = new GradienTeaDomeSpec(
		5,
		8,
		7,
		25,
		2,
		1d/24
	);

	public final static transient GradienTeaDomeSpec[] ALL = new GradienTeaDomeSpec[]{
		FULL_DOME,
		DEMO_DOME,
		FULL_DOME_SMALL_PANELS,
		LARGER_DOME_SMALL_PANELS
	};
}
