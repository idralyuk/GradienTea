package org.hypher.gradientea.artnet.player.animations;

import org.hypher.gradientea.artnet.player.io.BasicAudioReader;
import org.hypher.gradientea.artnet.player.io.GlobalAudioReader;
import org.hypher.gradientea.geometry.shared.math.DomeMath;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * A simple little app to play with FFT data
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AudioVisualizationTest {

	public static void main(String[] args) {

		final JFrame rawFftFrame = new JFrame("Raw FFT") {

			int fftDrawStart = AudioFluidTest.LOW_FREQ_BUCKET;
			int fftDrawEnd = AudioFluidTest.HIGH_FREQ_BUCKET;
			int fftDrawSize = fftDrawEnd - fftDrawStart;

			int y = 0;
			@Override
			public void paint(final Graphics graphics) {
				final Graphics2D g = (Graphics2D) graphics;

				float[] rawFft = GlobalAudioReader.getReader().getBuffer(0).getFreqDomainData();
				float[] dataToUse = rawFft;

				float average = GlobalAudioReader.getReader().getRMSMean(0.5f).mean();

				graphics.setColor(Color.black);
				graphics.fillRect(0, y, getWidth(), 2);

				int highestIndex = highestIndex(dataToUse);

				int bandWidth = (int)((float)getWidth()/fftDrawSize);

				for (int i=fftDrawStart; i<fftDrawEnd; i++) {
					float sample = dataToUse[i];
					float intensity = (float) Math.log(Math.abs(sample) + 1) * 0.25f;

					graphics.setColor(Color.getHSBColor(
						(float)i / fftDrawSize,
						.8f,
						Math.max(0, Math.min(1, intensity))
					));
					graphics.fillRect(i*bandWidth, y, bandWidth, 1);
				}

				float rms = GlobalAudioReader.getReader().getBuffer(0).getRms()*100;
				graphics.setColor(Color.blue);
				graphics.drawRect((int)(getWidth() - rms), y, (int)(rms), 2);

				y = (y + 1) % getHeight();

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
		rawFftFrame.setSize(1024, 500);
		rawFftFrame.setVisible(true);

		final JFrame fftAnalysisFrame = new JFrame("FFT Analysis") {
			private int averageQueueSize = 20;
			private int sampleQueueSize = 3;

			Deque <float[]> averageQueue = new ArrayDeque<float[]>(averageQueueSize);
			List<float[]> sampleQueue = new ArrayList<float[]>(sampleQueueSize);

			int y = 0;
			@Override
			public void paint(final Graphics graphics) {
				final Graphics2D g = (Graphics2D) graphics;

				if (averageQueue.size() >= 20) averageQueue.remove();

				float[] compressedFft = new float[AudioFluidTest.FREQUENCY_BANDS];
				BasicAudioReader.compressLogWithRms(
					GlobalAudioReader.getReader().getBuffer(0).getFreqDomainData(),
					AudioFluidTest.LOW_FREQ_BUCKET,
					AudioFluidTest.HIGH_FREQ_BUCKET,
					compressedFft
				);
				sampleQueue.add(compressedFft);

				if (sampleQueue.size() == sampleQueueSize) {
					averageQueue.add(DomeMath.average(sampleQueue));
					sampleQueue.clear();

					if (averageQueue.size() > 2) {
						graphics.setColor(Color.black);
						graphics.fillRect(0, y, getWidth(), 2);

						int highestIndex = highestIndex(compressedFft);

						int bandDrawingWidth = getWidth() / compressedFft.length;
						for (int i=0; i<compressedFft.length; i++) {
							Iterator<float[]> historyIter = averageQueue.descendingIterator();

							float sample_0 = historyIter.next()[i];
							float sample_1 = historyIter.next()[i];
							float sample_2 = historyIter.next()[i];

							boolean beat = sample_1 > sample_0 && sample_1 > sample_2;

							graphics.setColor(
								Color.getHSBColor(
									(float)i / compressedFft.length,
									1.0f,
									(float) Math.max(0, Math.min(1, sample_1*0.25))
										* (i == highestIndex ? 1f : 0.2f) // Highlight the highest band
										* (beat ? 1f : 0.2f) // And the beats
 								)
							);
							graphics.drawRect(
								i*bandDrawingWidth,
								y,
								bandDrawingWidth,
								2
							);
						}

						float rms = GlobalAudioReader.getReader().getBuffer(0).getRms()*100;
						graphics.setColor(Color.red);
						graphics.drawRect((int)(getWidth() - rms), y, (int)(rms), 2);

						y = (y + 2) % getHeight();

						graphics.setColor(Color.white);
						graphics.drawLine(0, y, getWidth(), y);
					}
				}
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
		rawFftFrame.setSize(512, 500);
		rawFftFrame.setVisible(true);

		fftAnalysisFrame.setLocation(512, 0);
		fftAnalysisFrame.setSize(512, 500);
		fftAnalysisFrame.setVisible(true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					rawFftFrame.repaint();
					fftAnalysisFrame.repaint();

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
