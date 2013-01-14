package org.hypher.gradientea.ui.client.simulator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoFace;
import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeGeometry;
import org.hypher.gradientea.ui.client.player.DmxInterface;

import java.util.List;

/**
 * Renders a {@link org.hypher.gradientea.lightingmodel.shared.rendering.RenderableAnimation} onto a model
 * of a geodesic dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeModelWidget extends Composite implements RequiresResize, DmxInterface {

	/**
	 * Rotation speed in rotations per second
	 */
	protected double rotationsPerMinute = 2;

	protected GradienTeaDomeGeometry domeGeometry;

	protected List<GeoFace> faceList;

	protected GradienTeaDomeRenderer domeRenderer;

	protected Canvas canvas = Canvas.createIfSupported();

	protected LayoutPanel layout = new LayoutPanel();

	public DomeModelWidget() {
		Preconditions.checkNotNull(canvas, "Canvas not supported");

		layout.add(canvas);
		initWidget(layout);


		domeRenderer = new GradienTeaDomeRenderer(canvas);
	}

	public void displayDome(GradienTeaDomeGeometry geometry) {
		domeGeometry = geometry;
		faceList = Ordering.from(GeoFace.arbitraryComparator).sortedCopy(
			ImmutableList.copyOf(geometry.getLightedFaces())
		);

		domeRenderer.renderDome(geometry);
		onResize();
	}

	protected double calculateCameraRotation() {
		return ((rotationsPerMinute * Duration.currentTimeMillis()) / (60 * 1000.0))%1.0 * Math.PI * 2;
	}

	@Override
	public void onResize() {
		domeRenderer.setSize(
			layout.getElement().getClientWidth(),
			layout.getElement().getClientHeight()
		);

		domeRenderer.renderFrame(calculateCameraRotation(), false);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
			@Override
			public void execute(final double timestamp) {
				onResize();
			}
		});
	}

	@Override
	public void display(final int[][] dmxChannelValues) {
		int faceIndex = 0;
		universeLoop:
		for (int u=0; u<dmxChannelValues.length && faceIndex < faceList.size(); u++) {
			int[] universe = dmxChannelValues[u];

			for (int c=0; c<universe.length-3 && faceIndex < faceList.size(); c += 3, faceIndex ++) {
				domeRenderer.applyFaceColor(
					faceList.get(faceIndex),
					universe[c],
					universe[c+1],
					universe[c+2]
				);
			}
		}

		domeRenderer.renderFrame(calculateCameraRotation(), false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

}
