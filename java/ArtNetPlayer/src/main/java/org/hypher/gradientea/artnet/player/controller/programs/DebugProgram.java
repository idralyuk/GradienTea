package org.hypher.gradientea.artnet.player.controller.programs;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.animation.shared.color.RgbColor;
import org.hypher.gradientea.artnet.player.controller.DomeOutput;
import org.hypher.gradientea.artnet.player.controller.OscConstants;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GeoVector3;

import java.util.Arrays;
import java.util.List;

/**
 * Program which enables turning specific pixels on and off for wiring, testing and debugging.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DebugProgram extends BaseDomeProgram {
	private OscHelper.OscBoolean dome1Enabled = OscHelper.booleanValue(OscConstants.Control.Debug.ENABLE_1, false);
	private OscHelper.OscBoolean dome2Enabled = OscHelper.booleanValue(OscConstants.Control.Debug.ENABLE_2, false);

	private OscHelper.OscDouble singlePanelFraction = OscHelper.doubleValue(OscConstants.Control.Debug.SINGLE_PANEL_INDEX, 0, 1, 0);
	private OscHelper.OscDouble singlePanelLabel = OscHelper.doubleValue(OscConstants.Control.Debug.SINGLE_PANEL_LABEL, 0, 1, 0);
	private OscHelper.OscBoolean allPanelsRed = OscHelper.booleanValue(OscConstants.Control.Debug.ALL_PANELS_RED, false);
	private OscHelper.OscBoolean allPanelsGreen = OscHelper.booleanValue(OscConstants.Control.Debug.ALL_PANELS_GREEN, false);
	private OscHelper.OscBoolean allPanelsBlue = OscHelper.booleanValue(OscConstants.Control.Debug.ALL_PANELS_BLUE, false);

	private OscHelper.OscDouble singleVertexFraction = OscHelper.doubleValue(OscConstants.Control.Debug.SINGLE_VERTEX_INDEX, 0, 1, 0);
	private OscHelper.OscDouble singleVertexLabel = OscHelper.doubleValue(OscConstants.Control.Debug.SINGLE_VERTEX_LABEL, 0, 1, 0);
	private OscHelper.OscBoolean allVerticesRed = OscHelper.booleanValue(OscConstants.Control.Debug.ALL_VERTICES_RED, false);
	private OscHelper.OscBoolean allVerticesGreen = OscHelper.booleanValue(OscConstants.Control.Debug.ALL_VERTICES_GREEN, false);
	private OscHelper.OscBoolean allVerticesBlue = OscHelper.booleanValue(OscConstants.Control.Debug.ALL_VERTICES_BLUE, false);

	private int lastPanelIndex = 0;
	private int lastVertexIndex = 0;


	public DebugProgram() {
		super(ProgramId.DEBUG);
	}


	@Override
	protected void initialize() {
	}

	@Override
	public boolean isFocusDesired() {
		return dome1Enabled.value() || dome2Enabled.value();
	}

	@Override
	public void start() {
	}

	@Override
	public void update() {
		// Get the selected output
		List<DomeOutput> selectedOutputs = FluentIterable.from(Arrays.asList(
			dome1Enabled.value() ? controller.getOutputs().get(0) : null,
			dome2Enabled.value() ? controller.getOutputs().get(1) : null
		)).filter(Predicates.notNull()).toImmutableList();

		for (DomeOutput output : selectedOutputs) {
			output.getCanvas().clear();

			if (allPanelsRed.value() || allPanelsGreen.value() || allPanelsBlue.value()) {
				paintAllPanels(output, allPanelsRed.value(), allPanelsGreen.value(), allPanelsBlue.value());
			} else {
				paintSinglePanel(output, singlePanelFraction.getValue());
			}

			if (allVerticesRed.value() || allVerticesGreen.value() || allVerticesBlue.value()) {
				paintAllVertices(output, allVerticesRed.value(), allVerticesGreen.value(), allVerticesBlue.value());
			} else {
				paintSingleVertex(output, singleVertexFraction.getValue());
			}
		}
	}

	private void paintSinglePanel(
		final DomeOutput output, 
		final double panelFractionalIndex
	) {
		final int index = (int) (output.getGeometry().getLightedFaces().size() * (panelFractionalIndex % 1.0));
		final GeoFace panel = Iterables.get(
			output.getGeometry().getLightedFaces(),
			index
		);
		singlePanelLabel.setValue(index);
		if (index != lastPanelIndex) {
			lastPanelIndex = index;
			OscHelper.instance().pushToKnownHosts();
		}
		
		output.getCanvas().clear();
		output.getCanvas().draw(panel, panelFractionalIndex, 1.0, 1.0);
	}

	private void paintAllPanels(
		final DomeOutput output,
		final boolean red,
		final boolean green,
		final boolean blue
	) {
		final RgbColor color = new RgbColor(
			red ? 255 : 0,
			green ? 255 : 0,
			blue ? 255 : 0
		);

		output.getCanvas().clear();
		output.getCanvas().draw(output.getGeometry().getLightedFaces(), color);
	}


	private void paintSingleVertex(
		final DomeOutput output,
		final double fractionalIndex
	) {
		final int index = (int) (output.getGeometry().getLightedVertices().size() * (fractionalIndex % 1.0));
		final GeoVector3 vertex = Iterables.get(
			output.getGeometry().getLightedVertices(),
			index
		);

		singleVertexLabel.setValue(index);
		if (index != lastVertexIndex) {
			lastVertexIndex = index;
			OscHelper.instance().pushToKnownHosts();
		}

		output.getCanvas().draw(vertex, new HsbColor(fractionalIndex, 1.0, 1.0));
	}

	private void paintAllVertices(
		final DomeOutput output,
		final boolean red,
		final boolean green,
		final boolean blue
	) {
		final RgbColor color = new RgbColor(
			red ? 255 : 0,
			green ? 255 : 0,
			blue ? 255 : 0
		);

		output.getCanvas().drawVertices(output.getGeometry().getLightedVertices(), color);
	}

	@Override
	public void stop() {
	}
}
