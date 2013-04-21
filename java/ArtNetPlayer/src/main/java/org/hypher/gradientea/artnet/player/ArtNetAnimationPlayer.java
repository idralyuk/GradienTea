package org.hypher.gradientea.artnet.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import fr.azelart.artnetstack.listeners.ServerListener;
import fr.azelart.artnetstack.server.ArtNetServer;
import fr.azelart.artnetstack.utils.ArtNetPacketEncoder;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxRendering;
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

	/**
	 * Sends the given pixel values to the hardware.
	 *
	 * @param pixelValues
	 */
	public void display(final List<PixelValue> pixelValues) {
		int[][] channelData = DmxRendering.composite(pixelValues);

		boolean errored = false;
		for (int i=0; i<channelData.length; i++) {
			if (channelData[i] != null) {
				try {
					server.sendPacket(ArtNetPacketEncoder.encodeArtDmxPacket(
						i+1, 0, channelData[i]
					));
				} catch (IOException e) {
					System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
					errored = true;
					break;
				}
			}
		}

		if (errored) {
			try {
				// TODO: Come up with a less hacky method to avoid error spam when we can't send data
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				/* Do nothing */
			}
		}
	}
}
