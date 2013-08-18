package org.hypher.gradientea.artnet.player.controller.programs;

import org.hypher.gradientea.artnet.player.controller.OscConstants;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.math.DomeMath;
import toxi.math.noise.PerlinNoise;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.hypher.gradientea.geometry.shared.math.DomeMath.floor;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.ceil;

/**
 * Based on https://github.com/gregfriedland/AuroraLEDwall/blob/master/LEDwallProcessing/AlienBlob.pde
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class FireProgram extends BaseDomeProgram  {
	private BufferedImage image;

	// Flame colors
	private Color[] palette;
	private float angle;
	private int[] calcXCurrent, calcYDown1, calcXLeft, calcXRight, calcYDown2;

	private int size = 32;

	private int fireWidth;
	private int fireHeight;

	private float fireUnitArc;
	private float fireUnitRadius;

	private int[][] fire;

	PerlinNoise noise = new PerlinNoise();

	private OscHelper.OscBoolean oscEnabled = OscHelper.booleanValue(
		OscConstants.Control.Fire.ENABLE, false
	);

	private OscHelper.OscBoolean oscEmitDownwards = OscHelper.booleanValue(
		OscConstants.Control.Fire.DOWNWARDS, false
	);

	private OscHelper.OscBoolean oscGlobalColor = OscHelper.booleanValue(
		OscConstants.Control.Fire.USE_GLOBAL_COLOR, false
	);

	private OscHelper.OscDouble oscClusterSize = OscHelper.doubleValue(
		OscConstants.Control.Fire.CLUSTER_SIZE, 0, 1.0, 0.9
	);

	private OscHelper.OscDouble oscSpeed = OscHelper.doubleValue(
		OscConstants.Control.Fire.SPEED, 0, 1.0, 0.6
	);

	private OscHelper.OscDouble oscBrightness = OscHelper.doubleValue(
		OscConstants.Control.OVERALL_BRIGHTNESS, 0, 1.0, 0.6
	);

	public FireProgram() {
		super(ProgramId.FIRE);
	}

	@Override
	protected void initialize() {
		image = new BufferedImage(
			(int)(size*1.5),
			(int)(size*1.5),
			BufferedImage.TYPE_3BYTE_BGR
		);

		fireWidth = size;
		fireHeight = size;

		fireUnitArc = (float) (DomeMath.TWO_PI / size)*1.5f;
		fireUnitRadius = (float) (2.75f/size);

		calcXCurrent = new int[fireWidth];
		calcXLeft = new int[fireWidth];
		calcXRight = new int[fireWidth];

		calcYDown1 = new int[fireHeight];
		calcYDown2 = new int[fireHeight];

		palette = new Color[256];

		fire = new int[fireWidth][fireHeight];


		// Generate the palette
		for(int x = 0; x < palette.length; x++) {
			//Hue goes from 0 to 85: red to yellow
			//Saturation is always the maximum: 255
			//Lightness is 0..255 for x=0..128, and 255 for x=128..255
			palette[x] = Color.getHSBColor(
				(x / 3f) / 255f,
				1f,
				DomeMath.clip(0, 255, x * 3) / 255f
			);
		}

		// Precalculate which pixel values to add during animation loop
		// this speeds up the effect by 10fps
		for (int x = 0; x < fireWidth; x++) {
			calcXCurrent[x] = x % fireWidth;
			calcXLeft[x] = (x - 1 + fireWidth) % fireWidth;
			calcXRight[x] = (x + 1) % fireWidth;
		}

		for(int y = 0; y < fireHeight; y++) {
			calcYDown1[y] = (y + 1) % fireHeight;
			calcYDown2[y] = (y + 2) % fireHeight;
		}

		((Graphics2D) image.getGraphics()).setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON
		);
	}

	@Override
	public boolean isFocusDesired() {
		return oscEnabled.value();
	}

	@Override
	public void start() {
	}

	int frameCount = 0;

	private float x(float theta, float r) {
		return (int) ((image.getWidth() / 2) + Math.cos(DomeMath.clip(0,DomeMath.FTWO_PI,theta)) * r * ((image.getWidth() - 1) / 2));
	}

	private float y(float theta, float r) {
		return ((int) ((image.getHeight() / 2) + Math.sin(DomeMath.clip(0,DomeMath.FTWO_PI,theta)) * r * ((image.getHeight() - 1) / 2)));
	}

	private void setImagePixel(float theta, float r, Color color) {
		int width = image.getWidth();
		int height = image.getHeight();

		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setColor(color);
		g.fillPolygon(
			new int[] {floor(x(theta, r)), floor(x(theta+fireUnitArc, r)), ceil(x(theta, r+fireUnitRadius)), ceil(x(theta + fireUnitArc, r + fireUnitRadius))},
			new int[] {floor(y(theta, r)), floor(y(theta+fireUnitArc, r)), ceil(y(theta, r+fireUnitRadius)), ceil(y(theta+fireUnitArc, r+fireUnitRadius))},
			4
		);
	}

	@Override
	public void update() {
		frameCount ++;

		// speed of 0: we skip 2/3 of the time; speed of 1; we skip 0 of the time
		int frameSkip = 3 - Math.round(oscSpeed.floatValue() * 2);
		if (frameCount % frameSkip == 0) {
			// Randomize the bottom row of the fire buffer
			int clusterSize = Math.round(oscClusterSize.floatValue()*10);
			for(int x = 0; x < fireWidth; x+=clusterSize) {
				int i = (int) (Math.random() * 190);
				for (int x2=x; x2<Math.min(x+clusterSize, fireWidth); x2++) {
					fire[x2][fireHeight -1] = i;
				}
			}

			for (int y = 0; y < fireHeight; y++) {
				for(int x = 0; x < fireWidth; x++) {
					// Add pixel values around current pixel

					int fireVal;
					fireVal = fire[x][y] =
						((fire[calcXLeft[x]][calcYDown1[y]]
							+ fire[calcXCurrent[x]][calcYDown1[y]]
							+ fire[calcXRight[x]][calcYDown1[y]]
							+ fire[calcXCurrent[x]][calcYDown2[y]]
						) << 5) / 135; //129;

					// Output everything to screen using our palette colors
					if (x < size && y<size) {
						float theta = (float) (((float) x / fireWidth) * DomeMath.TWO_PI);
						float radius = (float) (oscEmitDownwards.value()?(fireHeight-y):y) / fireHeight * 1.25f;

						if (oscGlobalColor.value()) {
							setImagePixel(theta, radius, controller.getColor(fireVal / 256f));
						} else {
							setImagePixel(theta, radius, palette[fireVal]);
						}
					}
				}
			}
		}

		controller.displayImage(image);
	}

	@Override
	public void drawOverlay(final Graphics2D g, final int width, final int height) {
		//g.drawImage(image, 0, 0, width, height, null);
	}

	@Override
	public void stop() {
	}

//
//	// Randomize the ring
//	float increment = (float) (TWO_PI / ((1-oscClusterSize.getValue())*20));
//	for (float theta=0; theta< TWO_PI; theta += increment) {
//		int randomValue = (int) (190 * Math.random());
//
//		for (float subTheta=theta; subTheta<theta+increment; subTheta+=fireUnitArc) {
//			set(subTheta, 1f, randomValue);
//		}
//	}
//
//	int counter = 0;
//
//	// Do the fire calculations for every pixel, from inside to outside
//	float radiusIncrement = 1/fireWidth;
//	for (float r=0; r<1f; r+=radiusIncrement) {
//		for (float theta=0; theta<TWO_PI; theta+=fireUnitArc) {
//			int fireVal = ((
//				get(theta-fireUnitArc, r+radiusIncrement) +
//					get(theta, r) +
//					get(theta+fireUnitArc, r+radiusIncrement) +
//					get(theta, r+fireUnitArc*2)
//			) << 5) / 135;
//
//			set(theta, r, fireVal);
//
//
//			if (oscGlobalColor.value()) {
//				setImagePixel(theta, r, controller.getColor(fireVal/256f).getRGB());
//			} else {
//				setImagePixel(theta, r, palette[fireVal].getRGB());
//			}
//		}
//	}
}
