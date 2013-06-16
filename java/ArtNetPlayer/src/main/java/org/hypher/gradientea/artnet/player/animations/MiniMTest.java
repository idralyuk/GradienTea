package org.hypher.gradientea.artnet.player.animations;

import ddf.minim.AudioInput;
import ddf.minim.Sound;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class MiniMTest {
	public static void main(String[] args) {
		Sound sound = new Sound();
		final AudioInput lineIn = sound.getLineIn();
		final FFT fft = new FFT(1024, lineIn.sampleRate());
		fft.logAverages(30, 2);

		final BeatDetect beatDetect = new BeatDetect(1024, lineIn.sampleRate());

		final JFrame frame = new JFrame(){

			@Override
			public void paint(final Graphics graphics) {
				Graphics2D g2 = (Graphics2D) graphics;

				g2.setColor(Color.black);
				g2.fillRect(0, 0, getWidth(), getHeight());

				g2.setColor(Color.red);

				beatDetect.detect(lineIn.mix);
				if (beatDetect.isKick()) {
					g2.fillRect(10, 10, 20, 20);
				}

				fft.forward(lineIn.mix);

				int barSize = getWidth() / fft.avgSize();
				for (int i=0; i<fft.avgSize(); i++) {
					g2.setColor(Color.getHSBColor((float) i / fft.avgSize(), 1f, 1f));

					g2.fillRect(
						i*barSize,
						0,
						barSize,
						(int) (fft.getAvg(i) * getHeight() * 0.1)
					);
				}
			}
		};

		frame.setSize(640, 480);
		frame.setVisible(true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					frame.repaint();
					try {
						Thread.sleep(33);
					} catch (InterruptedException e) {}
				}
			}
		}).start();
	}
}
