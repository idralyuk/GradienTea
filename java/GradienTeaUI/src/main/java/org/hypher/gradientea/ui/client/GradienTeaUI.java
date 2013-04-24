package org.hypher.gradientea.ui.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import org.hypher.gradientea.animation.shared.function.ExpandedAnimationWrapper;
import org.hypher.gradientea.animation.shared.function.HsbTween;
import org.hypher.gradientea.animation.shared.function.SingleDefinedAnimation;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxPixel;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GeodesicSphereGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpec;
import org.hypher.gradientea.animation.shared.pixel.ListPixelGroup;
import org.hypher.gradientea.animation.shared.pixel.PixelGroup;
import org.hypher.gradientea.animation.shared.RenderableAnimation;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;
import org.hypher.gradientea.ui.client.player.ClientDmxAnimationPlayer;
import org.hypher.gradientea.ui.client.simulator.DomeModelWidget;
import org.hypher.gradientea.ui.client.widgets.DomeInfoPanel;
import org.hypher.gradientea.ui.client.widgets.DomeSpecEditor;

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

	protected DomeSpecEditor specEditor = new DomeSpecEditor();
	protected DomeInfoPanel domeInfoPanel = new DomeInfoPanel();

	@Override
	public void onModuleLoad() {
		DockLayoutPanel layoutPanel = new DockLayoutPanel(Style.Unit.EM);

		widget = new DomeModelWidget();
		player = new ClientDmxAnimationPlayer(widget);


		FlowPanel controls = new FlowPanel();

		final ListBox presets = new ListBox();
		for (String name : GradienTeaDomeSpecs.NAMED.keySet()) {
			presets.addItem(name);
		}
		presets.setSelectedIndex(0);

		presets.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(final ChangeEvent event) {
				specEditor.applySpec(GradienTeaDomeSpecs.NAMED.get(presets.getItemText(presets.getSelectedIndex())));
				setup();
			}
		});

		specEditor.applySpec(GradienTeaDomeSpecs.NAMED.get(presets.getItemText(presets.getSelectedIndex())));

		Button updateButton = new Button("Update");
		updateButton.addClickHandler(
			new ClickHandler() {
				@Override
				public void onClick(final ClickEvent event) {
					setup();
				}
			}
		);

		controls.add(presets);

		controls.add(specEditor);
		controls.add(updateButton);
		controls.add(domeInfoPanel);

		layoutPanel.addWest(controls, 20);
		layoutPanel.add(widget);

		RootLayoutPanel.get().add(layoutPanel);

		setup();
	}

	protected void setup() {
		GradienTeaDomeSpec spec = specEditor.buildSpec();

		GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(spec);
		domeInfoPanel.showGeometryInfo(geometry);

		widget.displayDome(geometry);

		// Calculate groups
		final List<GeoFace> faces = Ordering.from(GeoFace.arbitraryComparator).sortedCopy(
			ImmutableList.copyOf(
				geometry.getLightedFaces()
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
			.ringsFrom(GeodesicSphereGeometry.icosahedronVerticies[2])) {
			List<DmxPixel> pixels = Lists.newArrayList();

			for (GeoFace face : ring) {
				if (faces.contains(face)) {
					pixels.add(new DmxPixel(faceChannels.get(face)[0], faceChannels.get(face)[1]));
				}
			}

			ringGroups.add(new ListPixelGroup(pixels));
		}

		player.play(
			new RenderableAnimation(
				new SingleDefinedAnimation(
					new ExpandedAnimationWrapper(
						new HsbTween(new HsbColor(0, 1.0, 1.0), new HsbColor(1.0, 1.0, 1.0)),
						ExpandedAnimationWrapper.TRIANGLE,
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
