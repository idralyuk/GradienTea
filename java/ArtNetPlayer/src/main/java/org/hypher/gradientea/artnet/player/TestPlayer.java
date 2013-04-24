package org.hypher.gradientea.artnet.player;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import fr.azelart.artnetstack.constants.Constants;
import org.hypher.gradientea.animation.shared.function.DefinedAnimation;
import org.hypher.gradientea.animation.shared.function.ExpandedAnimationWrapper;
import org.hypher.gradientea.animation.shared.function.HsbTween;
import org.hypher.gradientea.animation.shared.function.SingleDefinedAnimation;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.animation.shared.color.PixelColor;
import org.hypher.gradientea.animation.shared.color.RgbColor;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxPixel;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;
import org.hypher.gradientea.animation.shared.RenderableAnimation;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class TestPlayer {
	public final static int PIXEL_COUNT = 100;

	interface GPIOMapping {
		Pin SCROLL_CLK = RaspiPin.GPIO_02; // Marked 17
		Pin SCROLL_DATA = RaspiPin.GPIO_00; // Marked 27
		Pin SCROLL_PUSH = RaspiPin.GPIO_03; // Marked 22
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Please specify one argument indicating the ArtNet address to send to");
			System.exit(-1);
		}

		String broadcastAddress = args[0];

		final ArtNetAnimationPlayer player = new ArtNetAnimationPlayer();

		player.start(
			InetAddress.getLocalHost(),
			InetAddress.getByName(broadcastAddress),
			Constants.DEFAULT_ART_NET_UDP_PORT
		);

		while (true) {
			for (int v : new int[] {0,75,125,255}) {
				List<PixelValue> values = Lists.newArrayList();
				for (int i=1;i<510;i+=3) {
					values.add(new PixelValue(new DmxPixel(1, i), new RgbColor(v,v,v)));
				}
				player.display(values);

				System.out.println(v + " / 255");
				System.in.read();
			}
		}

	}

	private final static int ANIMATION_COUNT = 6;

	private static void playAnimation(ArtNetAnimationPlayer player, int index) {
		int correctedIndex = index % ANIMATION_COUNT;
		if (correctedIndex < 0) {
			correctedIndex = ANIMATION_COUNT + correctedIndex;
		}

		System.out.println("Playing " + correctedIndex);

		switch (correctedIndex) {
			case 0: omniRainbow(player, PIXEL_COUNT); break;
			case 1: solidColor(player, PIXEL_COUNT, new HsbColor(255,0,1.0)); break;
			case 2: movingRainbow(player, PIXEL_COUNT); break;
			case 3: fadingRainbow(player, PIXEL_COUNT); break;
			case 4: movingDot(player, PIXEL_COUNT, 1); break;
			case 5: strobe(player, PIXEL_COUNT, 8, 0.005); break;
		}
	}

	private static void omniRainbow(final ArtNetAnimationPlayer player, final int pixelCount) {
		final OmniColor omni = new OmniColor();

		player.playAnimations(
			Arrays.asList(
				new RenderableAnimation(new DefinedAnimation() {
					CompositingFrameBuffer frame = new CompositingFrameBuffer(pixelCount);

					@Override
					public List<PixelValue> render(final double fraction) {
						frame.clear();

						for (int i=0; i<pixelCount; i++) {
							frame.pixel(i, omni.mapHue(rotate(i/(double)pixelCount, fraction)), 1.0, 1.0);
						}

						return frame.render();
					}
				},
					10
				)
			)
		);
	}

	private static void solidColor(final ArtNetAnimationPlayer player, final int pixelCount, final HsbColor hsbColor) {
		player.playAnimations(
			Arrays.asList(
				new RenderableAnimation(
					new SingleDefinedAnimation(
						new ExpandedAnimationWrapper(
							new HsbTween(hsbColor, hsbColor),
							ExpandedAnimationWrapper.TRIANGLE,
							1.0
						),
						DmxPixel.pixels(1, pixelCount)
					),
					100
				)
			)
		);
	}


	private static void fft(ArtNetAnimationPlayer player, final int pixelCount) throws LineUnavailableException {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info mixerInfo: mixerInfos){
			Mixer m = AudioSystem.getMixer(mixerInfo);
			Line.Info[] lineInfos = m.getSourceLineInfo();

			System.out.println (mixerInfo);

			for (Line.Info lineInfo:lineInfos){
				Line line = m.getLine(lineInfo);
				System.out.println("\t" + lineInfo + " -- " + line);
			}
			lineInfos = m.getTargetLineInfo();
			for (Line.Info lineInfo:lineInfos){
				TargetDataLine line = (TargetDataLine) m.getLine(lineInfo);
				System.out.println("\t" + lineInfo + " -- " + line);
			}
		}

		final AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
		final TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(AudioSystem.getMixer(mixerInfos[1]).getTargetLineInfo()[0]);

		microphone.open(format);
		microphone.start();

		player.playAnimations(
			Arrays.asList(
				new RenderableAnimation(new DefinedAnimation() {
					FadingFrameBuffer frame = new FadingFrameBuffer(pixelCount);

					protected double[] readAudio(double seconds) {

						int sampleByteSize = (int) Math.max(seconds * format.getSampleRate() * format.getSampleSizeInBits()/8, microphone.available());
						byte[] buffer = new byte[(int) (sampleByteSize)];

						int readBytes = microphone.read(buffer, 0, buffer.length);

						if (readBytes > 0 ) {
							DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(buffer, 0, readBytes));
							double[] values = new double[readBytes/(format.getSampleSizeInBits()/8)];

							int valueCount=0;
							try {
								while (inputStream.available() > 0) {
									values[valueCount++] = ((double)inputStream.readShort() / Short.MAX_VALUE) / 10.0;
								}
							} catch (IOException e) {}

							return values;
						}

						return new double[0];
					}

					@Override
					public List<PixelValue> render(final double fraction) {
						frame.fadeBrightnessByPercentage(0.2);

						double[] data = readAudio(0.01);

						FFTSample sample = new FFTSample(format.getSampleRate(), data);

						sample.findMaxima();
//
//						for (int i=0; i<pixelCount; i++) {
//							if (pixelData[i] > 0.05) {
//								frame.pixel(
//									i,
//									(double) i / pixelCount,
//									1.0,
//									pixelData[i]
//								);
//							}
//						}

						return frame.render();
					}
				},
					100.0
				)
			)
		);
	}

	private static void movingRainbow(final ArtNetAnimationPlayer player, final int pixelCount) {
		player.playAnimations(
			Arrays.asList(
				new RenderableAnimation(
					new SingleDefinedAnimation(
						new ExpandedAnimationWrapper(
							new HsbTween(new HsbColor(0, 1.0, 0.5), new HsbColor(1.0, 1.0, 1.0)),
							ExpandedAnimationWrapper.TRIANGLE,
							1.0
						),
						DmxPixel.pixels(1, pixelCount)
					),
					5
				)
			)
		);
	}


	private static void fadingRainbow(final ArtNetAnimationPlayer player, final int pixelCount) {
		final OmniColor omni = new OmniColor();

		player.playAnimations(
			Arrays.asList(
				new RenderableAnimation(new DefinedAnimation() {
					CompositingFrameBuffer frame = new CompositingFrameBuffer(pixelCount);

					@Override
					public List<PixelValue> render(final double fraction) {
						frame.clear();

						for (int i=0; i<pixelCount; i++) {
							frame.pixel(i, omni.mapHue(fraction), 1.0, 1.0);
						}

						return frame.render();
					}
				},
					5
				)
			)
		);
	}

	private static void movingVuMeter(ArtNetAnimationPlayer player, final int pixelCount) throws LineUnavailableException {
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
				TargetDataLine line = (TargetDataLine) m.getLine(lineInfo);
				System.out.println("\t" + lineInfo +" -- "+line.getFormat());
			}

		}

		AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
		final TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(AudioSystem.getMixer(mixerInfos[1]).getTargetLineInfo()[0]);

		microphone.open(format);
		microphone.start();

		player.playAnimations(
			Arrays.asList(
				new RenderableAnimation(new DefinedAnimation() {
					FadingFrameBuffer frame = new FadingFrameBuffer(pixelCount);

					double step = 0;
					byte[] buffer = new byte[4096];

					LinkedList<Double> lastValues = Lists.newLinkedList();
					LinkedList<Double> lastAverages = Lists.newLinkedList();

					@Override
					public List<PixelValue> render(final double fraction) {
						frame.fadeBrightnessByPercentage(0.85);

						microphone.flush();
						int readBytes = microphone.read(buffer, 0, Math.min(buffer.length, microphone.available()));

						DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(buffer, 0, readBytes));

						double sum = 0.0;
						int readValues = 0;

						try {
							while (inputStream.available() > 0) {
								sum += inputStream.readShort();
								readValues ++;
							}
						} catch (IOException e) {}

						double average = sum / (readValues);
						double usefulValue = Math.log(1+Math.abs(average)) / Math.log(Short.MAX_VALUE) * 2;

						//System.out.println(usefulValue);

						lastValues.addLast(usefulValue);
						while (lastValues.size() > 20) {
							lastValues.removeFirst();
						}

						double timeSum = 0;
						for (Double v : lastValues) {
							timeSum += v;
						}
						double timeAverage = Math.min(timeSum / lastValues.size(), 1.0);

						lastAverages.addLast(timeAverage);
						if (lastAverages.size() > 200) {
							lastAverages.removeFirst();
						}

						Double localMaxima = Collections.max(lastAverages);
						double relativeValue = timeAverage / localMaxima;

						for (int i=0; i<(int) (relativeValue * pixelCount); i++) {
							frame.pixel((int) (rotate((double)i/pixelCount * 0.2, rotate(fraction%5*5, 0.0))*pixelCount), (double)i/pixelCount, 1.0, 1.0);
							frame.pixel((int) (rotate(-(double)i/pixelCount * 0.2, rotate(fraction%5*5, 0.0))*pixelCount), (double)i/pixelCount, 1.0, 1.0);

							frame.pixel((int) (rotate((double)i/pixelCount * 0.2, rotate(fraction%5*5, 0.5))*pixelCount), (double)i/pixelCount, 1.0, 1.0);
							frame.pixel((int) (rotate(-(double)i/pixelCount * 0.2, rotate(fraction%5*5, 0.5))*pixelCount), (double)i/pixelCount, 1.0, 1.0);
						}


//						frame.pixel((int) (localMaxima*pixelCount), fraction%40 * 40, 1.0, 1.0);
//						frame.pixel((int) (localMaxima*pixelCount)+1, fraction%40 * 40, 1.0, 1.0);
//						frame.pixel((int) (localMaxima*pixelCount)+2, fraction%40 * 40, 1.0, 1.0);

						return frame.render();
					}
				},
					100.0
				)
			)
		);
	}

	protected static void movingDot(ArtNetAnimationPlayer player, final int pixelCount, double duration) {
		player.playAnimations(
			Arrays.asList(
				new RenderableAnimation(
					new DefinedAnimation() {
						CompositingFrameBuffer frame = new CompositingFrameBuffer(pixelCount);

						double step = 0;

						@Override
						public List<PixelValue> render(final double fraction) {
							frame.clear();
							step++;
							drawDot(rotate(1 - fraction, 0.0));

							return frame.render();
						}

						private double rotate(double value, final double by) {
							value = value + by;
							while (value < 0) value += 1;
							while (value > 1) value -= 1;

							return value;
						}

						private double compress(final double value, final double by) {
							return value * by;
						}

						private void drawDot(final double fraction) {

							frame.pixel((int) (pixelCount * fraction), fraction, 1.0, 1.0);

							int trailSize = 2;
							for (int i = 1; i <= trailSize; i++) {
								frame.pixel(
									(int) (pixelCount * fraction) + i,
									fraction,
									1.0,
									1 - i / (double) trailSize
								);
								frame.pixel(
									(int) (pixelCount * fraction) + -i,
									fraction,
									1.0,
									1 - i / (double) trailSize
								);
							}
						}
					},
					duration
				)
			)
		);
	}

	protected static void strobe(ArtNetAnimationPlayer player, final int pixelCount, double frequency, final double ratio) {
		player.playAnimations(
			Arrays.asList(
				new RenderableAnimation(new DefinedAnimation() {
					CompositingFrameBuffer frame = new CompositingFrameBuffer(pixelCount);

					@Override
					public List<PixelValue> render(final double fraction) {
						frame.clear();
						if (fraction > ratio) {
							frame.fill(0,0,0);
						} else {
							frame.fill(Math.random(),1.0,1.0);
						}

						return frame.render();
					}
				},
					1 / frequency
				)
			)
		);
	}

	protected static class CompositingFrameBuffer {
		private static final PixelColor black = new RgbColor(0,0,0);

		private int pixelCount;
		private Multimap<Integer, PixelColor> colorMap = Multimaps.newMultimap(
			Maps.<Integer, Collection<PixelColor>>newHashMap(),
			new Supplier<Collection<PixelColor>>() {
				@Override
				public Collection<PixelColor> get() {
					return Lists.newArrayList();
				}
			}
		);

		public CompositingFrameBuffer(final int pixelCount) {
			this.pixelCount = pixelCount;
		}

		public CompositingFrameBuffer pixel(int index, double hue, double saturation, double brightness) {
			return pixel(index, new HsbColor(hue, saturation, brightness));
		}

		private CompositingFrameBuffer pixel(int index, final PixelColor hsbColor) {
			while (index<0) index += pixelCount;
			while (index>pixelCount-1) index -= pixelCount;

			colorMap.put(index, hsbColor);

			return this;
		}

		public CompositingFrameBuffer pixelRgb(int index, int red, int green, int blue) {
			return pixel(index, new RgbColor(red, green, blue));
		}

		public void clear() {
			colorMap.clear();
		}

		public List<PixelValue> render() {
			List<PixelValue> values = Lists.newArrayList();

			for (int i=0; i<pixelCount; i++) {
				if (colorMap.containsKey(i)) {
					final int ii = i;
					values.addAll(
						FluentIterable.from(colorMap.get(i)).transform(
							new Function<PixelColor, PixelValue>() {
								@Nullable
								@Override
								public PixelValue apply(@Nullable final PixelColor input) {
									return new PixelValue(new DmxPixel(1, ii * 3 + 1), input);
								}
							}
						).toImmutableSet()
					);
				} else {
					values.add(new PixelValue(new DmxPixel(1, i*3+1), black));
				}
			}

			return values;
		}

		public void fill(int start, int end, double hue, double saturation, double brightness) {
			for (int i=start; i<end; i++) {
				pixel(i, hue, saturation, brightness);
			}
		}

		public void fill(double hue, double saturation, double brightness) {
			fill(0, pixelCount, hue, saturation, brightness);
		}
	}

	protected static class FadingFrameBuffer {
		private static final PixelColor black = new RgbColor(0,0,0);

		private int pixelCount;
		private Map<Integer, PixelColor> colorMap = Maps.newHashMap();

		public FadingFrameBuffer(final int pixelCount) {
			this.pixelCount = pixelCount;
		}

		public FadingFrameBuffer pixel(int index, double hue, double saturation, double brightness) {
			return pixel(index, new HsbColor(hue, saturation, brightness));
		}

		private FadingFrameBuffer pixel(int index, final PixelColor hsbColor) {
			while (index<0) index += pixelCount;
			while (index>pixelCount-1) index -= pixelCount;

			colorMap.put(index, hsbColor);

			return this;
		}

		public FadingFrameBuffer pixelRgb(int index, int red, int green, int blue) {
			return pixel(index, new RgbColor(red, green, blue));
		}

		public void clear() {
			colorMap.clear();
		}

		public List<PixelValue> render() {
			List<PixelValue> values = Lists.newArrayList();

			for (int i=0, c = 1, u = 1; i<pixelCount; i++, c += 3) {

				if (c > 510) {
					c = 1;
					u ++;
				}

				if (colorMap.containsKey(i)) {
					values.add(new PixelValue(new DmxPixel(u, c), colorMap.get(i)));
				} else {
					values.add(new PixelValue(new DmxPixel(u, c), black));
				}
			}

			return values;
		}

		public void fill(int start, int end, double hue, double saturation, double brightness) {
			for (int i=start; i<end; i++) {
				pixel(i, hue, saturation, brightness);
			}
		}

		public void fill(double hue, double saturation, double brightness) {
			fill(0, pixelCount, hue, saturation, brightness);
		}

		public void fadeBrightnessByPercentage(double percentage) {
			for (Map.Entry<Integer, PixelColor> entry : colorMap.entrySet()) {
				if (entry.getValue() instanceof RgbColor) {
					RgbColor value = (RgbColor) entry.getValue();
					entry.setValue(new RgbColor(
						value.getRed() * percentage,
						value.getGreen() * percentage,
						value.getBlue() * percentage
					));
				} else {
					HsbColor value = (HsbColor) entry.getValue();
					entry.setValue(new HsbColor(
						value.getHue(),
						value.getSaturation(),
						value.getBrightness() * percentage
					));
				}
			}
		}
	}


	public static double rotate(double value, final double by) {
		value = value + by;
		while (value < 0) value += 1;
		while (value > 1) value -= 1;

		return value;
	}

	protected static double compress(final double value, final double by) {
		return value * by;
	}
}
