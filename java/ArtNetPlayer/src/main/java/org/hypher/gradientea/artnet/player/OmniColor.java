package org.hypher.gradientea.artnet.player;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class OmniColor {
	public final static OmniColor instance = new OmniColor();

	protected double[] mappings = new double[] {
		/*0*/ 1, 1, 1, 1, 1, 1, 1, 1, // Red to Green
		/*8*/ 1, 1, 1, 1, 1, 1, 1, 1, // Green to Blue
		/*16*/ 1, 1, 1, 1, 1, 1, 1, 1, // Blue to Red
	};

	protected double[] mappingSums;
	protected double mappingSum;

	protected void add(int start, int end, double value) {
		for (int i=start; i<=end; i++) {
			mappings[i] += value;
		}
	}

	{
		add(0, 1, 1.0);
		add(3, 3, 1.0);
		add(6, 11, 1.0);
		add(12, 18, 0.6);
		add(16, 18, 1.0);
		add(15, 15, 1.5);
		add(21, 22, -0.5);
		add(4, 4, -1);
		add(7, 7, 1);

		add(19, 19, 1.0);
		add(2, 4, 1.0);
		add(22, 23, 0.5);

		updateMapping();
	}

	private void updateMapping() {
		mappingSums = new double[mappings.length];
		mappingSum = 0.0;

		for (int i=0; i<mappings.length; i++) {
			mappingSum += mappings[i];
			mappingSums[i] = mappingSum;
		}
	}

	public double mappingCellForHue(double inputHue) {
		double mappedPoint = inputHue * mappingSum;

		int cellIndex = 0;
		for (;cellIndex<mappings.length && mappedPoint >= mappingSums[cellIndex]; cellIndex++);

		if (cellIndex >= mappingSums.length) cellIndex = mappingSums.length-1;

		double cellStart = (cellIndex == 0 ? 0 : mappingSums[cellIndex-1]);
		double cellEnd = mappingSums[cellIndex];

		double fractionalIndex = cellIndex + (mappedPoint - cellStart) / (cellEnd - cellStart);

		return fractionalIndex;
	}

	public double mapHue(double hue) {
		return mappingCellForHue(hue) / mappings.length;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
