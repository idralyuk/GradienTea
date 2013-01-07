package org.hypher.gradientea.artnet.player;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import fr.azelart.artnetstack.listeners.ServerListener;
import fr.azelart.artnetstack.server.ArtNetServer;
import fr.azelart.artnetstack.utils.ArtNetPacketEncoder;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxPixel;
import org.hypher.gradientea.lightingmodel.shared.pixel.Pixel;
import org.hypher.gradientea.lightingmodel.shared.pixel.PixelValue;
import org.hypher.gradientea.lightingmodel.shared.rendering.RenderableAnimation;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * A standalone player for animations.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ArtNetAnimationPlayer {
	protected ArtNetServer server;
	protected Player player = new Player();

	public ArtNetAnimationPlayer() {

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	public void start(
		final InetAddress listenAddress,
		final InetAddress broadcastAddress,
		final int port
	) throws IOException {
		server = new ArtNetServer(listenAddress, broadcastAddress, port);
		server.addListenerServer(
			new ServerListener() {
				@Override
				public void onConnect() {
					Thread playerThread = new Thread(player);
					playerThread.setDaemon(true);
					playerThread.start();
				}

				@Override
				public void onTerminate() {
					player.stop();
				}
			}
		);

		server.start();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public void playAnimations(List<RenderableAnimation> animations) {
		player.setAnimations(animations);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	protected class Player implements Runnable {
		protected boolean running = true;
		protected double fps = 45;

		List<RenderableAnimation> animations = Lists.newArrayList();
		protected volatile boolean animationsUpdated = true;

		@Override
		public void run() {
			ImmutableList<RenderableAnimation> animationsSnapshot = ImmutableList.copyOf(animations);
			int animationIndex = 0;
			long currentAnimationStartTime = 0;
			animationsUpdated = true;

			while (running) {
				if (animationsUpdated) {
					animationsSnapshot = ImmutableList.copyOf(animations);
					animationsUpdated = false;

					animationIndex = 0;
					currentAnimationStartTime = System.currentTimeMillis();
				}

				if (! animationsSnapshot.isEmpty()) {
					RenderableAnimation currentAnimation = animationsSnapshot.get(animationIndex);

					double progress = (double)(System.currentTimeMillis() - currentAnimationStartTime) / (currentAnimation.getSuggestedDurationSeconds()*1000);

					if (progress > 1.0) {
						animationIndex ++;
						if (animationIndex >= animationsSnapshot.size()) {
							animationIndex = 0;
							currentAnimationStartTime = System.currentTimeMillis();
						}
						continue;
					} else {
						display(currentAnimation.getAnimation().render(progress));
					}
				}

				try {
					Thread.sleep((long) (1000 / fps));
				} catch (InterruptedException e) {}
			}
		}

		public void stop() {
			running = false;
		}

		public void setAnimations(final List<RenderableAnimation> animations) {
			this.animations = animations;
			animationsUpdated = true;
		}
	}

	protected void display(final List<PixelValue> pixelValues) {
		Multimap<Pixel, PixelValue> pixelValueMap = ArrayListMultimap.create();

		for (PixelValue pixelValue : pixelValues) {
			pixelValueMap.put(pixelValue.getPixel(), pixelValue);
		}

		int[][] universes = new int[4][512];

		for (Pixel pixel : pixelValueMap.keySet()) {
			if (pixel instanceof DmxPixel) {
				DmxPixel dmxPixel = (DmxPixel) pixel;

				int redSum = 0;
				int greenSum = 0;
				int blueSum = 0;
				int count = pixelValueMap.get(pixel).size();

				for (PixelValue value : pixelValueMap.get(pixel)) {
					int[] rgb = value.getColor().asRgb();

					redSum += rgb[0];
					greenSum += rgb[1];
					blueSum += rgb[2];
				}

				int[] universe = universes[dmxPixel.getUniverse() - 1];
				int startChannel = dmxPixel.getFirstChannel()-1;

				universe[startChannel+0] = (int) Math.round((double)redSum/count);
				universe[startChannel+1] = (int) Math.round((double)greenSum/count);
				universe[startChannel+2] = (int) Math.round((double)greenSum/count);
			}
		}

		for (int i=0; i<universes.length; i++) {
			if (universes[i] != null) {
				try {
					server.sendPacket(ArtNetPacketEncoder.encodeArtDmxPacket(
						i+1, 0, universes[i]
					));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
