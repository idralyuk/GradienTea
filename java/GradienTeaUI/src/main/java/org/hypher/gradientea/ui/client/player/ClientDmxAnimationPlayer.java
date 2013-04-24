package org.hypher.gradientea.ui.client.player;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.core.client.Duration;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxPixel;
import org.hypher.gradientea.lightingmodel.shared.dmx.DmxRendering;
import org.hypher.gradientea.animation.shared.RenderableAnimation;

/**
 * Class which handles playing a {@link RenderableAnimation} consisting of {@link DmxPixel}s.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ClientDmxAnimationPlayer {

	protected RenderableAnimation animation;
	protected DmxInterface dmxInterface;

	protected boolean playing = false;
	protected double startTime;

	public ClientDmxAnimationPlayer(final DmxInterface dmxInterface) {
		this.dmxInterface = dmxInterface;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	public void play(final RenderableAnimation animation) {
		this.animation = animation;
		this.startTime = Duration.currentTimeMillis();

		start();
	}

	protected void start() {
		if (playing) return;

		playing = true;

		AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
			@Override
			public void execute(final double timestamp) {
			if (! playing) return;

			double progress = ((Duration.currentTimeMillis() - startTime) / (animation.getSuggestedDurationSeconds()*1000)) % 1.0;

			render(progress);

			AnimationScheduler.get().requestAnimationFrame(this);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utility Methods

	public void stop() {
		this.playing = true;
	}

	protected void render(double progress) {
		dmxInterface.display(DmxRendering.render(animation.getAnimation(), progress));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters


	public DmxInterface getDmxInterface() {
		return dmxInterface;
	}

	public RenderableAnimation getAnimation() {
		return animation;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
