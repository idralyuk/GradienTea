package org.hypher.gradientea.ui.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeGeometry;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeInfoPanel extends Composite {
	interface DomeSpecSelectorUiBinder extends UiBinder<Widget, DomeInfoPanel> { }
	private static DomeSpecSelectorUiBinder ourUiBinder = GWT.create(DomeSpecSelectorUiBinder.class);

	@UiField
	InlineLabel heightLabel;

	@UiField
	InlineLabel totalStrutLengthLabel;

	@UiField
	InlineLabel jointCountLabel;

	@UiField
	InlineLabel strutCountLabel;

	@UiField
	InlineLabel faceCountLabel;

	@UiField
	InlineLabel lightedFaceCountLabel;

	@UiField
	InlineLabel singlePanelAreaLabel;

	@UiField
	InlineLabel totalPanelAreaLabel;

	public DomeInfoPanel() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public void showGeometryInfo(GradienTeaDomeGeometry geometry) {
		setValue(heightLabel, geometry.getHeight());
		setValue(jointCountLabel, geometry.getDomeGeometry().getVertices().size());

		setValue(strutCountLabel, geometry.getDomeGeometry().getEdges().size());
		setValue(totalStrutLengthLabel, geometry.getTotalStrutLength());

		setValue(faceCountLabel, geometry.getDomeGeometry().getFaces().size());

		setValue(lightedFaceCountLabel, geometry.getLightedFaces().size());
		setValue(singlePanelAreaLabel, geometry.getAveragePanelArea());
		setValue(totalPanelAreaLabel, geometry.getLightedFaces().size() * geometry.getAveragePanelArea());
	}

	protected void setValue(HasText hasText, double value) {
		hasText.setText(NumberFormat.getDecimalFormat().format(value));
	}
}