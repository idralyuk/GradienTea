package org.hypher.gradientea.artnet.player.demo;

import org.hypher.gradientea.artnet.player.demo.io.BasicAudioReader;

/**
 * Class for testing and playing with audio input.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class AudioTester implements Runnable {
	private final static int METER_WIDTH = 40;
	private final float SAMPLES_PER_SECOND = 40;

	public static void main(String[] args) {
		new AudioTester().run();
	}

	public void run() {
		BasicAudioReader.printMixerInfo();

		printLabel("Short Avg");
		printLabel("Long Avg");
		printLabel("Short Scaled to Long");
		System.out.println();

		long lastRead = System.nanoTime();

		final BasicAudioReader reader = GlobalAudioReader.getReader();

		while (true) {
			float currentLevel = reader.getBufferRMS(0);
			BasicAudioReader.LevelAverage longAverage = reader.getRMSMean(1.0f);

			final long nanosNow = System.nanoTime();

			printLevel(currentLevel);
			printMovingMean(longAverage);
			printLevel(longAverage.scale(currentLevel));

			System.out.print(" dur: " + (nanosNow - lastRead) / 1000000.0 + "ms (desired: " + (1/SAMPLES_PER_SECOND)*1000 + "ms)");
			System.out.print("\n");

			lastRead = nanosNow;

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
	}

	private void printLabel(String label) {
		if (label.length() > METER_WIDTH) {
			System.out.print(label.substring(0, METER_WIDTH));
		} else {
			System.out.print(label);
			for (int i=label.length(); i<METER_WIDTH; i++) {
				System.out.print(' ');
			}
		}

		System.out.print('|');
	}

	private void printMovingMean(BasicAudioReader.LevelAverage avg) {
		StringBuilder b = new StringBuilder(100);

		final int lowest = (int) (avg.lowest() * METER_WIDTH);
		final int highest = (int) (avg.highest() * METER_WIDTH);
		final float mean = METER_WIDTH * avg.mean();

		for (int i=0; i<METER_WIDTH; i++) {
			if (i == lowest) {
				b.append('<');
			}
			else if (i == highest) {
				b.append('>');
			}
			else if (i < mean) {
				b.append('=');
			}
			else {
				b.append(' ');
			}
		}
		b.append("|");

		System.out.print(b);
	}

	private void printLevel(final float level) {
		StringBuilder b = new StringBuilder(100);
		for (int i=0; i<METER_WIDTH; i++) {
			b.append( i <METER_WIDTH*level ? '=' : ' ');
		}
		b.append("|");

		System.out.print(b);
	}

	private void printDifference(final float diff) {
		StringBuilder b = new StringBuilder(100);
		float halfWidth = METER_WIDTH/2;
		for (int i=0; i<METER_WIDTH; i++) {
			if (i == (int)halfWidth) b.append("0");
			else if (diff <= 0 && i < halfWidth && i > halfWidth+halfWidth*diff) b.append("-");
			else if (diff > 0 && i > halfWidth && i < halfWidth+halfWidth*diff) b.append("+");
			else b.append(" ");
		}
		b.append("|");

		System.out.print(b);
	}
}
