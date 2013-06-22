package org.hypher.gradientea.artnet.player.controller;

import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.math.DomeMath;
import org.msafluid.MSAFluidSolver2D;

import java.awt.image.BufferedImage;

import static org.hypher.gradientea.geometry.shared.math.DomeMath.TWO_PI;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.f;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeFluidCanvas {
	public static final int DEFAULT_SIZE = 40;
	private int currentSize;
	private MSAFluidSolver2D fluidSolver;
	private BufferedImage image;

	public DomeFluidCanvas() {
		this(DEFAULT_SIZE);
	}

	public DomeFluidCanvas(final int size) {
		setSize(size);
		setupOsc();
	}

	private void setupOsc() {
		OscHelper.instance().mapValue(new OscHelper.OscDouble(OscConstants.Control.Fluid.FLUID_SIZE, 20, 80, fluidSolver.getVisc()) {
			@Override
			public void applyDouble(final double value) {
				setSize((int) value);
			}

			@Override
			public double getValue() {
				return currentSize;
			}
		}
		);

		OscHelper.instance().mapValue(new OscHelper.OscDouble(OscConstants.Control.Fluid.VISCOSITY, 0.000005, 0.00500, fluidSolver.getVisc()) {
			@Override
			public void applyDouble(final double value) {
				fluidSolver.setVisc(f(value));
			}

			@Override
			public double getValue() {
				return fluidSolver.getVisc();
			}
		}
		);

		OscHelper.instance().mapValue(new OscHelper.OscDouble(OscConstants.Control.Fluid.SPEED, 0.1, 3.0, fluidSolver.getVisc()) {
			@Override
			public void applyDouble(final double value) {
				fluidSolver.setDeltaT(f(value));
			}

			@Override
			public double getValue() {
				return fluidSolver.getDeltaT();
			}
		}
		);

		OscHelper.instance().mapValue(new OscHelper.OscDouble(OscConstants.Control.Fluid.FADE, 0.0001, 0.10, fluidSolver.getVisc()) {
			@Override
			public void applyDouble(final double value) {
				fluidSolver.setFadeSpeed((float) value);
			}

			@Override
			public double getValue() {
				return fluidSolver.getFadeSpeed();
			}
		}
		);
	}

	public void update() {
		fluidSolver.update();
		updateImage();
	}

	private void updateImage() {
		int width = getWidth();
		int height = getHeight();
		int rgb[] = new int[3];

		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				rgbAt(x, y, rgb);
				rgb[0] = DomeMath.clip(0, 255, (int) (rgb[0] * 2.5));
				rgb[1] = DomeMath.clip(0, 255, (int) (rgb[1] * 2.5));
				rgb[2] = DomeMath.clip(0, 255, (int) (rgb[2] * 2.5));
				int average = Math.max(rgb[0], Math.max(rgb[1], rgb[2]));

				image.setRGB(
					x,
					y,
					(average << 24) | (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2] << 0)
				);
			}
		}
	}

	public BufferedImage getImage() {
		return image;
	}

	private void setSize(final int size) {
		if (size != currentSize) {
			float viscosity = 0.00023f;
			float deltaT = 0.8f;
			float fadeSpeed = 0.015f;

			if (fluidSolver != null) {
				viscosity = fluidSolver.getVisc();
				deltaT = fluidSolver.getDeltaT();
				fadeSpeed = fluidSolver.getFadeSpeed();
			}

			this.currentSize = size;
			fluidSolver = new MSAFluidSolver2D(size, size);
			fluidSolver
				.enableRGB(true)
				.setVisc(viscosity)
				.setDeltaT(deltaT)
				.setFadeSpeed(fadeSpeed);

			this.image = new BufferedImage(
				fluidSolver.getWidth(),
				fluidSolver.getHeight(),
				BufferedImage.TYPE_INT_ARGB
			);
		}
	}

	public int getWidth() {
		return fluidSolver.getWidth();
	}

	public int getHeight() {
		return fluidSolver.getWidth();
	}

	public void rgbAt(int x, int y, int[] rgbOut) {
		int i = y * fluidSolver.getWidth() + x;
		rgbOut[0] = DomeMath.clip(0, 255, (int) ((fluidSolver.r[i] < 0.0001 ? 0.000f : fluidSolver.r[i]) * 255));
		rgbOut[1] = DomeMath.clip(0, 255, (int) ((fluidSolver.g[i] < 0.0001 ? 0.000f : fluidSolver.g[i]) * 255));
		rgbOut[2] = DomeMath.clip(0, 255, (int) ((fluidSolver.b[i] < 0.0001 ? 0.000f : fluidSolver.b[i]) * 255));
	}

	public void rgbAt(int x, int y, float[] rgbOut) {
		int i = y * fluidSolver.getWidth() + x;
		rgbOut[0] = DomeMath.clip(0f, 1f, (fluidSolver.r[i] < 0.0001 ? 0.000f : fluidSolver.r[i]) * 2.5f);
		rgbOut[1] = DomeMath.clip(0f, 1f, (fluidSolver.g[i] < 0.0001 ? 0.000f : fluidSolver.g[i]) * 2.5f);
		rgbOut[2] = DomeMath.clip(0f, 1f, (fluidSolver.b[i] < 0.0001 ? 0.000f : fluidSolver.b[i]) * 2.5f);
	}

	public void emitDirectional(
		float fromX,
		float fromY,

		float toX,
		float toY,

		float hue,
		float velocity,
		float intensity
	) {
		emitDirectional(
			fromX, fromY,
			f(Math.atan2(toY - fromY, toX - fromX)),
			hue, velocity, intensity
		);
	}

	public void emitDirectional(
		float fromX,
		float fromY,

		float angle,

		float hue,
		float velocity,
		float intensity
	) {
		int[] drawColor = new HsbColor(hue, 1.0, 1.0).asRgb();

		fluidSolver.addColorAtPos(
			fromX,
			fromY,
			(drawColor[0]/255f) * intensity,
			(drawColor[1]/255f) * intensity,
			(drawColor[2]/255f) * intensity
		);

		fluidSolver.addForceAtPos(
			fromX,
			fromY,
			(float) (Math.cos(angle) * velocity),
			(float) (Math.sin(angle) * velocity)
		);
	}

	public void emitRing(
		float fromX,
		float fromY,

		float radius,
		float pointCount,

		float hue,
		float velocity,
		float intensity
	) {
		int[] drawColor = new HsbColor(hue, 1.0, 1.0).asRgb();

		for (double i=0; i<1; i+=1/pointCount) {
			double angle = i * TWO_PI;

			final float x = fromX + f(Math.cos(angle) * radius);
			final float y = fromY + f(Math.sin(angle) * radius);
			fluidSolver.addColorAtPos(
				x,
				y,

				(drawColor[0]/255f) * intensity,
				(drawColor[1]/255f) * intensity,
				(drawColor[2]/255f) * intensity
			);

			fluidSolver.addForceAtPos(
				x,
				y,
				f(Math.cos(angle) * velocity),
				f(Math.sin(angle) * velocity)
			);
		}
	}

	public void fade(final float fraction) {
		for (int i=0; i<fluidSolver.getNumCells(); i++) {
			fluidSolver.r[i] *= fraction;
			fluidSolver.g[i] *= fraction;
			fluidSolver.b[i] *= fraction;
		}
	}
}
