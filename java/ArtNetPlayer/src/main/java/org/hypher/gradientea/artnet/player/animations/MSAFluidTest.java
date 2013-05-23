package org.hypher.gradientea.artnet.player.animations;

import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.artnet.player.io.TrackballInput;
import org.hypher.gradientea.artnet.player.io.GlobalAudioReader;
import org.hypher.gradientea.artnet.player.io.BasicAudioReader;
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

/**
 * An animation which attempts to use {@link MSAFluidSolver2D} to draw pretty things on the dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class MSAFluidTest implements Runnable {
	public final static int WIDTH = 35;
	public final static int HEIGHT = 35;
	public final static float ASPECT_RATIO = (float)WIDTH/HEIGHT;

	public static void main(String[] args) throws IOException {
		new MSAFluidTest();
	}
	private UdpDomeClient transport = new UdpDomeClient();

	private GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(GradienTeaDomeSpecs.GRADIENTEA_DOME);
	private DomeImageMapper mapper = new DomeImageMapper(geometry);

	private BufferedImage image;

	private MSAFluidSolver2D fluidSolver;

	public MSAFluidTest() throws IOException {
		transport.connect("localhost");

		fluidSolver = new MSAFluidSolver2D(WIDTH, HEIGHT);
		fluidSolver.enableRGB(true).setFadeSpeed(0.015f).setDeltaT(0.3f).setVisc(0.0003f);

		image = new BufferedImage(fluidSolver.getWidth(), fluidSolver.getHeight(), BufferedImage.TYPE_INT_RGB);


		new Thread(this).start();
	}


	private int frameCount = 0;
	@Override
	public void run() {
		DomePixelCanvas canvas = new DomePixelCanvas(
			org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor.ADDITIVE,
			geometry);

		long lastFrame = System.currentTimeMillis();
		while (true) {

			long frameStart = System.currentTimeMillis();
			canvas.clear();
			frameCount ++;
			draw(canvas, frameStart - lastFrame);
			lastFrame = frameStart;

			transport.displayFrame(canvas.render());

			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {}
		}
	}

	private float mouseX = 0, mouseY = 0;
	private JFrame frame;
	{
		frame = new JFrame("...also, we have cookies."){
			@Override
			public void paint(final Graphics graphics) {
				if (image != null) {
					Graphics2D g2 = (Graphics2D) graphics;
					g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON
					);

					graphics.drawImage(image, 0, 0, getWidth(), getHeight(), null);
					mapper.drawMask(g2, getWidth(), getHeight());
				}
			}
		};
		frame.setSize(640, 480);
		frame.setVisible(true);
	}

	private void draw(final DomePixelCanvas canvas, final long elapsedMs) {
		updateAudio();
		updateFluid();
		fluidSolver.update();

		for(int i=0; i<fluidSolver.getNumCells(); i++) {
			int y = i/image.getWidth();
			int x = i - y*image.getWidth();

			fluidSolver.r[i] = fluidSolver.r[i] < 0.01 ? 0 : fluidSolver.r[i];
			fluidSolver.g[i] = fluidSolver.g[i] < 0.01 ? 0 : fluidSolver.g[i];
			fluidSolver.b[i] = fluidSolver.b[i] < 0.01 ? 0 : fluidSolver.b[i];

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

		mapper.drawImage(
			image,
			canvas
		);
		frame.repaint();
	}

	double audioDirection = 0;
	double audioVelocity = 0;
	private void updateAudio() {

		BasicAudioReader.LevelAverage longestAverage = GlobalAudioReader.getReader().getRMSMean(5.0f);
		double longAverage = Math.max(0, GlobalAudioReader.getReader().getRMSMean(1f).mean() - 0.005);
		double now = Math.max(0, GlobalAudioReader.getReader().getRMSMean(1/30f).mean()-0.005);

		if (now > longestAverage.mean()*.9) {
			audioVelocity += 0.5;
		} else {
			audioVelocity *= 0.7;
		}

		if (now < longestAverage.mean()*.5) {
			audioDirection += (Math.random()-0.5) * Math.PI/4;
		}

		audioVelocity = Math.max(0, Math.min(1, audioVelocity));
	}

	private void updateFluid() {
		float oldMouseX = mouseX;
		float oldMouseY = mouseY;

		for (TrackballInput.TrackballReading reading : TrackballInput.instance().read().asSet()) {
			mouseX = (float) (mouseX + reading.getDeltaX()*0.005 + Math.cos(audioDirection)*audioVelocity);
			mouseY = (float) (mouseY + reading.getDeltaY()*0.005 + Math.sin(audioDirection)*audioVelocity);
		}

		// Wrap
		mouseX = (mouseX % WIDTH) + (mouseX < 0 ? WIDTH : 0);
		mouseY = (mouseY % HEIGHT) + (mouseY < 0 ? HEIGHT : 0);


		addForce(mouseX/WIDTH, mouseY/HEIGHT, (mouseX - oldMouseX)/WIDTH, (mouseY - oldMouseY)/HEIGHT);

		fluidSolver.setFadeSpeed((float) ((Math.sin(frameCount/30.0)+0.7) * 0.020));
	}

	void addForce(float x, float y, float dx, float dy) {
		float speed = dx * dx  + dy * dy * ASPECT_RATIO;    // balance the x and y components of speed with the screen aspect ratio

		if(speed > 0) {
			if(x<0) x = 0;
			else if(x>1) x = 1;
			if(y<0) y = 0;
			else if(y>1) y = 1;

			float colorMult = 6;
			float velocityMult = 5.0f;

			Color drawColor;

			float hue =((x + y) * 220 + frameCount) % 600;
			drawColor = Color.getHSBColor(hue/600, 1, 1);

			fluidSolver.addColorAtPos(
				x,
				y,
				(drawColor.getRed()/255f) * colorMult,
				(drawColor.getGreen()/255f) * colorMult,
				(drawColor.getBlue()/255f) * colorMult
			);

			fluidSolver.addForceAtPos(
				x,
				y,
				(float) (dx * velocityMult),
				(float) (dy * velocityMult)
			);
		}
	}

	public static class FluidEmitter {
		public float x, y;

		public float emitterHue;
		public float emitterAngle;
		public float emitterIntensity;
		public float emitterVelocity;

		public void setEmitterFromDeltaPosition(float dx, float dy, float velocityMultiplier) {
			emitterAngle = (float) Math.atan2(dy, dx);
			emitterVelocity = (float) (Math.sqrt(dx*dx + dy*dy) * velocityMultiplier);
		}

		public void aimEmitterAt(float x, float y) {
			emitterAngle = (float) Math.atan2(y-this.y, x-this.x);
		}

		public void emit(MSAFluidSolver2D fluidSolver) {
			Color drawColor = Color.getHSBColor(emitterHue, 1, 1);

			float dx = (float) (Math.cos(emitterAngle) * emitterVelocity);
			float dy = (float) (Math.sin(emitterAngle) * emitterVelocity);

			fluidSolver.addColorAtPos(
				x,
				y,
				(drawColor.getRed()/255f) * emitterIntensity,
				(drawColor.getGreen()/255f) * emitterIntensity,
				(drawColor.getBlue()/255f) * emitterIntensity
			);

			fluidSolver.addForceAtPos(
				x,
				y,
				dx,
				dy
			);
		}
	}
}
