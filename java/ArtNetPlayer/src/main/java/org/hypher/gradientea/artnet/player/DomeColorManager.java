package org.hypher.gradientea.artnet.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import toxi.color.ColorGradient;
import toxi.color.ColorList;
import toxi.color.ColorRange;
import toxi.color.Hue;
import toxi.color.TColor;
import toxi.color.theory.ColorTheoryRegistry;
import toxi.color.theory.ColorTheoryStrategy;

import java.awt.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeColorManager {
	private static DomeColorManager instance;

	public static DomeColorManager instance() {
		if (instance == null) {
			instance = new DomeColorManager();
		}
		return instance;
	}

	private final List<ColorTheoryStrategy> colorStrategies = ColorTheoryRegistry.getRegisteredStrategies();
	private final Cache<DomePaletteSpec, DomePalette> paletteCache = CacheBuilder.newBuilder()
		.maximumSize(1000)
		.build();

	public DomeColorManager() {
	}

	public DomePaletteSpec specFor(
		final float strategy,
		final float hue,
		final int colorCount
	) {
		return new DomePaletteSpec(
			colorStrategies.get(Math.min((int) (strategy*colorStrategies.size()), colorStrategies.size()-1)),
			Hue.getClosest(hue, false),
			colorCount
		);
	}

	public DomePalette paletteFor(
		final float strategy,
		final float hue,
		final int colorCount
	) {
		return paletteFor(specFor(strategy, hue, colorCount));
	}

	public DomePalette paletteFor(final DomePaletteSpec spec) {
		try {
			return paletteCache.get(
				spec,
				new Callable<DomePalette>() {
					@Override
					public DomePalette call() throws Exception {
						return buildPaletteForSpec(spec);
					}
				}
			);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private DomePalette buildPaletteForSpec(
		DomePaletteSpec spec
	) {
		TColor baseColor = ColorRange.BRIGHT.getColor(spec.getHue());
		ColorList colList = ColorList.createUsingStrategy(spec.getStrategy(), baseColor);
		int colorListCount  = colList.size();

		ColorGradient grad = new ColorGradient();
		for (int i=0; i<colList.size(); i++) {
			grad.addColorAt((float)(i)*spec.getColorCount() / colList.size(), colList.get(i));
		}

		//grad.addColorAt(spec.getColorCount()-1, colList.get(0));
		ColorList colList2 = grad.calcGradient(0, spec.getColorCount());

		Color[] colors = new Color[colList2.size()];
		for (int i=0; i<colList2.size(); i++) {
			colors[i] = new Color(colList2.get(i).toARGB());
		}

		return new DomePalette(spec, colors);
	}

	public static class DomePalette {
		private DomePaletteSpec spec;
		private Color[] colors;

		public DomePalette(final DomePaletteSpec spec, final Color[] colors) {
			this.spec = spec;
			this.colors = colors;
		}

		public DomePaletteSpec getSpec() {
			return spec;
		}

		public Color[] getColors() {
			return colors;
		}

		public Color getColor(float i) {
			return colors[Math.min(colors.length-1, (int)((i%1f) * colors.length))];
		}
	}

	public static class DomePaletteSpec {
		private ColorTheoryStrategy strategy;
		private Hue hue;
		private int colorCount;

		public DomePaletteSpec(final ColorTheoryStrategy strategy, final Hue hue, final int colorCount) {
			this.strategy = strategy;
			this.hue = hue;
			this.colorCount = colorCount;
		}

		public ColorTheoryStrategy getStrategy() {
			return strategy;
		}

		public Hue getHue() {
			return hue;
		}

		public int getColorCount() {
			return colorCount;
		}

		public String getHueName() {
			return hue.getName().substring(0,1).toUpperCase() + hue.getName().substring(1);
		}

		public String getStrategyName() {
			String split = strategy.getName().replaceAll("([a-z])([A-Z])", "$1 $2");
			return split.substring(0,1).toUpperCase() + split.substring(1);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final DomePaletteSpec that = (DomePaletteSpec) o;

			if (colorCount != that.colorCount) return false;
			if (Math.abs(hue.getHue() - that.getHue().getHue()) > 0.001) return false;
			if (!strategy.equals(that.strategy)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = strategy.hashCode();
			result = 31 * result + (int) (hue.getHue() * 200);
			result = 31 * result + colorCount;
			return result;
		}
	}
}
