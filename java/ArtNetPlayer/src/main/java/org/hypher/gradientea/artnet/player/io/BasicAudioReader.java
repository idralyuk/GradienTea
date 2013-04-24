package org.hypher.gradientea.artnet.player.io;

import com.google.common.collect.Maps;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Map;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class BasicAudioReader {
	public static String[] DESIRED_MIXERS = new String[]{
		"display audio",
		"built-in micro"
	};

	public static AudioFormat DESIRED_FORMAT = new AudioFormat(48000, 16, 1, true, true);

	private Thread thread;
	private float desiredSampleDurationSeconds;
	private boolean running = false;
	private TargetDataLine microphone;

	public BasicAudioReader(float desiredSampleDurationSeconds) {
		this.desiredSampleDurationSeconds = desiredSampleDurationSeconds;

		this.microphone = selectDesiredInput();
	}

	static long timerStarted;
	static long timerLastRead;
	public static void startTimer() {
		timerStarted = System.nanoTime();
		//System.out.println();
	}

	public static void printTimer(String name) {
		long now = System.nanoTime();
		//System.out.println((now-timerStarted)/1000000 + "ms: " + name + " (+" + (now-timerLastRead)/1000000 + "ms)");
		timerLastRead = now;
	}

	public void start(final AudioDataCallback callback) {
		if (running) return;

		running = true;

		final long desiredSampleDurationMs = (long) (desiredSampleDurationSeconds * 1000);
		final int maxBufferSize = (int) (desiredSampleDurationSeconds * 2 * 48000 * 2);

		thread = new Thread(new Runnable() {
			byte[] buffer = new byte[maxBufferSize];
			float[] normalizedBuffer = new float[maxBufferSize/2];
			ShortBuffer shortBuffer = ByteBuffer.wrap(buffer).asShortBuffer();

			long nextFrameDuration = desiredSampleDurationMs;

			protected int readSample() {
				microphone.start();
				printTimer("Mic Started");

				try {
					Thread.sleep(desiredSampleDurationMs);
				} catch (InterruptedException e) {}

				int bytesAvailable = microphone.available();
				int bytesDesired = Math.min(bytesAvailable, maxBufferSize);

				int bytesRead = microphone.read(buffer, 0, bytesDesired);
				printTimer("Read");

				if (bytesDesired < bytesAvailable) {
					microphone.flush();
					printTimer("Flushed");
				}

				microphone.stop();
				printTimer("Mic Stopped");

				if (bytesRead > 0) {
					for (int i=0; i<bytesRead/2; i++) {
						normalizedBuffer[i] = Math.max(0, Math.abs(shortBuffer.get(i)) / (float)Short.MAX_VALUE - 0.01f);
					}
					printTimer("Normalized");
				} else {
					return 0;
				}

				return bytesRead/2;
			}

			@Override
			public void run() {
				try {
					microphone.open(DESIRED_FORMAT);

					callback.onAudioStart();

					while (running) {
						startTimer();

						long frameStart = System.currentTimeMillis();
						int valuesRead = readSample();

						printTimer("Sample Read");

						if (valuesRead > 0) {
							callback.onAudioData(
								normalizedBuffer,
								valuesRead,
								(valuesRead / DESIRED_FORMAT.getSampleRate())
							);
						}

						printTimer("Callback Called");

						long frameDuration = System.currentTimeMillis() - frameStart;
						if (frameDuration > desiredSampleDurationMs && nextFrameDuration > 1 && nextFrameDuration < 1000) {
							nextFrameDuration --;
						} else {
							nextFrameDuration ++;
						}

						printTimer("Loop Over");
					}

					microphone.close();
				} catch (Exception e) {
					System.err.println("Audio Capture Failed!");
					e.printStackTrace();
				}
			}
		});

		thread.setDaemon(true);
		thread.start();
	}

	public static TargetDataLine selectDesiredInput() {
		Map<String, TargetDataLine> mixerNameLineMap = Maps.newLinkedHashMap();

		try {
			for (Mixer.Info mixer :  AudioSystem.getMixerInfo()) {
				Line.Info[] targetLines = AudioSystem.getMixer(mixer).getTargetLineInfo();
				if (targetLines.length > 0) {
					Line line = AudioSystem.getLine(targetLines[0]);

					if (line instanceof TargetDataLine) {
						mixerNameLineMap.put(mixer.toString().toLowerCase(), (TargetDataLine) line);
					}
				}
			}
		} catch (LineUnavailableException e) {
			throw new RuntimeException(e);
		}

		TargetDataLine selectedLine = null;

		for (String desiredName: DESIRED_MIXERS) {
			for (Map.Entry<String, TargetDataLine> entry : mixerNameLineMap.entrySet()) {
				if (entry.getKey().contains(desiredName.toLowerCase())) {
					System.out.println("Reading Audio From Desired Input: " + entry.getKey());
					return entry.getValue();
				}
			}
		}

		if (mixerNameLineMap.isEmpty()) {
			throw new RuntimeException("No Mixers Available for Input");
		}

		System.out.println("Reading Audio From First Device: " + mixerNameLineMap.keySet().iterator().next());
		return mixerNameLineMap.values().iterator().next();
	}

	public static void printMixerInfo() {
		try {
			Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
			for (Mixer.Info mixerInfo: mixerInfos){
				Mixer m = AudioSystem.getMixer(mixerInfo);
				Line.Info[] lineInfos = m.getSourceLineInfo();
				System.out.println ("Mixer: " + mixerInfo);
				for (Line.Info lineInfo:lineInfos){
					System.out.println("\tLineInfo" + lineInfo);

					Line line = m.getLine(lineInfo);
					System.out.println("\t\tLine: "+line);
				}

				lineInfos = m.getTargetLineInfo();
				for (Line.Info lineInfo:lineInfos){
					final Line line = m.getLine(lineInfo);
					if (line instanceof TargetDataLine) {
						TargetDataLine targetDataLine = (TargetDataLine) line;
						System.out.println("\tTargetDataLine:" + lineInfo + " -- " + targetDataLine.getFormat());
					}
				}
			}
		} catch (LineUnavailableException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}


	/**
	 * 	 * @param data
	 * @param count
	 * @return
	 */
	public static float volumeRMS(
		float[] data,
		int count
	) {
		float meanSquareSum = 0f;
		int usedCount = 0;
		for (int i = 0; i < count; i++) {
			if (data[i] != Double.POSITIVE_INFINITY && data[i] != Double.NEGATIVE_INFINITY) {
				meanSquareSum += data[i] * data[i];
				usedCount ++;
			}
		}
		return (float) Math.sqrt(meanSquareSum / usedCount);
	}

	public static float mean(float[] data, int length) {
		if (length == 0) return 0.0f;

		float total = 0;
		int count = 0;
		for (int i=0; i<length; i++) {
			if (data[i] != Double.POSITIVE_INFINITY && data[i] != Double.NEGATIVE_INFINITY) {
				total += data[i];
				count ++;
			}
		}

		return total / count;
	}

	public void stop() {
		if (running) {
			running = false;
			microphone.close();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion

	public interface AudioDataCallback {
		void onAudioStart();
		void onAudioData(float[] normalizedBuffer, int sampleCount, float lengthInSeconds);
	}
}
