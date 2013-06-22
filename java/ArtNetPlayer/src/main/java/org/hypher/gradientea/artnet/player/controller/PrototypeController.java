package org.hypher.gradientea.artnet.player.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hypher.gradientea.artnet.player.controller.programs.DomeAnimationProgram;
import org.hypher.gradientea.artnet.player.controller.programs.ManualControlProgram;
import org.hypher.gradientea.artnet.player.controller.programs.MotionControlProgram;
import org.hypher.gradientea.artnet.player.controller.programs.MusicControlProgram;
import org.hypher.gradientea.artnet.player.controller.programs.OffProgram;
import org.hypher.gradientea.artnet.player.io.kinect.KinectDisplay;
import org.hypher.gradientea.artnet.player.io.kinect.KinectInput;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

	private OscHelper.OscBoolean oscShowDome1Overlay = OscHelper.booleanValue(OscConstants.Control.Fluid.SHOW_DOME_OVERLAY_1, true);
	private OscHelper.OscBoolean oscShowDome2Overlay = OscHelper.booleanValue(OscConstants.Control.Fluid.SHOW_DOME_OVERLAY_2, false);

	private long frameCounter = 0;

	public static void main(String[] args) {
		new PrototypeController(args.length > 0 ? args[0] : "localhost").start();
	}

	public PrototypeController(final String domeControllerHost) {
		this.domeControllerHost = domeControllerHost;
		this.fluidCanvas = new DomeFluidCanvas();

		outputs.add(new DomeOutput(GradienTeaDomeSpecs.PROTOTYPE_DOME, 0));
		outputs.add(new DomeOutput(GradienTeaDomeSpecs.GRADIENTEA_DOME, 1));

		addProgram(new OffProgram());
		addProgram(new ManualControlProgram());
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
			@Override
			public void paint(final Graphics g) {
				Graphics2D g2 = (Graphics2D) g;

				g2.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON
				);
				g2.setRenderingHint(
					RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
				);

				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());

				int fluidCanvasWidth = fluidCanvas.getWidth();
				int fluidCanvasHeight = fluidCanvas.getHeight();

				final int outputCount = outputs.size();

				double pixelWidth = getWidth() / fluidCanvasWidth;
				double pixelHeight = getHeight() / fluidCanvasHeight;

				float[] rgb = new float[3];

				Image scaledFluid = fluidCanvas.getImage();

				if (oscShowDome1Overlay.value() && outputs.size() >= 1) {
					outputs.get(0).getImageMapper().drawMask(
						g2,
						0,
						0,
						getWidth(),
						getHeight(),
						false,
						true
					);
				}


				if (oscShowDome2Overlay.value() && outputs.size() >= 2) {
					outputs.get(1).getImageMapper().drawMask(
						g2,
						0,
						0,
						getWidth(),
						getHeight(),
						false,
						true
					);
				}

				((Graphics2D) g).drawImage(
					scaledFluid,
					0,
					0,
					getWidth(),
					getHeight(),
					null
				);
			}

			public Dimension getPreferredSize() {
				return new Dimension(480, 480);
			}
		};

		statusWindow.setLayout(new BorderLayout());
		statusWindow.add(statusWidget, BorderLayout.WEST);

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

	private void heartbeat() {
		if (frameCounter % FPS == 0) {
			oscHeartBeat.setValue(! oscHeartBeat.value());
			OscHelper.instance().pushToKnownHosts();
			OscHelper.instance().saveState(STATE_FILE);
		}
	}

	private void renderFrame() {
		checkForProgramChange();

		heartbeat();
		activeProgram().update();

		fluidCanvas.update();
		statusWidget.repaint();

		if (KinectInput.instance().isKinectEnabled()) {
			kinectWidget.updateDepth();
			kinectWidget.repaint();
		}

		sendFrame();
	}

	private void sendFrame() {
		//for (DomeOutput output : outputs) {
		DomeOutput output = outputs.get(1);
			output.getImageMapper().drawImage(
				fluidCanvas.getImage(),
				output.getCanvas()
			);
			output.send();
//		}
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
	public Collection<DomeOutput> getOutputs() {
		return outputs;
	}

	@Override
	public DomeFluidCanvas getFluidCanvas() {
		return fluidCanvas;
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
