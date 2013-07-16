package org.hypher.gradientea.artnet.player.controller.programs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.animation.shared.color.PixelColor;
import org.hypher.gradientea.animation.shared.color.RgbColor;
import org.hypher.gradientea.artnet.player.controller.DomeOutput;
import org.hypher.gradientea.artnet.player.controller.OscConstants;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;

import java.util.List;

/**
 * An auxiliary program that provides illumination for the door lights by assigning it's color to the brightest
 * color on the dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DoorLightAnimation extends BaseDomeAnimation {
	private static final double FADE_FRACTION = 0.1;

	private List<SingleDomeControl> domeControls;
	private OscHelper.OscDouble door1Index = OscHelper.doubleValue(OscConstants.Control.Door.INDEX_1, 0, 1, 0);
	private OscHelper.OscDouble door1Label = OscHelper.doubleValue(OscConstants.Control.Door.INDEX_1_LABEL, 0, 1, 0);

	private OscHelper.OscDouble door2Index = OscHelper.doubleValue(OscConstants.Control.Door.INDEX_2, 0, 1, 0);
	private OscHelper.OscDouble door2Label = OscHelper.doubleValue(OscConstants.Control.Door.INDEX_2_LABEL, 0, 1, 0);

	@Override
	protected void initialize() {
		domeControls = ImmutableList.of(
			new SingleDomeControl(
				door1Index,
				door1Label,
				controller.getOutputs().get(0)
			),

			new SingleDomeControl(
				door2Index,
				door2Label,
				controller.getOutputs().get(1)
			)
		);
	}

	@Override
	public void start() {
	}

	@Override
	public void update() {
		if (domeControls != null) {
			for (SingleDomeControl control : domeControls) {
				control.update();
			}
		}
	}

	class SingleDomeControl {
		protected OscHelper.OscDouble doorFractionalIndex;
		private OscHelper.OscDouble doorIndexLabel;
		protected DomeOutput domeOutput;
		protected int[] targetColor = new int[] {0,0,0};
		protected double[] currentColor = new double[] {0,0,0};

		protected int lastIndex = 0;

		SingleDomeControl(
			final OscHelper.OscDouble doorFractionalIndex,
			final OscHelper.OscDouble doorIndexLabel,
			final DomeOutput domeOutput
		) {
			this.doorFractionalIndex = doorFractionalIndex;
			this.doorIndexLabel = doorIndexLabel;
			this.domeOutput = domeOutput;
		}

		public void update() {
			updateTargetColor();
			updateCurrentColor();

			int index = (int) (domeOutput.getGeometry().getLightedFaces().size() * (doorFractionalIndex.getValue() % 1.0));

			if (index != lastIndex) {
				lastIndex = index;
				doorIndexLabel.setValue(index);
				OscHelper.instance().pushToKnownHosts();
			}

			domeOutput.getCanvas().draw(
				Iterables.get(
					domeOutput.getGeometry().getLightedFaces(),
					index
				),
				new RgbColor(
					currentColor[0]*.25,
					currentColor[2]*.25,
					currentColor[1]*.25
				)
			);
		}

		private void updateCurrentColor() {
			currentColor[0] = currentColor[0] + (targetColor[0] - currentColor[0]) * FADE_FRACTION;
			currentColor[1] = currentColor[1] + (targetColor[1] - currentColor[1]) * FADE_FRACTION;
			currentColor[2] = currentColor[2] + (targetColor[2] - currentColor[2]) * FADE_FRACTION;
		}

		private void updateTargetColor() {
			double highestBrightness = 0;
			PixelColor brightestColor = null;

			for (PixelColor color :
				Iterables.concat(
					domeOutput.getCanvas().getFaceColorMap().values(),
					domeOutput.getCanvas().getVertexColorMap().values()
				)
				) {
				HsbColor hsbColor = HsbColor.hsbColor(color);
				if (hsbColor.getBrightness() > highestBrightness) {
					brightestColor = hsbColor;
					highestBrightness = hsbColor.getBrightness();
				}
			}

			if (highestBrightness > 0.5) {
				targetColor = brightestColor.asRgb();
			}
		}
	}

	@Override
	public void stop() {
	}
}
