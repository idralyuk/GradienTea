package org.hypher.gradientea.ui.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import org.hypher.gradientea.lightingmodel.shared.animation.ExpandedAnimationWrapper;
import org.hypher.gradientea.lightingmodel.shared.animation.HsbTween;
import org.hypher.gradientea.lightingmodel.shared.animation.SingleDefinedAnimation;
import org.hypher.gradientea.lightingmodel.shared.color.HsbColor;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxPixel;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoFace;
import org.hypher.gradientea.lightingmodel.shared.dome.GeodesicSphereGeometry;
import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeGeometry;
import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeSpec;
import org.hypher.gradientea.lightingmodel.shared.pixel.ListPixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;
import org.hypher.gradientea.lightingmodel.shared.rendering.RenderableAnimation;
import org.hypher.gradientea.ui.client.player.ClientDmxAnimationPlayer;
import org.hypher.gradientea.ui.client.simulator.DomeModelWidget;

import java.util.List;
import java.util.Map;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaUI implements EntryPoint {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	protected ClientDmxAnimationPlayer player;
	protected DomeModelWidget widget;

	protected InlineLabel faceCountLabel = new InlineLabel();
	protected TextBox animationVertex = new TextBox();

	@Override
	public void onModuleLoad() {
		DockLayoutPanel layoutPanel = new DockLayoutPanel(Style.Unit.EM);

		widget = new DomeModelWidget();
		player = new ClientDmxAnimationPlayer(widget);


		FlowPanel controls = new FlowPanel();

		final TextBox frequency = new TextBox();
		frequency.setText("5");

		final TextBox diameter = new TextBox();
		diameter.setText("50");

		final TextBox layers = new TextBox();
		layers.setText("8");

		final TextBox triangleSideLength = new TextBox();
		triangleSideLength.setText("2.33");

		animationVertex.setText("2");

		Button button = new Button("Update");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				setup(
					new GradienTeaDomeSpec(
						Integer.parseInt(frequency.getText()), // 4v
						Integer.parseInt(layers.getText()),
						Integer.parseInt(diameter.getText()) / 2, // radius
						Double.parseDouble(triangleSideLength.getText()), // standard panel size
						1d / 24 // 1/2 inch
					)
				);
			}
		});

		controls.add(new InlineLabel("Frequency (1-5):"));
		controls.add(frequency);

		controls.add(new InlineLabel("Diameter (ft):"));
		controls.add(diameter);

		controls.add(new InlineLabel("Layers (1 to 3*(f^2)):"));
		controls.add(layers);

		controls.add(new InlineLabel("Panel Size (2.33):"));
		controls.add(triangleSideLength);

		controls.add(new InlineLabel("Animation Vertex (0-11)"));
		controls.add(animationVertex);

		controls.add(button);

		controls.add(new InlineLabel("Faces:"));
		controls.add(faceCountLabel);

		layoutPanel.addSouth(controls, 5);
		layoutPanel.add(widget);

		RootLayoutPanel.get().add(layoutPanel);

		setup(
			new GradienTeaDomeSpec(
				Integer.parseInt(frequency.getText()), // 4v
				Integer.parseInt(layers.getText()),
				Integer.parseInt(diameter.getText()) / 2, // radius
				Double.parseDouble(triangleSideLength.getText()), // standard panel size
				1d / 24 // 1/2 inch
			)
		);
	}

	protected void setup(GradienTeaDomeSpec spec) {
		GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(spec);

		faceCountLabel.setText(Integer.toString(spec.calculateFaceCount()));

		widget.displayDome(geometry);

		// Calculate groups
		final List<GeoFace> faces = Ordering.from(GeoFace.arbitraryComparator).sortedCopy(
			ImmutableList.copyOf(
				geometry.getDomeGeometry()
					.getFaces()
			));

		int dmxChannel = 1;
		int dmxUniverse = 1;
		final Map<GeoFace, Integer[]> faceChannels = Maps.newHashMap();
		for (GeoFace face : faces) {
			faceChannels.put(face, new Integer[] {dmxUniverse, dmxChannel});

			dmxChannel += 3;
			if (dmxChannel > 510) {
				dmxChannel = 1;
				dmxUniverse ++;
			}
		}

		List<PixelGroup> ringGroups = Lists.newArrayList();
		for (List<GeoFace> ring : geometry.getDomeGeometry()
			.ringsFrom(GeodesicSphereGeometry.icosahedronVerticies[Integer.parseInt(animationVertex.getText())])) {
			List<DmxPixel> pixels = Lists.newArrayList();

			for (GeoFace face : ring) {
				pixels.add(new DmxPixel(faceChannels.get(face)[0], faceChannels.get(face)[1]));
			}

			ringGroups.add(new ListPixelGroup(pixels));
		}

		player.play(
			new RenderableAnimation(
				new SingleDefinedAnimation(
					new ExpandedAnimationWrapper(
						new HsbTween(new HsbColor(0, 0.8, .5), new HsbColor(1.0, 1.0, 1.0)),
						ExpandedAnimationWrapper.SIN,
						0.3
					),
					new ListPixelGroup(ringGroups)
				),
				5
			)
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
