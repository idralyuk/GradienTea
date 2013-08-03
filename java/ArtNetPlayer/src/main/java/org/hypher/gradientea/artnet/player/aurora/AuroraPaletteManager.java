package org.hypher.gradientea.artnet.player.aurora;

import toxi.color.ColorGradient;
import toxi.color.ColorList;
import toxi.color.ColorRange;
import toxi.color.Hue;
import toxi.color.TColor;
import toxi.color.theory.ColorTheoryRegistry;
import toxi.color.theory.ColorTheoryStrategy;

import java.awt.*;
import java.util.ArrayList;

/**
 * This palette manager is borrowed and adapted from the AuroraLEDwall here: https://github.com/gregfriedland/AuroraLEDwall
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AuroraPaletteManager {
	Palette[] kPs;
	int kPalInd = 0;
	int PT_KULER=0;
	int paletteType = PT_KULER;
	ArrayList ptStrategies = ColorTheoryRegistry.getRegisteredStrategies();
	int NUM_PT = 1 + ptStrategies.size();
	int basePaletteColors = -1;
	private Palette hsbPalette;

	public AuroraPaletteManager() {
		kPs = new Palette[21];

		// the first palette should be the basic full saturation/brightness HSB colors
		// colorMode(HSB);
		Color[] hsbWheel = new Color[256];
		for (int i=0; i<256; i++) {
			hsbWheel[i] = Color.getHSBColor(i/255f, 1f, 1f);
		}
		hsbPalette = new Palette(hsbWheel);

//		k = new Kuler(pa);
//		k.setKey("5F5D21FE5CA6CBE00A40BD4457BAF3BA");
//		k.setNumResults(20);

//		KulerTheme[] kt = (KulerTheme[]) k.getHighestRated();
//		for (int i=0; i<kt.length; i++) {
//			kPs[i+1] = kt[i];
//			kPs[i+1].addColor(kPs[i+1].getColor(0));
//		}
	}

	public Palette getHsbPalette() {
		return hsbPalette;
	}

	public Palette getNewPalette(ColorTheoryStrategy s, int numColors, float hue) {
		Color[] colors = new Color[numColors];
		//color[] colors = new color[numColors];
//		if (paletteType == PT_KULER) {
//			kPalInd = (kPalInd + 1) % kPs.length;
//			Palette p = kPs[kPalInd];
//			basePaletteColors = p.totalSwatches();
//
//			Gradient g = new Gradient(p, numColors, false);
//			for (int i=0; i<g.totalSwatches(); i++) {
//				colors[i] = g.getColor(i);
//			}
//		} else {
			TColor col = ColorRange.BRIGHT.getColor(Hue.getClosest(hue, true));
			ColorList colList = ColorList.createUsingStrategy(s, col);
			basePaletteColors = colList.size();

			ColorGradient grad = new ColorGradient();
			for (int i=0; i<colList.size(); i++) {
				grad.addColorAt((float)(i)*numColors/colList.size(), colList.get(i));
			}
			grad.addColorAt(numColors-1, colList.get(0));
			ColorList colList2 = grad.calcGradient(0, numColors);

			for (int i=0; i<colList2.size(); i++) {
				colors[i] = new Color(colList2.get(i).toARGB());
			}
//		}

		return new Palette(colors);
	}

	int basePaletteColors() { return this.basePaletteColors; }

	void nextPaletteType() {
		paletteType = (paletteType + 1) % NUM_PT;
	}

	String getPaletteType() {
		if (paletteType == PT_KULER) {
			return "Kuler";
		} else {
			ColorTheoryStrategy s = (ColorTheoryStrategy) ptStrategies.get(paletteType-1);
			return s.getName();
		}
	}

	public static class Palette {
		private Color[] colors;

		public Palette(final Color[] color) {
			this.colors = color;
		}

		public Color[] getColors() {
			return colors;
		}

		public Color getColor(float fraction) {
			fraction = fraction % 1f;
			return colors[Math.min((int) (fraction * colors.length), colors.length - 1)];
		}
	}
}
