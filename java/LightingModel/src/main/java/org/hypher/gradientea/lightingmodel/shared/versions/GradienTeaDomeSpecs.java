package org.hypher.gradientea.lightingmodel.shared.versions;

import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Various prebuilt dome specifications.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaDomeSpecs {
	public final static transient GradienTeaDomeSpec MEDIUM_DOME_LARGE_PANELS = new GradienTeaDomeSpec(
		4,
		7,
		6,
		20,
		3.5,
		1d/24
	);

	public final static transient GradienTeaDomeSpec DEMO_DOME_LARGE_PANELS = new GradienTeaDomeSpec(
		2,
		3,
		3,
		18/2.0,
		3.5,
		1d/24
	);

	public final static transient GradienTeaDomeSpec DEMO_DOME_SMALL_PANELS = new GradienTeaDomeSpec(
		2,
		3,
		3,
		18/2.0,
		2,
		1d/24
	);

	public final static transient GradienTeaDomeSpec MEDIUM_DOME_SMALL_PANELS = new GradienTeaDomeSpec(
		4,
		7,
		6,
		20,
		2,
		1d/24
	);

	public final static transient GradienTeaDomeSpec LARGE_DOME_SMALL_PANELS = new GradienTeaDomeSpec(
		5,
		8,
		7,
		25,
		2,
		1d/24
	);

	public final static transient Map<String, GradienTeaDomeSpec> NAMED = new LinkedHashMap<String, GradienTeaDomeSpec>(){{
		put("Large Dome w/ Small Panels", LARGE_DOME_SMALL_PANELS);
		put("Medium Dome w/ Small Panels", MEDIUM_DOME_SMALL_PANELS);
		put("Demo Dome w/ Small Panels", DEMO_DOME_SMALL_PANELS);
		put("Medium Dome w/ Large Panels", MEDIUM_DOME_LARGE_PANELS);
		put("Demo Dome w/ Large Panels", DEMO_DOME_LARGE_PANELS);
	}};
}
