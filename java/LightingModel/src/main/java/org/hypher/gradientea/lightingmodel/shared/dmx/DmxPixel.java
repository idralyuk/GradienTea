package org.hypher.gradientea.lightingmodel.shared.dmx;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.hypher.gradientea.lightingmodel.shared.color.PixelColor;
import org.hypher.gradientea.lightingmodel.shared.pixel.ListPixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.Pixel;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelGroup;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;

import java.util.Collections;
import java.util.List;

/**
 * A pixel in a DMX multiverse.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DmxPixel implements Pixel {
	/**
	 * The DMX universe of the pixel.
	 */
	protected /*final*/ int universe;

	/**
	 * The DMX channel where the pixel is connected.
	 */
	protected /*final*/ int firstChannel;

	protected DmxPixel() {}

	public DmxPixel(final int universe, final int firstChannel) {
		Preconditions.checkArgument(universe >= 1 && universe <= 4, "Universe (%s) must be between 1 and 4 (inclusive)", universe);
		Preconditions.checkArgument(firstChannel >= 1 && firstChannel <= 510, "First Channel (%s) must be between 1 and 510 (inclusive)", firstChannel);

		this.universe = universe;
		this.firstChannel = firstChannel;
	}

	public static PixelGroup pixels(final int firstChannel, final int count) {
		Preconditions.checkArgument(firstChannel >= 1 && firstChannel <= (512*4)-(count*3), "First Channel (%s) must be between 1 and %s (inclusive)", firstChannel, (512*4)-(count*3));

		ImmutableList.Builder<DmxPixel> pixels = ImmutableList.builder();

		int currentUniverse = (firstChannel-1) / 512 + 1;
		int currentUniverseChannel = firstChannel - (currentUniverse-1)*512;

		if (currentUniverseChannel > 510) {
			currentUniverseChannel = 1;
			currentUniverse ++;
		}

		for (int pixelIndex=0; pixelIndex<count; pixelIndex++) {
			pixels.add(new DmxPixel(currentUniverse, currentUniverseChannel));

			currentUniverseChannel += 3;

			if (currentUniverseChannel > 510) {
				currentUniverseChannel = 1;
				currentUniverse ++;
			}
		}

		return new ListPixelGroup(pixels.build());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> applyColor(final PixelColor color) {
		return Collections.singletonList(new PixelValue(
			this,
			color
		));
	}

	@Override
	public List<PixelGroup> getChildren() {
		return Collections.<PixelGroup>singletonList(this);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final DmxPixel dmxPixel = (DmxPixel) o;

		if (firstChannel != dmxPixel.firstChannel) return false;
		if (universe != dmxPixel.universe) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = universe;
		result = 31 * result + firstChannel;
		return result;
	}

	@Override
	public String toString() {
		return "DmxPixel{" +
			"universe=" + universe +
			", firstChannel=" + firstChannel +
			'}';
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public int getUniverse() {
		return universe;
	}

	public int getFirstChannel() {
		return firstChannel;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
