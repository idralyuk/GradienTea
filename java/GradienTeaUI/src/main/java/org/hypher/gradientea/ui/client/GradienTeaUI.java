package org.hypher.gradientea.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import org.hypher.gradientea.lightingmodel.shared.animation.ExpandedAnimationWrapper;
import org.hypher.gradientea.lightingmodel.shared.animation.HsbTween;
import org.hypher.gradientea.lightingmodel.shared.animation.SingleDefinedAnimation;
import org.hypher.gradientea.lightingmodel.shared.color.HsbColor;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxPixel;
import org.hypher.gradientea.lightingmodel.shared.dome.DomeSpecification;
import org.hypher.gradientea.lightingmodel.shared.rendering.RenderableAnimation;
import org.hypher.gradientea.ui.client.player.ClientDmxAnimationPlayer;
import org.hypher.gradientea.ui.client.simulator.DomeModelWidget;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GradienTeaUI implements EntryPoint {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	protected ClientDmxAnimationPlayer player;
	protected DomeModelWidget widget;

	@Override
	public void onModuleLoad() {
		DockLayoutPanel layoutPanel = new DockLayoutPanel(Style.Unit.EM);

		widget = new DomeModelWidget();
		player = new ClientDmxAnimationPlayer(widget);


		FlowPanel controls = new FlowPanel();

		final TextBox frequency = new TextBox();
		frequency.setText("5");

		final TextBox diameter = new TextBox();
		diameter.setText("40");

		final TextBox layers = new TextBox();
		layers.setText("10");

		Button button = new Button("Update");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				setup(
					Integer.parseInt(frequency.getText()),
					Integer.parseInt(diameter.getText()),
					Integer.parseInt(layers.getText())
				);
			}
		});

		controls.add(frequency);
		controls.add(diameter);
		controls.add(layers);
		controls.add(button);

		layoutPanel.addSouth(controls, 5);
		layoutPanel.add(widget);

		RootLayoutPanel.get().add(layoutPanel);

		setup(
			Integer.parseInt(frequency.getText()),
			Integer.parseInt(diameter.getText()),
			Integer.parseInt(layers.getText())
		);
	}

	protected void setup(int frequency, double diameter, int layers) {
		widget.displayDome(new DomeSpecification(
			frequency, // 4v
			layers,
			diameter/2, // radius
			2.33, // standard panel size
			1d/24 // 1/2 inch
		));

		player.play(
			new RenderableAnimation(
				new SingleDefinedAnimation(
					new ExpandedAnimationWrapper(
						new HsbTween(new HsbColor(0, 1.0, .5), new HsbColor(1.0, 1.0, 1.0)),
						ExpandedAnimationWrapper.SIN,
						1.0
					),
					DmxPixel.pixels(1, (int) (20 * Math.pow(frequency, 2)))
				),
				10
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
