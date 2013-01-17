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

import java.util.SortedSet;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeInfoPanel extends Composite {
	interface OurUiBinder extends UiBinder<Widget, DomeInfoPanel> { }
	private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

	@UiField
	InlineLabel heightLabel;

	@UiField
	InlineLabel floorDiameterLabel;

	@UiField
	InlineLabel floorAreaLabel;

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
	@UiField
	InlineLabel strutLengthsLabel;


	public DomeInfoPanel() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public void showGeometryInfo(GradienTeaDomeGeometry geometry) {
		setValue(heightLabel, geometry.getHeight());
		setValue(floorDiameterLabel, geometry.getFloorRadius() * 2);
		setValue(floorAreaLabel, geometry.getFloorArea());

		setValue(jointCountLabel, geometry.getDomeGeometry().getVertices().size());

		setValue(strutCountLabel, geometry.getDomeGeometry().getEdges().size());
		setLengthsValue(strutLengthsLabel, geometry.getStrutLengths());
		setValue(totalStrutLengthLabel, geometry.getTotalStrutLength());

		setValue(faceCountLabel, geometry.getDomeGeometry().getFaces().size());

		setValue(lightedFaceCountLabel, geometry.getLightedFaces().size());
		setValue(singlePanelAreaLabel, geometry.getAveragePanelArea());
		setValue(totalPanelAreaLabel, geometry.getLightedFaces().size() * geometry.getAveragePanelArea());
	}

	private void setLengthsValue(final HasText hasText, final SortedSet<Double> values) {
		if (values.isEmpty()) {
			hasText.setText("");
		} else {
			StringBuilder builder = new StringBuilder(values.size() * 8);

			final NumberFormat decimalFormat = NumberFormat.getDecimalFormat();

			for (Double value : values) {
				int feet = (int) Math.floor(value);
				double inches = (value - feet) * 12;


				builder.append(feet).append("' ");
				builder.append(decimalFormat.format(inches)).append("\"");
				builder.append(", ");
			}

			builder.setLength(builder.length()-2);

			hasText.setText(builder.toString());
		}
	}

	protected void setValue(HasText hasText, double value) {
		hasText.setText(NumberFormat.getDecimalFormat().format(value));
	}
}