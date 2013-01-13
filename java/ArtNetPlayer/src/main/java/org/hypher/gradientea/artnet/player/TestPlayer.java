package org.hypher.gradientea.artnet.player;

import fr.azelart.artnetstack.constants.Constants;
import org.hypher.gradientea.lightingmodel.shared.animation.ExpandedAnimationWrapper;
import org.hypher.gradientea.lightingmodel.shared.animation.HsbTween;
import org.hypher.gradientea.lightingmodel.shared.animation.SingleDefinedAnimation;
import org.hypher.gradientea.lightingmodel.shared.color.HsbColor;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxPixel;
import org.hypher.gradientea.lightingmodel.shared.rendering.RenderableAnimation;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class TestPlayer {
	public static void main(String[] args) throws IOException {
		ArtNetAnimationPlayer player = new ArtNetAnimationPlayer();
		player.start(
			InetAddress.getLocalHost(),
			InetAddress.getByName("10.0.77.7"),
			Constants.DEFAULT_ART_NET_UDP_PORT
		);


		player.playAnimations(
			Arrays.asList(
				new RenderableAnimation(
					new SingleDefinedAnimation(
						new ExpandedAnimationWrapper(
							new HsbTween(new HsbColor(0, 0.8, .5), new HsbColor(1.0, 1.0, 1.0)),
							ExpandedAnimationWrapper.SIN,
							0.15
						),
						DmxPixel.pixels(1, 9)
					),
					4
				)
			)
		);


//		player.playAnimations(Arrays.asList(
//			new RenderableAnimation(
//				AnimationScene.emptyScene()
//					.with(
//						new SingleDefinedAnimation(
//							new ExpandedAnimationWrapper(
//								new HsbTween(new HsbColor(0, 1, 0.1), new HsbColor(1.0, 1, 0.3)),
//								ExpandedAnimationWrapper.SIN
//							),
//							DmxPixel.pixels(1, 1, 5)
//						), 0, 1.0
//					)
//					.with(
//						new SingleDefinedAnimation(
//							new ExpandedAnimationWrapper(
//								new HsbTween(new HsbColor(0, 0, 0), new HsbColor(0, 1, 0)),
//								ExpandedAnimationWrapper.TRIANGLE
//							),
//							DmxPixel.pixels(1, 1, 5)
//						), 0, 1.0
//					),
//				10
//			)
//		));
	}
}
