package org.hypher.gradientea.artnet.player;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.hypher.gradientea.artnet.player.io.DomeProperties;
import org.hypher.gradientea.transport.shared.DomeAnimationFrame;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A mapping which accepts dome animation data in the form of {@link DomeAnimationFrame}s and outputs
 * that data as DMX universe data destined for a particular set of hardware. The mapping is configured with a property
 * file for each distinct set of hardware.
 *
 * <h2>Property File Format</h2>
 *
 * <h3>Metadata</h3>
 *
 * <table>
 *     <tr><th>Name</th><th>Description</th></tr>
 *     <tr><td>metadata.shortName</td><td>Short, human readable name of the mapping</td></tr>
 *     <tr><td>metadata.dome.frequency</td><td>Frequency of the dome being mapped</td></tr>
 *     <tr><td>metadata.dome.litLayers</td><td>The number of layers of the dome being illuminated</td></tr>
 *     <tr><td>metadata.hardware.colorOrder</td><td>The color sequence of the DMX pixels. Must be the letters r, g and b in any order</td></tr>
 * </table>
 *
 * <h3>Pixel Mapping</h3>
 *
 * <h4>General</h4>
 * The mapping is defined as set of dome-addresses or address ranges and the corresponding DMX universe and pixel ranges.
 * If ranges are used, the number of dome-addresses must be equal to the number of DMX-addresses, and the addresses
 * may not cross a DMX universe barrier.
 *
 * <h4>Remapping</h4>
 * Mappings are processed in order. If there are two mappings for a single dome pixel or DMX address, the later mapping
 * will overwrite the earlier. This allows for general ranges to be defined with overrides for specific pixels or
 * DMX channels.
 *
 * <h4>Single Pixel Mapping</h4>
 *
 * A single pixel mapping should be in the form {@code mapping.N=U:C} where {@code N} is the dome-pixel address,
 * {@code U} is the DMX universe and {@code C} is the 1-based DMX channel. The pixel will consume channels C through
 * C+2.
 *
 * <h4>Pixel Range Mapping</h4>
 *
 * A pixel range mapping should be in the form {@code mapping.N-M=U:C} where {@code N} is the starting dome-pixel
 * address (inclusive), {@code M} is the ending dome-pixel address (exclusive), {@code U} is the DMX universe,
 * {@code C} is the starting DMX channel. DMX Channels C through C+(M-N)*3 will be consumed.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DmxDomeMapping {
	private String name;
	private ColorChannelOrder colorOrder;
	private int domeFrequency;
	private int litLayers;
	private Map<Integer, DmxAddress> pixelMapping = Maps.newHashMap();
	private int pixelCount = 0;

	private int firstUniverse = 0;
	private int lastUniverse = 0;
	private int universeCount;

	private int intensityMin = 0;
	private int intensityMax = 255;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Methods

	public void applyProperties(InputStream input) {
		applyProperties(new InputStreamReader(input, Charsets.UTF_8));
	}

	private void applyProperties(final Reader reader) {
		applyProperties(DomeProperties.parseProperties(reader));
	}

	public void applyProperties(LinkedHashMap<String, String> properties) {
		pixelMapping.clear();

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			if (entry.getKey().startsWith("metadata")) {
				handleMetadata(entry.getKey(), entry.getValue());
			} else if (entry.getKey().startsWith("mapping")) {
				handleMapping(entry.getKey(), entry.getValue());
			}
		}

		firstUniverse = Collections.min(
			Collections2.transform(
				pixelMapping.values(),
				DmxAddress.getUniverse
			)
		);

		lastUniverse = Collections.max(
			Collections2.transform(
				pixelMapping.values(),
				DmxAddress.getUniverse
			)
		) + 1;

		universeCount = lastUniverse - firstUniverse;

		pixelCount = Collections.max(pixelMapping.keySet());

		for (int i=0; i<pixelCount; i++) {
			System.out.println(i + ": " + pixelMapping.get(i));
		}
	}

	public int[][] allocateBuffer() {
		return new int[universeCount][512];
	}

	public int[][] map(byte[] domePixelData, int[][] dmxData) {
		for (int[] universeBuffer : dmxData) {
			for (int i=0; i<universeBuffer.length; i++) {
				universeBuffer[i] = 0;
			}
		}

		for (int dataIndex=0, pixelIndex=0; dataIndex<domePixelData.length; dataIndex+=3, pixelIndex++) {
			if (pixelMapping.containsKey(pixelIndex)) {
				DmxAddress dmxAddress = pixelMapping.get(pixelIndex);

				colorOrder.mapFromRgb(
					domePixelData,
					dataIndex,

					dmxData[dmxAddress.universe-firstUniverse],
					dmxAddress.channel-1, // DMX is 1-based,

					intensityMin,
					intensityMax
				);
			} else {
				// Oh well... we don't have a mapping for this pixel.
			}
		}

		return dmxData;
	}

	//endregion


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Internal Methods

	private void handleMetadata(String key, String value) {
		if (key.equals("metadata.shortName")) {
			this.name = value;
		} else if (key.equals("metadata.dome.frequency")) {
			this.domeFrequency = Integer.parseInt(value);
		} else if (key.equals("metadata.dome.litLayers")) {
			this.litLayers = Integer.parseInt(value);
		} else if (key.equals("metadata.hardware.colorOrder")) {
			this.colorOrder = ColorChannelOrder.valueOf(value.toUpperCase());
		} else if (key.equals("metadata.hardware.intensityMin")) {
			this.intensityMin = Integer.parseInt(value);
		} else if (key.equals("metadata.hardware.intensityMax")) {
			this.intensityMax = Integer.parseInt(value);
		}
	}

	private void handleMapping(String key, String value) {
		DmxAddress dmxAddress = DmxAddress.parse(value);

		String pixelKey = key.substring("mapping.".length());

		// Is this a range mapping?
		if (pixelKey.contains("-")) {
			String[] parts = pixelKey.split("-");
			int startPixel = Integer.parseInt(parts[0]);
			int endPixel = Integer.parseInt(parts[1]);

			for (int i=startPixel; i<endPixel; i++) {
				pixelMapping.put(i, dmxAddress);
				dmxAddress = dmxAddress.next();
			}
		} else {
			pixelMapping.put(Integer.parseInt(pixelKey), dmxAddress);
		}
	}

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Utility Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	@Override
	public String toString() {
		return "DmxDomeMapping{" +
			"pixelMapping=" + pixelMapping +
			'}';
	}

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	public int getFirstUniverse() {
		return firstUniverse;
	}

	public int getLastUniverse() {
		return lastUniverse;
	}

	public int getUniverseCount() {
		return universeCount;
	}


	//endregion

	protected static class DmxAddress {
		public static Function<DmxAddress, Integer> getUniverse = new Function<DmxAddress, Integer>() {
			public Integer apply(final DmxAddress input) {
				return input.universe;
			}
		};

		private int universe;
		private int channel;

		public DmxAddress(final int universe, final int channel) {
			Preconditions.checkArgument(channel > 0 && channel < 510, "Channel (" + channel + ") must be between 1 and 510 inclusive.");

			this.universe = universe;
			this.channel = channel;
		}

		public int getUniverse() {
			return universe;
		}

		public int getChannel() {
			return channel;
		}

		public static DmxAddress parse(final String value) {
			String[] valueParts = value.split(":");
			int universe = Integer.parseInt(valueParts[0]);
			int startingChannel = Integer.parseInt(valueParts[1]);

			return new DmxAddress(universe, startingChannel);
		}

		public DmxAddress next() {
			return new DmxAddress(universe, channel + 3);
		}

		@Override
		public String toString() {
			return universe + ":" + channel;
		}
	}

	protected enum ColorChannelOrder {
		RGB(0,1,2),
		RBG(0,2,1),
		GRB(1,0,2),
		GBR(1,2,0),
		BGR(2,1,0),
		BRG(2,0,1);

		int greenOffset, redOffset, blueOffset;

		private ColorChannelOrder(final int greenOffset, final int redOffset, final int blueOffset) {
			this.greenOffset = greenOffset;
			this.redOffset = redOffset;
			this.blueOffset = blueOffset;
		}

		public void mapFromRgb(
			byte[] rgbInput,
			int rgbIndex,
			int[] output,
			int outputIndex,
			int intensityMin,
			int intensityMax
		) {
			output[outputIndex+redOffset] =
				(int) (intensityMin + ((rgbInput[rgbIndex] & 0xFF)/255d) * (intensityMax-intensityMin));
			output[outputIndex+greenOffset] =
				(int) (intensityMin + ((rgbInput[rgbIndex+1] & 0xFF)/255d) * (intensityMax-intensityMin));
			output[outputIndex+blueOffset] =
				(int) (intensityMin + ((rgbInput[rgbIndex+2] & 0xFF) / 255d) * (intensityMax-intensityMin));
		}
	}
}
