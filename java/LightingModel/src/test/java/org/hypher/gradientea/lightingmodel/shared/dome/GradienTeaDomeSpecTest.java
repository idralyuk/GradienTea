package org.hypher.gradientea.lightingmodel.shared.dome;

import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaDomeSpecTest {
	@Test
	public void testFaceCalculation() {
		// A 1v dome
		for (int i=0; i<=3; i++) {
			GradienTeaDomeSpec spec = new GradienTeaDomeSpec(1, i, 0, 1, 1, 1);

			int layers = 0;
			if (i > 0) layers += 5;
			if (i > 1) layers += 10;
			if (i > 2) layers += 5;

			Assert.assertThat(spec.getFrequency() +"v dome of " + i + " layer face count", spec.faceCount(), equalTo(layers));
		}

		// A 2v dome
		for (int i=0; i<=6; i++) {
			GradienTeaDomeSpec spec = new GradienTeaDomeSpec(2, i, 0, 1, 1, 1);

			int layers = 0;

			// Top
			if (i > 0) layers += 5 * 1;
			if (i > 1) layers += 5 * 3;

			// Middle
			if (i > 2) layers += 5 * 1 + 5 * 3;
			if (i > 3) layers += 5 * 1 + 5 * 3;

			// Bottom
			if (i > 4) layers += 5 * 3;
			if (i > 5) layers += 5 * 1;

			Assert.assertThat(spec.getFrequency() +"v dome of " + i + " layer face count", spec.faceCount(), equalTo(layers));
		}
	}
}
