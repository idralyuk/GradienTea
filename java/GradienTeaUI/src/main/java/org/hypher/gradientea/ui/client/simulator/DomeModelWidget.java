package org.hypher.gradientea.ui.client.simulator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
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
	interface OurUiBinder extends UiBinder<Widget, DomeModelWidget> { }
	private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

	@UiField(provided = true)
	protected Canvas canvas = Canvas.createIfSupported();

	@UiField
	CheckBox autoRotateCheckbox;

	@UiField
	InlineLabel cameraDistanceLabel;
	@UiField
	InlineLabel cameraHeightLabel;

	/**
	 * Rotation speed in rotations per second
	 */
	protected double rotationsPerMinute = 2;

	protected GradienTeaDomeGeometry domeGeometry;

	protected List<GeoFace> faceList;

	protected GradienTeaDomeRenderer domeRenderer;

	protected boolean initialCameraValuesSet = false;


	public DomeModelWidget() {
		Preconditions.checkNotNull(canvas, "Canvas not supported");

		initWidget(ourUiBinder.createAndBindUi(this));
		domeRenderer = new GradienTeaDomeRenderer(canvas);

		CanvasMouseHandler handler = new CanvasMouseHandler();
		canvas.addMouseDownHandler(handler);
		canvas.addMouseMoveHandler(handler);
		canvas.addMouseUpHandler(handler);
		canvas.addMouseWheelHandler(handler);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	public void displayDome(GradienTeaDomeGeometry geometry) {
		domeGeometry = geometry;
		faceList = Ordering.from(GeoFace.arbitraryComparator).sortedCopy(
			ImmutableList.copyOf(geometry.getLightedFaces())
		);

		domeRenderer.renderDome(geometry);

		if (! initialCameraValuesSet) {
			initialCameraValuesSet = true;

			domeRenderer.setCameraHeightFeet(geometry.getHeight()/2);
			domeRenderer.setCameraViewHeightFeet(geometry.getHeight()/2);
			domeRenderer.setCameraDistanceFeet(geometry.getSpec().getRadius()*2.5);
		}

		onResize();
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

		renderFrame();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Setup Methods

	@Override
	public void onResize() {
		domeRenderer.setSize(
			getWidget().getElement().getClientWidth(),
			getWidget().getElement().getClientHeight()
		);

		renderFrame();
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

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// UI Handler Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Internal Methods

	protected double calculateCameraRotation() {
		return ((rotationsPerMinute * Duration.currentTimeMillis()) / (60 * 1000.0))%1.0 * Math.PI * 2;
	}

	protected void renderFrame() {
		if (autoRotateCheckbox.getValue()) {
			domeRenderer.setCameraAngleRadians(calculateCameraRotation());
		}

		cameraDistanceLabel.setText(NumberFormat.getDecimalFormat().format(
			domeRenderer.getCameraDistanceFeet()
		) + " ft");

		cameraHeightLabel.setText(NumberFormat.getDecimalFormat().format(
			domeRenderer.getCameraHeightFeet()
		) + " ft");

		domeRenderer.renderFrame();
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	protected final static double RADIANS_PER_PIXEL = Math.PI/(360*3);
	protected final static double HEIGHT_FEET_PER_PIXEL = 0.2;

	protected class CanvasMouseHandler implements MouseWheelHandler, MouseDownHandler, MouseUpHandler, MouseMoveHandler {
		protected boolean mouseDown = false;
		protected int downX;
		protected int downY;
		protected double downRotation;

		protected double downCameraY;
		protected double downCameraTargetY;

		@Override
		public void onMouseDown(final MouseDownEvent event) {
			mouseDown = true;
			downX = event.getClientX();
			downRotation = domeRenderer.getCameraAngleRadians();


			downY = event.getClientY();
			downCameraY = domeRenderer.getCameraHeightFeet();
			downCameraTargetY = domeRenderer.getCameraViewHeightFeet();
		}

		@Override
		public void onMouseMove(final MouseMoveEvent event) {
			if (mouseDown) {
				domeRenderer.setCameraAngleRadians(downRotation - (event.getClientX()-downX)*RADIANS_PER_PIXEL);

				double deltaY = (downY - event.getY());

				domeRenderer.setCameraHeightFeet(
					downCameraY - deltaY*HEIGHT_FEET_PER_PIXEL
				);
				domeRenderer.setCameraViewHeightFeet(
					downCameraTargetY + deltaY*HEIGHT_FEET_PER_PIXEL
				);
			}
		}

		@Override
		public void onMouseUp(final MouseUpEvent event) {
			mouseDown = false;
		}

		@Override
		public void onMouseWheel(final MouseWheelEvent event) {
			domeRenderer.setCameraDistanceFeet(
				Math.min(
					1000,
					Math.max(0.01, domeRenderer.getCameraDistanceFeet() + event.getDeltaY())
				)
			);

			event.stopPropagation();
			event.preventDefault();
		}
	}
}
