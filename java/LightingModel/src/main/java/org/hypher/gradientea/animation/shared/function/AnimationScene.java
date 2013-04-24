package org.hypher.gradientea.animation.shared.function;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A scene containing many animations.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AnimationScene implements DefinedAnimation {
	protected List<AnimationSceneEntry> entries;

	protected AnimationScene() {}

	public AnimationScene(final List<AnimationSceneEntry> entries) {
		this.entries = ImmutableList.copyOf(entries);
	}

	public static AnimationScene emptyScene() {
		return new AnimationScene(Collections.<AnimationSceneEntry>emptyList());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelValue> render(final double fraction) {
		List<PixelValue> values = Lists.newArrayList();

		for (AnimationSceneEntry entry : entries) {
			if (fraction >= entry.getStartTime() && fraction <= entry.getEndTime()) {
				values.addAll(entry.render(fraction));
			}
		}

		return ImmutableList.copyOf(values);
	}

	public AnimationScene with(DefinedAnimation animation, double start, double end) {
		return new AnimationScene(
			ImmutableList.<AnimationSceneEntry>builder()
				.addAll(entries)
				.add(new AnimationSceneEntry(animation, start, end))
				.build()
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	public static class AnimationSceneEntry {
		protected double startTime;
		protected double endTime;

		protected DefinedAnimation animation;

		protected AnimationSceneEntry() {}

		public AnimationSceneEntry(
			final DefinedAnimation animation,
			final double startTime,
			final double endTime
		) {
			Preconditions.checkArgument(endTime > startTime, "endTime (%s) must be greater than startTime (%s)", endTime, startTime);

			this.startTime = startTime;
			this.endTime = endTime;
			this.animation = animation;
		}

		public double getStartTime() {
			return startTime;
		}

		public double getEndTime() {
			return endTime;
		}

		public DefinedAnimation getAnimation() {
			return animation;
		}

		public Collection<? extends PixelValue> render(final double fraction) {
			return animation.render(
				(fraction-startTime) / (endTime-startTime)
			);
		}

		@Override
		public String toString() {
			return "AnimationSceneEntry{" +
				"startTime=" + startTime +
				", endTime=" + endTime +
				", animation=" + animation +
				'}';
		}
	}
}
