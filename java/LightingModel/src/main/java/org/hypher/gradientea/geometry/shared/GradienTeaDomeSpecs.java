package org.hypher.gradientea.geometry.shared;

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
		19, // Radius (ft)
		2, // Panel Height (ft)
		8 /*mm*/ / 25.4 / 12 // Panel thickness (ft)
	);

	public final static transient GradienTeaDomeSpec GRADIENTEA_SPHERE = new GradienTeaDomeSpec(
		5, // Frequency
		15, // Layers
		15, // Lighted Layers
		19, // Radius (ft)
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

	public final static transient GradienTeaDomeSpec GRADIENTEA_QUAD = new GradienTeaDomeSpec(
		9, // Frequency
		14, // Layers
		13, // Lighted Layers
		19, // Radius (ft)
		1, // Panel Height (ft)
		8 /*mm*/ / 25.4 / 12 // Panel thickness (ft)
	);

	public final static transient Map<String, GradienTeaDomeSpec> NAMED = new LinkedHashMap<String, GradienTeaDomeSpec>(){{
		put("Planned GradienTea Dome", GRADIENTEA_DOME);
		put("Prototype GradienTea Dome", PROTOTYPE_DOME);
		put("GradienTea Sphere", GRADIENTEA_SPHERE);
		put("GradienTea Quad", GRADIENTEA_QUAD);
	}};
}
