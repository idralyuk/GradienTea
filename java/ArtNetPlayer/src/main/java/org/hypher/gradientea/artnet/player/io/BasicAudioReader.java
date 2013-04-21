package org.hypher.gradientea.artnet.player.io;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class BasicAudioReader {
	public BasicAudioReader() throws LineUnavailableException {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info mixerInfo: mixerInfos){
			Mixer m = AudioSystem.getMixer(mixerInfo);
			Line.Info[] lineInfos = m.getSourceLineInfo();
			for (Line.Info lineInfo:lineInfos){
				System.out.println (mixerInfo+" :: "+lineInfo);
				Line line = m.getLine(lineInfo);
				System.out.println("\t-----"+line);
			}
			lineInfos = m.getTargetLineInfo();
			for (Line.Info lineInfo:lineInfos){
				System.out.println (mixerInfo);
				final Line line = m.getLine(lineInfo);
				if (line instanceof TargetDataLine) {
					TargetDataLine targetDataLine = (TargetDataLine) line;
					System.out.println("\t" + lineInfo + " -- " + targetDataLine.getFormat());
				} else {
					System.out.println("\t" + lineInfo + " -- " + line);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
