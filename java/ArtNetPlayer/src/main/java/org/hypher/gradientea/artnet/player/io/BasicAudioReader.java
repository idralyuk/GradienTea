package org.hypher.gradientea.artnet.player.io;

import com.google.common.collect.Maps;
import jnt.FFT.RealFloatFFT;
import jnt.FFT.RealFloatFFT_Radix2;

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
		//"fast",
		"display audio",
		"built-in micro"
	};

	public static AudioFormat DESIRED_FORMAT = new AudioFormat(
		48000, 16, 1, true, true
	);

	private Thread thread;
	private boolean running = false;

	private TargetDataLine inputLine;

	private AudioBuffer[] buffers;
	private int currentBufferIndex = -1;
	private int buffersUsed = 0;

	private byte sampleSizeInBytes;
	private int bufferSizeInSamples;
	private int buffersToHold;
	public static final long DESIRED_FRAME_MS = 1000/30;

	public BasicAudioReader(float historyLengthSeconds) {
		this.inputLine = selectDesiredInput();

		this.sampleSizeInBytes = (byte) (DESIRED_FORMAT.getSampleSizeInBits() / 8);
		this.bufferSizeInSamples = 1024;

		this.buffersToHold = (int) ((DESIRED_FORMAT.getSampleRate() * historyLengthSeconds) / bufferSizeInSamples);

		allocateBuffers();
	}

	private void allocateBuffers() {
		buffers = new AudioBuffer[buffersToHold];
		for (int i=0; i<buffers.length; i++) {
			buffers[i] = new AudioBuffer(bufferSizeInSamples);
		}
	}

	public void start() {
		if (running) return;
		running = true;

		thread = new Thread(new Runnable() {
			byte[] byteBuffer = new byte[bufferSizeInSamples * sampleSizeInBytes];
			ShortBuffer shortBuffer = ByteBuffer.wrap(byteBuffer).asShortBuffer();

			protected void readSample(AudioBuffer audioBuffer) {
				// Read a sample
				synchronized (buffers) {
					for (int read = 0; read < byteBuffer.length;) {
						read += inputLine.read(byteBuffer, read, byteBuffer.length-read);
					}
					inputLine.flush();
				}

				// Convert the data into its normalized format
				float[] normalizedBuffer = audioBuffer.timeDomainData;
				for (int i=0; i<normalizedBuffer.length; i++) {
					normalizedBuffer[i] = (float)shortBuffer.get(i) / Short.MAX_VALUE;
				}
			}

			@Override
			public void run() {
				try {
					inputLine.open(DESIRED_FORMAT, byteBuffer.length*2);
					inputLine.start();

					while (running) {
						long start = System.currentTimeMillis();
						AudioBuffer normalizedBuffer = buffers[
							currentBufferIndex == buffers.length - 1
								? 0
								: (currentBufferIndex + 1)
						];

						if (buffersUsed < buffers.length)
							buffersUsed ++;

						normalizedBuffer.reset();
						readSample(normalizedBuffer);

						if (currentBufferIndex == buffers.length - 1) {
							currentBufferIndex = 0;
						} else {
							currentBufferIndex ++;
						}

						Thread.sleep(Math.max(0, DESIRED_FRAME_MS - (System.currentTimeMillis() - start)));
					}
				} catch (Exception e) {
					System.err.println("Audio Capture Failed!");
					e.printStackTrace();
				} finally {
					inputLine.close();
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

	public AudioBuffer getBuffer(int relativeIndex) {
		synchronized (buffers) {
			relativeIndex = currentBufferIndex - relativeIndex;
			if (relativeIndex < 0) {
				relativeIndex += buffers.length;
			}

			return buffers[relativeIndex];
		}
	}

	public float getBufferRMS(final int relativeIndex) {
		return getBuffer(relativeIndex).getRms();
	}

	public LevelAverage getRMSMean(float offsetInSeconds, float sizeInSeconds) {
		int offsetInBuffers = (int) Math.ceil((offsetInSeconds * DESIRED_FORMAT.getSampleRate()) / bufferSizeInSamples);
		int sizeInBuffers = (int) Math.ceil((sizeInSeconds * DESIRED_FORMAT.getSampleRate()) / bufferSizeInSamples);

		if (offsetInBuffers + sizeInBuffers > buffersUsed) {
			sizeInBuffers = buffersUsed - offsetInBuffers;
		}

		float sum = 0f;
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;

		for (int i=offsetInBuffers; i<offsetInBuffers + sizeInBuffers; i++) {
			float v = getBufferRMS(i);

			if (v > max) max = v;
			if (v < min) min = v;
			sum += v;
		}

		return new LevelAverage(min, max, sum / sizeInBuffers);
	}

	public LevelAverage getRMSMean(float windowSize) {
		return getRMSMean(0f, windowSize);
	}

	public int getAvailableBuffers() {
		return buffersUsed;
	}

	public float getBufferLengthInSeconds() {
		return bufferSizeInSamples / DESIRED_FORMAT.getSampleRate();
	}

	/**
	 * @return
	 */
	public static float volumeRMS(
		float[] data,
		int startOffset,
		int endOffset
	) {
		if ((endOffset - startOffset) <= 0) return 0.0f;

		float meanSquareSum = 0f;
		int usedCount = 0;
		for (int i = startOffset; i < endOffset && i < data.length; i++) {
			if (data[i] != Double.POSITIVE_INFINITY && data[i] != Double.NEGATIVE_INFINITY) {
				meanSquareSum += data[i] * data[i];
				usedCount ++;
			}
		}
		return (float) Math.sqrt(meanSquareSum / usedCount);
	}

	public static float mean(float[] data, int startOffset, int endOffset) {
		if ((endOffset - startOffset) <= 0) return 0.0f;

		float total = 0;
		int count = 0;
		for (int i=startOffset; i<startOffset+endOffset; i++) {
			if (data[i] != Double.POSITIVE_INFINITY && data[i] != Double.NEGATIVE_INFINITY) {
				total += data[i];
				count ++;
			}
		}

		return total / count;
	}

	public static void compressLinearWithRms(float[] input, float[] output) {
		float sizeFactor = input.length / output.length;

		for (int i=0; i<output.length; i++) {
			int start = (int)(i*sizeFactor);
			int end = (int)((i+1)*sizeFactor);

			if (end == start) end = start + 1;

			output[i] = volumeRMS(input, start, end);
		}
	}

	public static void compressLogWithRms(float[] input, int inputStart, int inputEnd, float[] output) {
		int inputLength = inputEnd - inputStart;
		double factor = Math.pow(inputLength, 1.0/output.length);

		for (int i=0; i<output.length; i++) {
			int start = inputStart + (int)(Math.pow(factor, i));
			int end = inputStart + (int)(Math.pow(factor, i+1));

			if (end == start) end = start + 1;

			output[i] = volumeRMS(input, start, end);
		}
	}

	public void stop() {
		if (running) {
			running = false;
			inputLine.close();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion

	public class LevelAverage {
		float min;
		float max;
		float mean;

		public LevelAverage(final float min, final float max, final float mean) {
			this.mean = mean;
			this.min = min;
			this.max = max;
		}

		public float scale(float v) {
			return (v - min) / (max - min);
		}

		public float capScale(float v) {
			float scaled = scale(v);
			return scaled < 0f ? 0f : (scaled > 1.0f ? 1.0f : scaled);
		}

		public float lowest() {
			return min;
		}

		public float highest() {
			return max;
		}

		public float mean() {
			return mean;
		}
	}

	public class AudioBuffer {
		private float[] timeDomainData;
		private float[] freqDomainData;

		private RealFloatFFT fft;
		private boolean fftComputed = false;

		private float rms;
		private boolean rmsComputed = false;

		protected AudioBuffer(int size) {
			this.timeDomainData = new float[size];
			this.freqDomainData = new float[size];
			this.fft = new RealFloatFFT_Radix2(size);
		}

		public void reset() {
			fftComputed = false;
			rmsComputed = false;
		}

		public float getRms() {
			if (! rmsComputed) {
				rms = volumeRMS(timeDomainData, 0, timeDomainData.length);
				rmsComputed = true;
			}

			return rms;
		}

		public float[] getTimeDomainData() {
			return timeDomainData;
		}

		public float[] getFreqDomainData() {
			if (!fftComputed) {
				fftComputed = true;

				fft();
			}

			return freqDomainData;
		}

		private void fft() {
			System.arraycopy(timeDomainData, 0, freqDomainData, 0, timeDomainData.length);
			fft.transform(freqDomainData);
		}
	}
}
