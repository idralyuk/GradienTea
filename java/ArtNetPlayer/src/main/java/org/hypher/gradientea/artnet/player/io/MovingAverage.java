package org.hypher.gradientea.artnet.player.io;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.LinkedList;
import java.util.SortedSet;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class MovingAverage {
	private final int length;

	private SortedSet<Float> sortedValues = Sets.newTreeSet();
	private LinkedList<Float> orderedValues = Lists.newLinkedList();

	public MovingAverage(final int length) {
		Preconditions.checkArgument(length > 0, "Length must be a positive integer");

		this.length = length;
	}

	public synchronized void add(float value) {
		if (orderedValues.size() >= length) {
			sortedValues.remove(orderedValues.removeFirst());
		}

		sortedValues.add(value);
		orderedValues.add(value);
	}

	public synchronized float mean() {
		float sum = 0;
		int count = 0;

		for (Float value : sortedValues) {
			if (value.isInfinite() || value.isNaN()) continue;

			sum += value;
			count ++;
		}

		if (count == 0) return 0.0f;
		else return sum / count;
	}

	public synchronized float lowest() {
		if (sortedValues.isEmpty()) return 0;

		return sortedValues.first();
	}

	public synchronized float highest() {
		if (sortedValues.isEmpty()) return 0;

		return sortedValues.last();
	}

	public synchronized float scale(float v) {
		if (sortedValues.isEmpty()) return 0;

		if (v <= lowest()) return 0;
		if (v >= highest()) return 1.0f;

		return (v - lowest()) / (highest() - lowest());
	}

	public synchronized void clear() {
		sortedValues.clear();
		orderedValues.clear();
	}
}
