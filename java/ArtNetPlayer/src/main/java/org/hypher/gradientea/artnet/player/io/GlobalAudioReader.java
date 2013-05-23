package org.hypher.gradientea.artnet.player.io;

import org.hypher.gradientea.artnet.player.io.BasicAudioReader;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class GlobalAudioReader {
	private static BasicAudioReader reader;

	public static BasicAudioReader getReader() {
		if (reader == null) {
			reader = new BasicAudioReader(5.0f);
		}

		reader.start();
		return reader;
	}

	public static void stopReader() {
		if (reader != null) {
			reader.stop();
		}
	}
}
