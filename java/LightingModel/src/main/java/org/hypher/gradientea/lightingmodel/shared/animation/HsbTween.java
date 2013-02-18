package org.hypher.gradientea.lightingmodel.shared.animation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.hypher.gradientea.lightingmodel.shared.context.RenderingContext;
import org.hypher.gradientea.lightingmodel.shared.color.HsbColor;
import org.hypher.gradientea.lightingmodel.shared.color.PixelColor;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;
import org.hypher.gradientea.lightingmodel.shared.value.RenderingValue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A simple linear tween between two HSB colors
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class HsbTween implements Animation {
	protected Set<ColorStop> colorStops = Sets.newTreeSet();

	protected HsbTween() {}

	public HsbTween(List<ColorStop> colorStops) {
		this.colorStops.addAll(colorStops);
	}

	public HsbTween(ColorStop ... colorStops) {
		this.colorStops.addAll(Arrays.asList(colorStops));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> render(
		final RenderingContext renderingContext,
		final PixelGroup group,
		final double renderPosition
	) {
		if (colorStops.isEmpty()) return Collections.emptyList();

		// Where are we in the list
		Iterator<ColorStop> stopIterator = colorStops.iterator();
		ColorStop current = null;
		ColorStop next = null;
		double logicalNextPosition = 0;
		while (stopIterator.hasNext()) {
			current = stopIterator.next();

			if (current.position <= renderPosition || !stopIterator.hasNext()) {
				if (stopIterator.hasNext()) {
					next = stopIterator.next();
					logicalNextPosition = next.position;
				} else {
					next = colorStops.iterator().next();
					logicalNextPosition = 1 + next.position;
				}
			}
		}

		double percent = (renderPosition - current.position) / (logicalNextPosition - current.position);

		return group.applyColor(interpolate(
			current.color.resolve(renderingContext),
			next.color.resolve(renderingContext),
			percent
		));
	}

	private Double interpolate(Double first, Double second, double percent) {
		double a = first == null ? 0 : first;
		double b = second == null ? 0 : second;

		return a + (b-a) * percent;
	}

	private PixelColor interpolate(final HsbColor first, final HsbColor second, final double percent) {
		return new HsbColor(
			interpolate(first.getHue(), second.getHue(), percent),
			interpolate(first.getSaturation(), second.getSaturation(), percent),
			interpolate(first.getBrightness(), second.getBrightness(), percent)
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public String toString() {
		return "HsbTween{" +
			"colorStops=" + colorStops +
			'}';
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	public static class ColorStop implements Serializable, Comparable<ColorStop> {
		private double position;
		private RenderingValue.Color color;

		public ColorStop() {}

		public ColorStop(final double position, final RenderingValue.Color color) {
			Preconditions.checkArgument(position >= 0 && position <= 1, "position (%s) must be between 0 and 1 (inclusive)", position);

			this.position = position;
			this.color = color;
		}

		public double getPosition() {
			return position;
		}

		public RenderingValue.Color getColor() {
			return color;
		}

		@Override
		public int compareTo(final ColorStop o) {
			return Double.compare(position, o.position);
		}

		@Override
		public String toString() {
			return "ColorStop{" +
				"position=" + position +
				", color=" + color +
				'}';
		}
	}
}
