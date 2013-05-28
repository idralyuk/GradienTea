package org.hypher.gradientea.artnet.player.linear;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.azelart.artnetstack.constants.Constants;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GeoVector3;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpec;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxPixel;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class VertexPlaying {
	public static void main(String[] args) throws IOException, InterruptedException {
		final ArtNetAnimationPlayer player = new ArtNetAnimationPlayer();

		GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(
			new GradienTeaDomeSpec(
				2, // Frequency
				3, // Layers
				2, // Lighted Layers
				9, // Radius (ft)
				2, // Panel Height (ft)
				8 /*mm*/ / 25.4 / 12 // Panel thickness (ft)
			)
		);

		final Set<GeoVector3> litVertices = Sets.newHashSet(FluentIterable.from(geometry.getLightedFaces()).transformAndConcat(
			new Function<GeoFace, Iterable<GeoVector3>>() {
				@Nullable
				@Override
				public Iterable<GeoVector3> apply(@Nullable final GeoFace input) {
					return input.getVertices();
				}
			}
		));

		System.out.println("Lit Vertices: " + litVertices.size());

		player.start(
			InetAddress.getLocalHost(),
			InetAddress.getByName("255.255.255.255"),
			Constants.DEFAULT_ART_NET_UDP_PORT
		);

		int frameCount = 0;
		while (true) {
			frameCount ++;

			List<PixelValue> values = Lists.newArrayList();
			for (int i=0; i<4; i++) {
				values.add(new PixelValue(
					new DmxPixel(1, 64 + i*3),
					new HsbColor(
						(frameCount % 100) / 100.0,
						1.0,
						1.0
					)
				));
			}

			player.display(values);

			Thread.sleep(33);
		}
	}
}
