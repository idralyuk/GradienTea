package org.hypher.gradientea.artnet.player;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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
 * address (inclusive), {@code M} is the ending dome-pixel address (inclusive), {@code U} is the DMX universe,
 * {@code C} is the starting DMX channel. DMX Channels C through C+(M-N)*3 will be consumed.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DmxDomeMapping {
	public static final String METADATA = "metadata";
	public static final String FACE_MAPPING = "faceMapping";
	public static final String VERTEX_MAPPING = "vertexMapping";
	public static final String METADATA_SHORT_NAME = "metadata.shortName";
	public static final String METADATA_ID = "metadata.id";
	public static final String METADATA_DOME_FREQUENCY = "metadata.dome.frequency";
	public static final String METADATA_DOME_LIT_LAYERS = "metadata.dome.litLayers";
	public static final String METADATA_HARDWARE_COLOR_ORDER = "metadata.hardware.colorOrder";
	public static final String METADATA_HARDWARE_INTENSITY_MIN = "metadata.hardware.intensityMin";
	public static final String METADATA_HARDWARE_INTENSITY_MAX = "metadata.hardware.intensityMax";
	public static final String METADATA_HARDWARE_PIXELS_PER_VERTEX = "metadata.hardware.pixelsPerVertex";
	public static final String METADATA_HARDWARE_VERTEX_COLOR_ORDER = "metadata.hardware.vertexColorOrder";

	private DomeIdentifier id;
	private String name;
	private ColorChannelOrder colorOrder;
	private ColorChannelOrder vertexColorOrder;

	private int domeFrequency;
	private int litLayers;
	private Map<Integer, DmxAddress> faceMapping = Maps.newHashMap();
	private Multimap<Integer, DmxAddress> vertexMapping = HashMultimap.create();
	private int pixelCount = 0;
	private int vertexCount = 0;

	private int firstUniverse = 0;
	private int lastUniverse = 0;
	private int universeCount;

	private int intensityMin = 0;
	private int intensityMax = 255;

	private int pixelsPerVertex = 0;;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Methods

	public void applyProperties(InputStream input) {
		applyProperties(new InputStreamReader(input, Charsets.UTF_8));
	}

	private void applyProperties(final Reader reader) {
		applyProperties(DomeProperties.parseProperties(reader));
	}

	public void applyProperties(LinkedHashMap<String, String> properties) {
		faceMapping.clear();

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			if (entry.getKey().startsWith(METADATA)) {
				handleMetadata(entry.getKey(), entry.getValue());
			} else if (entry.getKey().startsWith(FACE_MAPPING)) {
				handleFaceMapping(entry.getKey(), entry.getValue());
			} else if (entry.getKey().startsWith(VERTEX_MAPPING)) {
				handleVertexMapping(entry.getKey(), entry.getValue());
			}
		}

		firstUniverse = Collections.min(
			Collections2.transform(
				faceMapping.values(),
				DmxAddress.getUniverse
			)
		);

		lastUniverse = Collections.max(
			Collections2.transform(
				faceMapping.values(),
				DmxAddress.getUniverse
			)
		) + 1;

		universeCount = lastUniverse - firstUniverse;

		pixelCount = faceMapping.isEmpty() ? 0 : (Collections.max(faceMapping.keySet())+1);
		vertexCount = vertexMapping.isEmpty() ? 0 : (Collections.max(vertexMapping.keySet())+1);

		for (int i=0; i<pixelCount; i++) {
			System.out.println("Face " + i + ": " + Objects.firstNonNull(faceMapping.get(i), "NOT MAPPED"));
		}

		for (int i=0; i<vertexCount; i++) {
			System.out.println("Vertex " + i + ": " + Objects.firstNonNull(vertexMapping.get(i), "NOT MAPPED"));
		}
	}

	public int[][] allocateBuffer() {
		return new int[universeCount][512];
	}

	public int[][] map(
		byte[] facePixelData,
		byte[] vertexPixelData,
		int[][] dmxData
	) {
		for (int[] universeBuffer : dmxData) {
			for (int i=0; i<universeBuffer.length; i++) {
				universeBuffer[i] = 0;
			}
		}

		for (int dataIndex=0, faceIndex=0; dataIndex<facePixelData.length; dataIndex+=3, faceIndex++) {
			if (faceMapping.containsKey(faceIndex)) {
				DmxAddress dmxAddress = faceMapping.get(faceIndex);

				colorOrder.mapFromRgb(
					facePixelData,
					dataIndex,

					dmxData[dmxAddress.universe-firstUniverse],
					dmxAddress.channel-1, // DMX is 1-based, but java arrays aren't

					intensityMin,
					intensityMax
				);
			} else {
				// Oh well... we don't have a mapping for this pixel.
			}
		}

		for (int dataIndex=0, vertexIndex=0; dataIndex<vertexPixelData.length; dataIndex+=3, vertexIndex++) {
			if (vertexMapping.containsKey(vertexIndex)) {
				for (DmxAddress dmxAddress : vertexMapping.get(vertexIndex)) {
//				System.out.println(
//					"Vertex " + vertexIndex + ": rgb(" +
//						vertexPixelData[dataIndex] + "," +
//						vertexPixelData[dataIndex+1] + "," +
//						vertexPixelData[dataIndex+2] + "," +
//					")"
//				);

					vertexColorOrder.mapFromRgb(
						vertexPixelData,
						dataIndex,

						dmxData[dmxAddress.universe-firstUniverse],
						dmxAddress.channel-1, // DMX is 1-based, but java arrays aren't

						intensityMin,
						intensityMax
					);
				}
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
		if (key.equals(METADATA_SHORT_NAME)) {
			this.name = value;
		} else if (key.equals(METADATA_ID)) {
			this.id = DomeIdentifier.valueOf(value);
		} else if (key.equals(METADATA_DOME_FREQUENCY)) {
			this.domeFrequency = Integer.parseInt(value);
		} else if (key.equals(METADATA_DOME_LIT_LAYERS)) {
			this.litLayers = Integer.parseInt(value);
		} else if (key.equals(METADATA_HARDWARE_COLOR_ORDER)) {
			this.colorOrder = ColorChannelOrder.valueOf(value.toUpperCase());
		} else if (key.equals(METADATA_HARDWARE_VERTEX_COLOR_ORDER)) {
			this.vertexColorOrder = ColorChannelOrder.valueOf(value.toUpperCase());
		} else if (key.equals(METADATA_HARDWARE_INTENSITY_MIN)) {
			this.intensityMin = Integer.parseInt(value);
		} else if (key.equals(METADATA_HARDWARE_INTENSITY_MAX)) {
			this.intensityMax = Integer.parseInt(value);
		} else if (key.equals(METADATA_HARDWARE_PIXELS_PER_VERTEX)) {
			this.pixelsPerVertex = Integer.parseInt(value);
		}
	}

	private void handleFaceMapping(
		String key,
		String value
	) {
		DmxAddress dmxAddress = DmxAddress.parse(value);

		String pixelKey = key.substring(FACE_MAPPING.length() + 1);

		// Is this a range mapping?
		if (pixelKey.contains("-")) {
			String[] parts = pixelKey.split("-");
			int startPixel = Integer.parseInt(parts[0]);
			int endPixel = Integer.parseInt(parts[1]);

			if (startPixel < endPixel) {
				for (int i=startPixel; i<=endPixel; i++) {
					faceMapping.put(i, dmxAddress);
					dmxAddress = dmxAddress.next();
				}
			} else {
				for (int i=startPixel; i>=endPixel; i--) {
					faceMapping.put(i, dmxAddress);
					dmxAddress = dmxAddress.next();
				}
			}
		} else {
			faceMapping.put(Integer.parseInt(pixelKey), dmxAddress);
		}
	}

	private void handleVertexMapping(
		String key,
		String value
	) {
		DmxAddress dmxAddress = DmxAddress.parse(value);

		String pixelKey = key.substring(VERTEX_MAPPING.length() + 1);

		// Is this a range mapping?
		if (pixelKey.contains("-")) {
			String[] parts = pixelKey.split("-");
			int startPixel = Integer.parseInt(parts[0]);
			int endPixel = Integer.parseInt(parts[1]);

			if (startPixel < endPixel) {
				for (int i=startPixel; i<=endPixel; i++) {
					for (int p=0; p<pixelsPerVertex; p++) {
						vertexMapping.put(i, dmxAddress);
						dmxAddress = dmxAddress.next();
					}
				}
			} else {
				for (int i=startPixel; i>=endPixel; i--) {
					for (int p=0; p<pixelsPerVertex; p++) {
						vertexMapping.put(i, dmxAddress);
						dmxAddress = dmxAddress.next();
					}
				}
			}
		} else {
			vertexMapping.put(Integer.parseInt(pixelKey), dmxAddress);
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
			"faceMapping=" + faceMapping +
			'}';
	}

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	public DomeIdentifier getId() {
		return id;
	}

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
			output[outputIndex+redOffset] = scaleValue(rgbInput[rgbIndex] & 0xFF, intensityMin, intensityMax);
			output[outputIndex+greenOffset] = scaleValue(rgbInput[rgbIndex+1] & 0xFF, intensityMin, intensityMax);
			output[outputIndex+blueOffset] = scaleValue(rgbInput[rgbIndex+2] & 0xFF, intensityMin, intensityMax);
		}

		protected int scaleValue(
			int input,
			int intensityMin,
			int intensityMax
		) {
			int output = (int) (intensityMin + (input/255d) * (intensityMax-intensityMin));
			//output = (int) (Math.pow(256, output / 255) - 1);
			return output;
		}
	}

}
