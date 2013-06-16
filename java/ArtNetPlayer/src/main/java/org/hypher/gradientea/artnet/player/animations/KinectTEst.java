package org.hypher.gradientea.artnet.player.animations;

import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.artnet.player.DomeAnimationServerMain;
import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.artnet.player.io.osc.OscSynapse;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;
import org.msafluid.MSAFluidSolver2D;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.hypher.gradientea.artnet.player.io.osc.OscHelper.OscDouble;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.TWO_PI;
import static org.hypher.gradientea.geometry.shared.math.DomeMath.f;

/**
 * An animation which attempts to use {@link MSAFluidSolver2D} to draw pretty things on the dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class KinectTest implements Runnable {
	public final static int WIDTH = 30;
	public final static int HEIGHT = 30;
	public final static float ASPECT_RATIO = (float)WIDTH/HEIGHT;
	public static final int FREQUENCY_BANDS = 7;
	public static final int LOW_FREQ_BUCKET = 4;
	public static final int HIGH_FREQ_BUCKET = 128;
	private static final int FPS = 30;

	static {
		OscSynapse.instance();
	}


	public static void main(String[] args) throws IOException {
		new KinectTest();
	}
	private UdpDomeClient prototypeDomeTransport = new UdpDomeClient();
	private UdpDomeClient miniDomeTransport = new UdpDomeClient();

	private final GradienTeaDomeGeometry prototypeGeometry =
		new GradienTeaDomeGeometry(GradienTeaDomeSpecs.PROTOTYPE_DOME);

	private final GradienTeaDomeGeometry miniDomeGeometry =
		new GradienTeaDomeGeometry(GradienTeaDomeSpecs.GRADIENTEA_DOME);

	private DomeImageMapper prototypeMapper = new DomeImageMapper(prototypeGeometry);


	private DomeImageMapper miniDomeMapper =  new DomeImageMapper(miniDomeGeometry);

	DomePixelCanvas miniDomeCanvas = new DomePixelCanvas(
		org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor.ADDITIVE,
		miniDomeGeometry
	);

	DomePixelCanvas prototypeDomeCanvas = new DomePixelCanvas(
		org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor.ADDITIVE,
		prototypeGeometry
	);


	private BufferedImage image;

	private MSAFluidSolver2D fluidSolver;

	public KinectTest() throws IOException {
		prototypeDomeTransport.connect("localhost", DomeAnimationServerMain.DOME_PORT);
		miniDomeTransport.connect("localhost", DomeAnimationServerMain.DOME_PORT + 1);

		fluidSolver = new MSAFluidSolver2D(WIDTH, HEIGHT);
		fluidSolver.enableRGB(true).setFadeSpeed(0.080f).setDeltaT(0.9f).setVisc(0.00013f);

		image = new BufferedImage(fluidSolver.getWidth(), fluidSolver.getHeight(), BufferedImage.TYPE_INT_RGB);

		OscHelper.instance().mapValue(
			"/gt/fluid/viscosity", new OscDouble("/gt/fluid/viscosity", 0.00001, 0.00100, fluidSolver.getVisc()) {
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

		OscHelper.instance().mapValue(
			"/gt/fluid/dt", new OscDouble("/gt/fluid/dt", 0.1, 2.0, fluidSolver.getVisc()) {
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

		new Thread(this).start();
	}


	private int frameCount = 0;
	@Override
	public void run() {
		long lastFrame = System.currentTimeMillis();
		while (true) {

			long frameStart = System.currentTimeMillis();
			prototypeDomeCanvas.clear();
			miniDomeCanvas.clear();
			frameCount ++;
			draw(prototypeDomeCanvas, frameStart - lastFrame);
			lastFrame = frameStart;

			prototypeDomeTransport.displayFrame(prototypeDomeCanvas.render());
			miniDomeTransport.displayFrame(miniDomeCanvas.render());

			try {
				Thread.sleep(Math.max(0, (1000/FPS) - (System.currentTimeMillis() - lastFrame)));
			} catch (InterruptedException e) {}
		}
	}

	private JFrame frame;
	{
		frame = new JFrame("Fluid Simulation Mapping"){
			@Override
			public void paint(final Graphics graphics) {
				if (image != null) {
					Graphics2D g2 = (Graphics2D) graphics;
					g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON
					);

					graphics.drawImage(image, 0, 0, getWidth()/2, getHeight(), null);
					prototypeMapper.drawMask(g2, 0, 0, getWidth()/2, getHeight());

					graphics.drawImage(image, getWidth()/2, 0, getWidth()/2, getHeight(), null);
					miniDomeMapper.drawMask(g2, getWidth()/2, 0, getWidth()/2, getHeight());
				}
			}
		};
		frame.setSize(480*4, 480*2);
		frame.setLocation(0,520);
		frame.setVisible(true);
	}

	private void draw(final DomePixelCanvas canvas, final long elapsedMs) {
		updateFluid();
		fluidSolver.update();

		for(int i=0; i<fluidSolver.getNumCells(); i++) {
			int y = i/image.getWidth();
			int x = i - y*image.getWidth();

			fluidSolver.r[i] = fluidSolver.r[i] < 0.0001 ? 0.000f : fluidSolver.r[i];
			fluidSolver.g[i] = fluidSolver.g[i] < 0.0001 ? 0.000f : fluidSolver.g[i];
			fluidSolver.b[i] = fluidSolver.b[i] < 0.0001 ? 0.000f : fluidSolver.b[i];

			try {
				image.setRGB(x, y, new Color(
					(float) Math.min(1.0, fluidSolver.r[i]*2.5),
					(float) Math.min(1.0, fluidSolver.g[i]*2.5),
					(float) Math.min(1.0, fluidSolver.b[i]*2.5)
				).getRGB());
			} catch (Exception e) {
				System.err.println(i + " " + x + " " + y);
				break;
			}
		}

		prototypeMapper.drawImage(
			image,
			prototypeDomeCanvas
		);

		miniDomeMapper.drawImage(
			image,
			miniDomeCanvas
		);

		frame.repaint();
	}

	OscSynapse.OscJoint rightHand = OscSynapse.jointScreen(OscSynapse.Joint.RIGHTHAND);
	OscSynapse.OscJoint leftHand = OscSynapse.jointScreen(OscSynapse.Joint.LEFTHAND);

	float leftHue = 0.0f;
	float rightHue = 0.0f;

	private void updateFluid() {
		if (rightHand.getVelocity() > 0.01) {
			emitDirectional(
				(float) rightHand.getX(), (float) rightHand.getY(),
				(float) rightHand.getAngle(),
				(float) (rightHue + rightHand.getVelocity()),
				(float) rightHand.getVelocity()*0.5f,
				(float) rightHand.getVelocity()*100
			);
		} else {
			rightHue = (float) (rightHand.getY()/.7f) ;
		}

		if (leftHand.getVelocity() > 0.01) {
			emitDirectional(
				(float) leftHand.getX(), (float) leftHand.getY(),
				(float) leftHand.getAngle(),
				(float) (leftHue + leftHand.getVelocity()),
				(float) leftHand.getVelocity()*0.5f,
				(float) leftHand.getVelocity()*100
			);
		} else {
			leftHue = (float) (leftHand.getY()/.7f);
		}
	}

	private void emitDirectional(
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


	private void emitDirectional(
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

	private void emitRing(
		float fromX,
		float fromY,

		float radius,
		float duty,

		float hue,
		float velocity,
		float intensity
	) {
		int[] drawColor = new HsbColor(hue, 1.0, 1.0).asRgb();

		double circumference = 2*Math.PI*radius*WIDTH;
		for (double i=0; i<circumference; i+=1/duty) {
			double angle = (i/circumference) * TWO_PI;

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
}
