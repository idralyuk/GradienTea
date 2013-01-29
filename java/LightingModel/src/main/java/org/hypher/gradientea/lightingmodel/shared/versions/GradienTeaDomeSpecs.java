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
	public final static transient GradienTeaDomeSpec GRADIENTEA_DOME = new GradienTeaDomeSpec(
		5, // Frequency
		8, // Layers
		7, // Lighted Layers
		20, // Radius (ft)
		2, // Panel Height (ft)
		8 /*mm*/ / 25.4 / 12 // Panel thickness (ft)
	);

	public final static transient GradienTeaDomeSpec PROTOTYPE_DOME = new GradienTeaDomeSpec(
		2, // Frequency
		3, // Layers
		3, // Lighted Layers
		9, // Radius (ft)
		2, // Panel Height (ft)
		8 /*mm*/ / 25.4 / 12 // Panel thickness (ft)
	);

	public final static transient Map<String, GradienTeaDomeSpec> NAMED = new LinkedHashMap<String, GradienTeaDomeSpec>(){{
		put("Planned GradienTea Dome", GRADIENTEA_DOME);
		put("Prototype GradienTea Dome", PROTOTYPE_DOME);
	}};
}
