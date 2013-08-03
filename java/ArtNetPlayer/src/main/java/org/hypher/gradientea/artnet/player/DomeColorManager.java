package org.hypher.gradientea.artnet.player;

import org.hypher.gradientea.artnet.player.aurora.AuroraPaletteManager;
import org.hypher.gradientea.geometry.shared.math.DomeMath;
import toxi.color.theory.ColorTheoryRegistry;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeColorManager {
	public final static int PALETTE_COUNT = 60;
	public static final int COLORS_PER_PALETTE = 16;

	private static DomeColorManager instance;

	public static DomeColorManager instance() {
		if (instance == null) {
			instance = new DomeColorManager();
		}
		return instance;
	}

	private List<AuroraPaletteManager.Palette> palettes = new ArrayList<AuroraPaletteManager.Palette>(PALETTE_COUNT);

	public DomeColorManager() {
		AuroraPaletteManager manager = new AuroraPaletteManager();
		palettes.add(manager.getHsbPalette());
		for (int i=0; i<PALETTE_COUNT-1; i++) {
			palettes.add(manager.getNewPalette(
				ColorTheoryRegistry.COMPOUND,
				COLORS_PER_PALETTE,
				DomeMath.f(i) / PALETTE_COUNT
			));
		}
	}

	public Color getColor(float palette, float color) {
		return getPalette(palette).getColor(color);
	}

	public AuroraPaletteManager.Palette getPalette(final float palette) {
		return palettes.get(Math.min((int) (palette*palettes.size()), palettes.size()-1));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
