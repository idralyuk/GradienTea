package org.hypher.gradientea.artnet.player.animations;

import org.hypher.gradientea.artnet.player.io.BasicAudioReader;
import org.hypher.gradientea.artnet.player.io.GlobalAudioReader;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * A simple little app to play with FFT data
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AudioVisualizationTest {

	public static void main(String[] args) {

		final JFrame frame = new JFrame("FFT Test"){
			int y = 0;
			@Override
			public void paint(final Graphics graphics) {
				final Graphics2D g = (Graphics2D) graphics;

				float[] rawFft = GlobalAudioReader.getReader().getBuffer(0).getFreqDomainData();
				float[] compressed = new float[12];
				BasicAudioReader.compressLogWithRms(rawFft, 8, 128, compressed);

				float[] dataToUse = compressed;

				float average = GlobalAudioReader.getReader().getRMSMean(0.5f).mean();

				graphics.setColor(Color.black);
				graphics.fillRect(0, y, getWidth(), 2);

				int highestIndex = highestIndex(dataToUse);

				for (int i=0; i<getWidth(); i++) {
					final int index = (int) (((double) i / getWidth()) * dataToUse.length);
					float sample = dataToUse[index];
					float intensity = (float) Math.log(Math.abs(sample) + 1) * 0.5f;

					graphics.setColor(Color.getHSBColor(
						(float)i/getWidth(),
						highestIndex == index ? 1f : .2f,
						Math.max(0, Math.min(1, intensity))
					));
					graphics.fillRect(i, y, 1, 2);
				}

				float rms = GlobalAudioReader.getReader().getBuffer(0).getRms()*100;
				graphics.setColor(Color.blue);
				graphics.drawRect((int)(getWidth() - rms), y, (int)(rms), 2);

				y = (y + 2) % getHeight();

				graphics.setColor(Color.white);
				graphics.drawLine(0, y, getWidth(), y);
			}

			private int highestIndex(final float[] data) {
				float highestValue = 0;
				int highestIndex = 0;

				for (int i=0; i<data.length; i++) {
					if (data[i] > highestValue) {
						highestValue = data[i];
						highestIndex = i;
					}
				}

				return highestIndex;
			}
		};
		frame.setSize(1024, 500);
		frame.setVisible(true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					frame.repaint();

					try {
						Thread.sleep(33);
					} catch (InterruptedException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			}
		}).start();
	}
}
