package org.hypher.gradientea.artnet.player.io;

import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility methods for reading and writing order-aware properties from a properties file/
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeProperties {
	private DomeProperties() {}

	public static void writeProperties(Map<String, String> properties, final Writer writer) {
		PrintWriter printWriter = new PrintWriter(writer);

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			printWriter.println(entry.getKey() + "=" + entry.getValue());
		}

		printWriter.flush();
	}

	public static LinkedHashMap<String, String> parseProperties(final Reader reader) {
		LinkedHashMap<String, String> properties = Maps.newLinkedHashMap();

		BufferedReader bufferedReader = new BufferedReader(reader);

		try {
			for (String line; (line = bufferedReader.readLine()) != null;) {
				// Remove whitespace
				line = line.trim();

				// Remove comments
				if (line.contains("#")) {
					line = line.substring(0, line.indexOf('#'));
				}

				// Remove whitespace again
				line = line.trim();

				if (! line.isEmpty() && line.contains("=")) {
					int equals = line.indexOf("=");
					String key = line.substring(0,equals).trim();
					String value = line.substring(equals+1).trim();

					properties.put(key, value);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return properties;
	}
}
