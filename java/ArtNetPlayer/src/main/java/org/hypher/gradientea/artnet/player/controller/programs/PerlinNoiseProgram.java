package org.hypher.gradientea.artnet.player.controller.programs;

import org.hypher.gradientea.artnet.player.controller.OscConstants;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.math.DomeMath;
import toxi.math.noise.PerlinNoise;

import java.awt.image.BufferedImage;

import static java.lang.Math.ceil;
import static java.lang.Math.sin;
import static toxi.math.noise.SimplexNoise.noise;

/**
 * Based on https://github.com/gregfriedland/AuroraLEDwall/blob/master/LEDwallProcessing/AlienBlob.pde
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class PerlinNoiseProgram extends BaseDomeProgram  {
	float xoff = 0, yoff = 0, zoff = 0;
	float sineTable[];
	float dThresh, incr, xoffIncr, yoffIncr, zoffIncr, noiseMult;

	float xScrollVel, yScrollVel;

	BufferedImage image;

	PerlinNoise noise = new PerlinNoise();

	private OscHelper.OscBoolean oscEnabled = OscHelper.booleanValue(
		OscConstants.Control.Perlin.ENABLE, false
	);

	private OscHelper.OscDouble oscDetail = OscHelper.doubleValue(
		OscConstants.Control.Perlin.DETAIL, 0, 1.0, 0.9
	);

	private OscHelper.OscDouble oscZoom = OscHelper.doubleValue(
		OscConstants.Control.Perlin.ZOOM, 0, 0.5, 0.2
	);

	private OscHelper.OscDouble oscMultipler = OscHelper.doubleValue(
		OscConstants.Control.Perlin.MULTIPLER, 0, 2.0, 0.2
	);

	public PerlinNoiseProgram() {
		super(ProgramId.PERLIN);
	}

	@Override
	protected void initialize() {
		image = new BufferedImage(32, 32, BufferedImage.TYPE_3BYTE_BGR);

		// precalculate 1 period of the sine wave (360 degrees)
		sineTable = new float[360];
		for (int i = 0; i < 360; i ++) {
			sineTable[i] = (float) sin(DomeMath.radians(i));
		}

		dThresh = 90;
		incr = 0.03125f;
		xoffIncr = 0.003f; //0.3;
		yoffIncr = 0.0007f; //0.07;
		zoffIncr = 0.1f;
		noiseMult = 5; //3
	}

	@Override
	public boolean isFocusDesired() {
		return oscEnabled.value();
	}

	@Override
	public void start() {
	}

	@Override
	public void update() {
		float d, h, s, b, n;
		float xx;
		float yy = 0;
		int w2 = image.getWidth() / 2;
		int h2 = image.getHeight() / 2;

		int nd = (int) ceil(oscDetail.floatValue());
		float multiplier = oscMultipler.floatValue();
		noise.noiseDetail(nd);

		for (int y = 0; y < image.getHeight(); y++) {
			xx = 0;
			for (int x = 0; x < image.getWidth(); x++) {
				d = DomeMath.dist(x, y, w2, h2) * 0.025f;
				if (d * 20 <= dThresh) {
					n = (float) noise(xx * multiplier, yy * multiplier, zoff); // noise only needs to be computed once per pixel
					h = (float) (Math.sin(d + n * noiseMult)+1)/2f;// % 2;

					// determine pixel color

					image.setRGB(x, y, controller.getColor(h).getRGB());
				} else {
					image.setRGB(x, y, 0);
				}

				xx += incr;
			}

			yy += incr;
		}

		// move through noise space -> animation
		//xoff += xoffIncr * pow(2, getSpeed() * 2 - 1);
		//yoff += yoffIncr * pow(2, getSpeed() * 2 - 1);
		zoff += zoffIncr * oscZoom.floatValue();

		controller.displayImage(image);
	}

	@Override
	public void stop() {
	}
}
