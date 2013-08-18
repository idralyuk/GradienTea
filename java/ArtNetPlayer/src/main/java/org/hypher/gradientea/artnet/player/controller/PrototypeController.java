package org.hypher.gradientea.artnet.player.controller;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import org.hypher.gradientea.artnet.player.DomeColorManager;
import org.hypher.gradientea.artnet.player.controller.programs.DebugProgram;
import org.hypher.gradientea.artnet.player.controller.programs.DomeAnimationProgram;
import org.hypher.gradientea.artnet.player.controller.programs.DoorLightAnimation;
import org.hypher.gradientea.artnet.player.controller.programs.FireProgram;
import org.hypher.gradientea.artnet.player.controller.programs.ManualControlProgram;
import org.hypher.gradientea.artnet.player.controller.programs.MotionControlProgram;
import org.hypher.gradientea.artnet.player.controller.programs.MusicControlProgram;
import org.hypher.gradientea.artnet.player.controller.programs.OffProgram;
import org.hypher.gradientea.artnet.player.controller.programs.PerlinNoiseProgram;
import org.hypher.gradientea.artnet.player.io.ArduinoLedPanelOutput;
import org.hypher.gradientea.artnet.player.io.kinect.KinectDisplay;
import org.hypher.gradientea.artnet.player.io.kinect.KinectInput;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hypher.gradientea.artnet.player.io.osc.OscHelper.booleanValue;

