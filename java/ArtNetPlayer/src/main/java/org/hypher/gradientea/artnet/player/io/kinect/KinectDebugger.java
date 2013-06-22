package org.hypher.gradientea.artnet.player.io.kinect;

import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class KinectDebugger {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Kinect Debugger");
		frame.setSize(800, 600);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		final KinectDebugDisplay debugDisplay = new KinectDebugDisplay();
		frame.add(debugDisplay);
		frame.setVisible(true);

		while (true) {
			debugDisplay.updateDepth();
			debugDisplay.repaint();
		}
	}
}
