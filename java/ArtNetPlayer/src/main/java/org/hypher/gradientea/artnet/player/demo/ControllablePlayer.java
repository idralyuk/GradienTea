package org.hypher.gradientea.artnet.player.demo;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pi4j.io.gpio.RaspiPin;
import fr.azelart.artnetstack.constants.Constants;
import org.hypher.gradientea.artnet.player.demo.animations.AnimationContext;
import org.hypher.gradientea.artnet.player.demo.animations.FadingAudioStrobe;
import org.hypher.gradientea.artnet.player.demo.animations.FlashyAudioStrobe;
import org.hypher.gradientea.artnet.player.demo.animations.LinearSpectrumFader;
import org.hypher.gradientea.artnet.player.demo.animations.OmniRainbowAnimation;
import org.hypher.gradientea.artnet.player.demo.animations.params.AnimationParameter;
import org.hypher.gradientea.artnet.player.demo.animations.params.ConfigurableAnimation;
import org.hypher.gradientea.artnet.player.demo.io.RotarySwitchReader;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ControllablePlayer {
	public static final int PIXEL_COUNT = 50;
	public static final int CONFIG_TIMEOUT_MS = 2000;
	public static final int DOUBLE_PRESS_MS = 200;

	private final ArtNetAnimationPlayer player;
	private final AnimationContext context;
	private final List<ConfigurableAnimation> animations = Lists.newArrayList();

	private int currentAnimationIndex = -1;

	public static void main(String[] args) throws IOException {
		new ControllablePlayer(args.length > 0 ? args[0] : "127.255.255.255");
	}

	public ControllablePlayer(String hostname) throws IOException {
		player = new ArtNetAnimationPlayer();

		player.start(
			InetAddress.getLocalHost(),
			InetAddress.getByName(hostname),
			Constants.DEFAULT_ART_NET_UDP_PORT
		);

		context = new AnimationContext(PIXEL_COUNT);

		setupAnimations();

		new KeyboardRotaryEmulator(
			'q', 'w', ' ',
			new AnimationConfigurer()
		);

		try {
			new RotarySwitchReader(
				RaspiPin.GPIO_02, // Marked 17
				RaspiPin.GPIO_00, // Marked 27
				RaspiPin.GPIO_03, // Marked 22
				new AnimationConfigurer()
			);
		} catch (Throwable t) {
			System.out.println("Couldn't start GPIO switch reader: " + t.getClass().getSimpleName() + ": " + t.getMessage());
		}

		for (ConfigurableAnimation animation : animations) {
			animation.init();
		}

		playAnimation(0);
	}

	private void setupAnimations() {
		animations.add(new OmniRainbowAnimation(context));
		animations.add(new FadingAudioStrobe(context));
		animations.add(new FlashyAudioStrobe(context));
		animations.add(new LinearSpectrumFader(context));

		//animations.add(new MovingDotAnimation(context));
		//animations.add(new RandomBlipsAnimation(context));
	}

	private class AnimationConfigurer implements RotarySwitchReader.RotarySwitchCallback {
		int level = 0;
		long lastEventTime = 0;
		AnimationParameter currentParam;

		@Override
		public void onScrollLeft() {
			if (level == 0) {
				playAnimation(currentAnimationIndex - 1);
			} else {
				int newValue = Math.max(0, currentParam.getValue() - 1);
				if (currentParam.getValue() != newValue) {
					System.out.println("Setting " + currentParam.getName() + " to " + newValue);
					currentParam.setValue(newValue);
				}
			}
		}

		@Override
		public void onScrollRight() {
			if (level == 0) {
				playAnimation(currentAnimationIndex + 1);
			} else {

				int newValue = Math.min(currentParam.getMaxValue(), currentParam.getValue() + 1);
				if (currentParam.getValue() != newValue) {
					System.out.println("Setting " + currentParam.getName() + " to " + newValue);
					currentParam.setValue(newValue);
				}
			}
		}

		@Override
		public void onButtonPushed() {
			level ++;
			if (level > currentAnimation().getParameters().size() || (System.currentTimeMillis() - lastEventTime) < DOUBLE_PRESS_MS) {
				level = 0;
			}

			lastEventTime = System.currentTimeMillis();


			if (level > 0) {
				currentParam = Iterables.get(currentAnimation().getParameters(), level-1);
				System.out.println("Editing " + currentParam.getName());
			} else {
				System.out.println("Animation Selection Mode");
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Protected Methods

	public ConfigurableAnimation currentAnimation() {
		return animations.get(currentAnimationIndex);
	}

	public void playAnimation(int index) {
		index = index % animations.size();
		if (index < 0) index = animations.size() + index;

		if (currentAnimationIndex != index) {
			if (currentAnimationIndex >= 0) {
				currentAnimation().stop();
			}

			currentAnimationIndex = index;
			System.out.println("Playing " + currentAnimation().getClass().getSimpleName());
			currentAnimation().play(player);
		}
	}

	private class KeyboardRotaryEmulator {
		public KeyboardRotaryEmulator(
			final char leftChar,
			final char rightChar,
			final char clickChar,
			final RotarySwitchReader.RotarySwitchCallback animationConfigurer
		) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							char c = (char) System.in.read();

							if (c == leftChar) animationConfigurer.onScrollLeft();
							else if (c == rightChar) animationConfigurer.onScrollRight();
							else if (c == clickChar) animationConfigurer.onButtonPushed();
						} catch (IOException e) {

						}
					}
				}
			});

			t.setDaemon(true);
			t.start();
		}
	}

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
