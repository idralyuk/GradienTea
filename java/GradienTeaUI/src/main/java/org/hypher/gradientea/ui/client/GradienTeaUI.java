package org.hypher.gradientea.ui.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpec;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;
import org.hypher.gradientea.transport.shared.DomeAnimationFrame;
import org.hypher.gradientea.ui.client.gin.GradienTeaGinjector;
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

	public final static GradienTeaGinjector ginjector = GWT.create(GradienTeaGinjector.class);

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

		byte[] frame = new byte[3 * 225];
		List<Integer> layerTransitions = ImmutableList.of(
			5,
			5 + 5*3,
			5 + 5*3 + 5*5,
			5 + 5*3 + 5*5 + 5*7,
			5 + 5*3 + 5*5 + 5*7 + 5*9,
			5 + 5*3 + 5*5 + 5*7 + 5*9 + 5*9 + 5,
			225,
			275
		);

		for (
			int i=0, layer=0, layerI=0, layerCount=5;
			i<225;
			i++,
			layerI++,
			layer += layerTransitions.contains(i) ? 1 : 0,
			layerI = layerTransitions.contains(i) ? 0 : layerI,
			layerCount = layerTransitions.contains(i) ? (layerTransitions.get(layerTransitions.indexOf(i)+1) - i) : layerCount
		) {
			int[] color = new HsbColor(
				layer / 6.0,
				1.0,
				(double)layerI / layerCount
			).asRgb();

			frame[i*3+0] = (byte) color[0];
			frame[i*3+1] = (byte) color[1];
			frame[i*3+2] = (byte) color[2];
		}

		widget.displayFrame(new DomeAnimationFrame(frame));

		new Timer(){
			@Override
			public void run() {
				widget.renderFrame();
			}
		}.scheduleRepeating(1000/30);

		ginjector.getDomeAnimationCometTransport().start(widget);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
