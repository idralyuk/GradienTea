package org.hypher.gradientea.artnet.player.io;

/**
 * Simple utility class that provides the current audio level relative to a recent average.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RelativeAudioLevelReader implements BasicAudioReader.AudioDataCallback {
	private BasicAudioReader audioReader;
	private MovingAverage movingAverage;
	private float lastSample;

	public RelativeAudioLevelReader(
		float averageSeconds,
		float sampleSeconds
	) {
		audioReader = new BasicAudioReader(sampleSeconds);
		movingAverage = new MovingAverage((int) (averageSeconds / sampleSeconds));
	}

	public void start() {
		audioReader.start(this);
	}

	public void stop() {
		audioReader.stop();
	}

	@Override
	public void onAudioStart() {
		movingAverage.clear();
	}

	@Override
	public void onAudioData(final float[] normalizedBuffer, final int sampleCount, final float lengthInSeconds) {
		lastSample = BasicAudioReader.volumeRMS(normalizedBuffer, sampleCount);
		movingAverage.add(lastSample);
	}

	public float getRelativeLevel() {
		return movingAverage.scale(lastSample);
	}
}
