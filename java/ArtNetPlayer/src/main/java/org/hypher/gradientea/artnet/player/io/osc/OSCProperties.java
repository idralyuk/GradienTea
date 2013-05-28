package org.hypher.gradientea.artnet.player.io.osc;

import com.google.common.collect.Maps;
import org.hypher.gradientea.artnet.player.io.DomeProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Holds a collection of configurable properties which are both persistent between program executions and can be
 * modified via OSC. An instance of this class should be created for each grouping of properties.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class OSCProperties {
	private final static long SAVE_INTERVAL_MS = 5000;

	private long lastSave = System.currentTimeMillis();
	private Map<String, String> propertyMap = Collections.synchronizedMap(Maps.<String, String>newLinkedHashMap());
	private File file;

	public OSCProperties(final File file) {
		this.file = file;

		load();
	}

	protected void load() {
		propertyMap.clear();

		if (file.exists()) {
			try {
				propertyMap.putAll(DomeProperties.parseProperties(new FileReader(file)));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void save() {
		if (System.currentTimeMillis() - lastSave > SAVE_INTERVAL_MS) {
			lastSave = System.currentTimeMillis();

			synchronized (propertyMap) {
				try {
					DomeProperties.writeProperties(propertyMap, new FileWriter(file));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
