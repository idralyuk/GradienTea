package org.hypher.gradientea.ui.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeSpec;
import org.hypher.gradientea.lightingmodel.shared.versions.GradienTeaDomeSpecs;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeSpecEditor extends Composite {
	interface DomeSpecSelectorUiBinder extends UiBinder<Widget, DomeSpecEditor> { }
	private static DomeSpecSelectorUiBinder ourUiBinder = GWT.create(DomeSpecSelectorUiBinder.class);

	@UiField
	TextBox frequencyInput;

	@UiField
	TextBox diameterInput;

	@UiField
	TextBox layersInput;

	@UiField
	InlineLabel maxLayersLabel;

	@UiField
	TextBox unlightedLayersInput;

	@UiField
	InlineLabel maxUnlightedLayersLabel;

	@UiField
	TextBox maxPanelHeightInput;

	public DomeSpecEditor() {
		initWidget(ourUiBinder.createAndBindUi(this));

		applySpec(GradienTeaDomeSpecs.MEDIUM_DOME_LARGE_PANELS);

		final ChangeHandler updateLabelsChangeHandler = new ChangeHandler() {
			@Override
			public void onChange(final ChangeEvent event) {
				updateLabels();
			}
		};

		frequencyInput.addChangeHandler(updateLabelsChangeHandler);
		layersInput.addChangeHandler(updateLabelsChangeHandler);
	}

	public void applySpec(GradienTeaDomeSpec spec) {
		this.frequencyInput.setText(Integer.toString(spec.getFrequency()));
		this.diameterInput.setText(Double.toString(spec.getRadius() * 2));
		this.layersInput.setText(Integer.toString(spec.getLayers()));
		this.unlightedLayersInput.setText(Integer.toString(spec.getLayers() - spec.getLightedLayers()));
		this.maxPanelHeightInput.setText(Double.toString(spec.getMaxPanelHeight()));

		updateLabels();
	}

	public GradienTeaDomeSpec buildSpec() {
		final int layers = intValue(layersInput, 1);

		return new GradienTeaDomeSpec(
			intValue(frequencyInput, 1),
			layers,
			layers - intValue(unlightedLayersInput, 1),
			doubleValue(diameterInput, 20) / 2,
			doubleValue(maxPanelHeightInput, 2),
			0.0328084 // 1cm in feet
		);
	}

	private void updateLabels() {
		int frequency = intValue(frequencyInput, 1);
		int maxLayers = 3 * frequency * frequency;

		maxLayersLabel.setText("(1 to " + (maxLayers) + ")");

		int layers = intValue(layersInput, maxLayers / 2);

		maxUnlightedLayersLabel.setText("(0 to " + (layers) + ")");
	}

	protected int intValue(HasText widget, int def) {
		try {
			return Integer.parseInt(widget.getText());
		} catch (NumberFormatException e) {
			return def;
		}
	}

	protected double doubleValue(HasText widget, double def) {
		try {
			return Double.parseDouble(widget.getText());
		} catch (NumberFormatException e) {
			return def;
		}
	}
}