/**
 * Prototype controller for the gradientea dome. Provides the underlying drawing canvases, high-level OSC control,
 * and debug UI.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class PrototypeController implements Runnable, DomeController {
	public static final int FPS = 30;
	public static final File STATE_FILE = new File("/tmp/gradienTeaConfig.xml");

	private String domeControllerHost;
	private DomeFluidCanvas fluidCanvas;
	private List<DomeOutput> outputs = Lists.newArrayList();

	private OscHelper.OscBoolean oscHeartBeat = booleanValue(OscConstants.Status.HEART_BEAT, false);
	private JFrame statusWindow;
	private Component statusWidget;
	private KinectDisplay kinectWidget;

	private Map<DomeAnimationProgram.ProgramId, ProgramEntry> programMap = Maps.newLinkedHashMap();
	private DomeAnimationProgram.ProgramId activeProgramId;
	private DomeAnimationProgram.ProgramId defaultProgramId = DomeAnimationProgram.ProgramId.MUSIC;

	private OscHelper.OscDouble oscPaletteType = OscHelper.doubleValue(OscConstants.Control.Color.PALETTE_TYPE, 0, 1, 0);
	private OscHelper.OscDouble oscPaletteHue = OscHelper.doubleValue(OscConstants.Control.Color.PALETTE_HUE, 0, 1, 0);
	private OscHelper.OscDouble oscPaletteSize = OscHelper.doubleValue(OscConstants.Control.Color.PALETTE_COLOR_COUNT, 5, 35, 20);


	private OscHelper.OscText oscPaletteTypeLabel = OscHelper.textValue(OscConstants.Control.Color.PALETTE_TYPE_LABEL);
	private OscHelper.OscText oscPaletteHueLabel = OscHelper.textValue(OscConstants.Control.Color.PALETTE_HUE_LABEL);

	private OscHelper.OscDouble oscOverallFluidIntensity = OscHelper.doubleValue(OscConstants.Control.OVERALL_BRIGHTNESS, .1, 6, 3);

	private OscHelper.OscBoolean oscShowDome1Overlay = OscHelper.booleanValue(OscConstants.Control.Fluid.SHOW_DOME_OVERLAY_1, true);
	private OscHelper.OscBoolean oscShowDome2Overlay = OscHelper.booleanValue(OscConstants.Control.Fluid.SHOW_DOME_OVERLAY_2, false);

	private OscHelper.OscBoolean oscShowOverlayAddresses = OscHelper.booleanValue(OscConstants.Control.Fluid.SHOW_OVERLAY_ADDRESSES, true);
	private OscHelper.OscBoolean oscShowOutputOverlay = OscHelper.booleanValue(OscConstants.Control.Fluid.SHOW_OUTPUT_OVERLAY, true);
	private OscHelper.OscBoolean oscShowFluidOverlay = OscHelper.booleanValue(OscConstants.Control.Fluid.SHOW_FLUID_OVERLAY, true);
	private OscHelper.OscBoolean oscShowVertices = OscHelper.booleanValue(OscConstants.Control.Fluid.SHOW_VERTICES, true);
	private OscHelper.OscBoolean oscShowOutline = OscHelper.booleanValue(OscConstants.Control.Fluid.SHOW_OUTLINE, true);

	private DomeColorManager.DomePaletteSpec currentPaletteSpec = DomeColorManager.instance().specFor(0, 0, 10);
	private DomeColorManager.DomePalette currentPalette = DomeColorManager.instance().paletteFor(currentPaletteSpec);


	private Optional<ArduinoLedPanelOutput> arduinoOutput = ArduinoLedPanelOutput.getInstance();

	private long frameCounter = 0;

	private DoorLightAnimation doorProgram = new DoorLightAnimation();

	public static void main(String[] args) {
		new PrototypeController(args.length > 0 ? args[0] : "localhost").start();
	}

	public PrototypeController(final String domeControllerHost) {
		this.domeControllerHost = domeControllerHost;
		this.fluidCanvas = new DomeFluidCanvas();

		outputs.add(new DomeOutput(GradienTeaDomeSpecs.PROTOTYPE_DOME, 0));
		outputs.add(new DomeOutput(GradienTeaDomeSpecs.GRADIENTEA_DOME, 1));

		addProgram(new OffProgram());
		addProgram(new DebugProgram());
		addProgram(new ManualControlProgram());
		addProgram(new PerlinNoiseProgram());
		addProgram(new FireProgram());
		addProgram(new MusicControlProgram());

		if (KinectInput.instance().isKinectEnabled()) {
			addProgram(new MotionControlProgram());
		}
	}

	private void addProgram(DomeAnimationProgram program) {
		programMap.put(program.getProgramId(), new ProgramEntry(program));
	}

	private void initPrograms() {
		for (ProgramEntry programEntry : programMap.values()) {
			programEntry.program.init(this);
		}

		selectProgram(DomeAnimationProgram.ProgramId.OFF);
	}

	private DomeAnimationProgram activeProgram() {
		return programMap.get(activeProgramId).program;
	}

	private void createStatusWindow() {
		statusWindow = new JFrame("Prototype GradienTea Controller");
		statusWindow.setLocation(0, 0);
		statusWindow.setVisible(true);

		statusWidget = new Component() {
			private VolatileImage createBackBuffer() {
				return getGraphicsConfiguration().createCompatibleVolatileImage(getWidth(), getHeight());
			}

			@Override
			public void repaint() {
				paint(getGraphics());
			}

			@Override
			public void paint(final Graphics g) {
				// Hardware accelerated code from http://www.javalobby.org/forums/thread.jspa?threadID=16840&tstart=0

				// create the hardware accelerated image.
				final VolatileImage volatileImg = createBackBuffer();

				// Main rendering loop. Volatile images may lose their contents.
				// This loop will continually render to (and produce if necessary) volatile images
				// until the rendering is completed successfully.

				do {
					// Validate the volatile image for the graphics configuration of this
					// component. If the volatile image doesn't apply for this graphics configuration
					// (in other words, the hardware acceleration doesn't apply for the new device)
					// then we need to re-create it.
					GraphicsConfiguration gc = this.getGraphicsConfiguration();
					int valCode = volatileImg.validate(gc);

					// This means the device doesn't match up to this hardware accelerated image.
					if(valCode == VolatileImage.IMAGE_INCOMPATIBLE){
						createBackBuffer(); // recreate the hardware accelerated image.
					}

					Graphics offscreenGraphics = volatileImg.getGraphics();

					offscreenPaint((Graphics2D) offscreenGraphics);

					// paint back buffer to main graphics
					if (! volatileImg.contentsLost()) {
						g.drawImage(volatileImg, 0, 0, this);
					}
					// Test if content is lost
				} while(volatileImg.contentsLost());
			}

			private synchronized void offscreenPaint(final Graphics2D g) {
				g.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON
				);
				g.setRenderingHint(
					RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR
				);

				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());

				int fluidCanvasWidth = fluidCanvas.getWidth();
				int fluidCanvasHeight = fluidCanvas.getHeight();

				final int outputCount = outputs.size();

				final int simDrawSize = Math.min(getWidth(), getHeight());

				double pixelWidth = simDrawSize / fluidCanvasWidth;
				double pixelHeight = simDrawSize / fluidCanvasHeight;

				float[] rgb = new float[3];

				Image scaledFluid = fluidCanvas.getImage();

				for (int i=0; i<outputs.size(); i++) {
					if ((i==0 && oscShowDome1Overlay.value()) || (i==1 && oscShowDome2Overlay.value())) {
						if (oscShowOutline.value()) {
							outputs.get(i).getImageMapper().drawMask(
								g,
								0,
								0,
								simDrawSize,
								simDrawSize,
								oscShowOverlayAddresses.value(),
								oscShowVertices.value()
							);
						}

						if (oscShowOutputOverlay.value()) {
							outputs.get(i).getImageMapper().drawPanelState(
								outputs.get(i).getCanvas(),
								g,
								0,
								0,
								simDrawSize,
								simDrawSize,
								oscShowVertices.value(),
								0.5f
							);
						}
					}
				}

				if (oscShowFluidOverlay.value()) {
					try {
						((Graphics2D) g).drawImage(
							scaledFluid,
							0,
							0,
							simDrawSize,
							simDrawSize,
							null
						);
					} catch (Exception e) {
						/* This happens sometimes. Oh well. */
					}
				}

				// Draw the current color palette
				double swatchHeight = (double) (simDrawSize) / currentPalette.getColors().length;
				for (int i=0; i<currentPalette.getColors().length; i++) {
					g.setColor(
						currentPalette.getColors()[i]
					);
					g.fillRect(
						0,
						(int) (i*swatchHeight),
						20,
						(int) (swatchHeight + 1)
					);
				}

				// Let the current program draw if it would like
				activeProgram().drawOverlay(g, getWidth(), getHeight());
			}

			public void update(Graphics g) {
				paint(g);
			}

			public Dimension getPreferredSize() {
				return new Dimension(480, 480);
			}
		};

		statusWindow.setLayout(new BorderLayout());
		statusWindow.add(statusWidget, BorderLayout.CENTER);

		if (KinectInput.instance().isKinectEnabled()) {
			kinectWidget = new KinectDisplay();
			statusWindow.add(kinectWidget, BorderLayout.EAST);
		}

		statusWindow.pack();
	}

	private void start() {
		Thread t = new Thread(this);
		t.run();
	}

	@Override
	public void run() {
		initOutput();
		initPrograms();
		createStatusWindow();
		initOsc();

		doorProgram.init(this);

		while (true) {
			long frameStart = System.currentTimeMillis();

			synchronized (this) {
				for (DomeOutput output : outputs) {
					output.getCanvas().clear();
				}

				frameCounter++;
				try {
					renderFrame();
				} catch (Exception e) {
					System.err.println("Failed to render frame; " + e.getClass().getSimpleName() + ": " + e.getMessage());
					e.printStackTrace();
					Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
				}
			}

			try {
				final long sleepMillis = (1000 / FPS) - (System.currentTimeMillis() - frameStart);
				Thread.sleep(Math.max(0, sleepMillis));
			} catch (InterruptedException e) {}
		}
	}

	private void initOsc() {
		if (STATE_FILE.exists()) {
			OscHelper.instance().restoreState(STATE_FILE);
		}
	}

	private void initOutput() {
		for (DomeOutput output : outputs) {
			output.start(domeControllerHost);
		}
	}

	@Override
	public synchronized void selectProgram(DomeAnimationProgram.ProgramId newProgramId) {
		if (newProgramId != this.activeProgramId) {
			if (this.activeProgramId != null) {
				activeProgram().stop();
			}

			this.activeProgramId = newProgramId;
			this.activeProgram().start();

			for (ProgramEntry entry : programMap.values()) {
				entry.oscStatus.setValue(entry.program.getProgramId() == newProgramId);
			}

			OscHelper.instance().pushToKnownHosts();
		}
	}

	@Override
	public void displayImage(final BufferedImage image) {
		for (DomeOutput output : outputs) {
			output.getImageMapper().drawImage(
				image,
				output.getCanvas()
			);
		}

		if (arduinoOutput.isPresent()) {
			arduinoOutput.get().writeImage(image);
		}
	}

	private void heartbeat() {
		if (frameCounter % FPS == 0) {
			oscHeartBeat.setValue(! oscHeartBeat.value());
			OscHelper.instance().pushToKnownHosts();
			OscHelper.instance().saveState(STATE_FILE);
		}
	}

	private void renderFrame() {
		checkForProgramChange();
		updatePalette();

		heartbeat();
		activeProgram().update();

		fluidCanvas.update((float) oscOverallFluidIntensity.getValue());

		if (KinectInput.instance().isKinectEnabled()) {
			kinectWidget.updateDepth();
			kinectWidget.repaint();
		}

		sendFrame();

		// Update the status widget after sending the frame so we can display the current state of all the panels
		statusWidget.repaint();

		// Send data to arduino if possible
		if (arduinoOutput.isPresent() && activeProgramId.isFluidBased()) {
			arduinoOutput.get().writeImage(fluidCanvas.getImage());
		}
	}

	private void sendFrame() {
		for (DomeOutput output : outputs) {
			if (activeProgram().getProgramId().isFluidBased()) {
				output.getImageMapper().drawImage(
					fluidCanvas.getImage(),
					output.getCanvas()
				);
			}
		}

		if (activeProgramId != DomeAnimationProgram.ProgramId.DEBUG) {
			// Run the door program after the panels have been calculated so it can use the rendered data
			doorProgram.update();
		}

		for (DomeOutput output : outputs) {
			output.send();
		}
	}

	private void checkForProgramChange() {
		for (ProgramEntry entry : programMap.values()) {
			if (entry.program.isFocusDesired()) {
				selectProgram(entry.program.getProgramId());
				return;
			}
		}

		selectProgram(defaultProgramId);
	}

	@Override
	public List<DomeOutput> getOutputs() {
		return outputs;
	}

	@Override
	public DomeFluidCanvas getFluidCanvas() {
		return fluidCanvas;
	}


	private void updatePalette() {
		DomeColorManager.DomePaletteSpec newSpec = DomeColorManager.instance().specFor(
			oscPaletteType.floatValue(),
			oscPaletteHue.floatValue(),
			oscPaletteSize.intValue()
		);

		if (! newSpec.equals(currentPaletteSpec)) {
			currentPaletteSpec = newSpec;
			currentPalette = DomeColorManager.instance().paletteFor(currentPaletteSpec);

			oscPaletteTypeLabel.setValue(newSpec.getStrategyName());
			oscPaletteHueLabel.setValue(newSpec.getHueName());
			OscHelper.instance().pushToKnownHosts();
		}
	}

	@Override
	public Color getColor(final float color) {
		return currentPalette.getColor(color);
	}

	protected class ProgramEntry {
		DomeAnimationProgram program;
		OscHelper.OscBoolean oscStatus;

		public ProgramEntry(final DomeAnimationProgram program) {
			this.program = program;
			this.oscStatus = OscHelper.booleanValue(program.getProgramId().getOscIndicatorAddress(), false);
		}
	}
}
