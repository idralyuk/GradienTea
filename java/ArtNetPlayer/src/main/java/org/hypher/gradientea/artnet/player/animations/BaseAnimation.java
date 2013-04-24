package org.hypher.gradientea.artnet.player.animations;

import com.google.common.collect.Maps;
import org.hypher.gradientea.artnet.player.ArtNetAnimationPlayer;
import org.hypher.gradientea.artnet.player.animations.canvas.PixelCanvas;
import org.hypher.gradientea.artnet.player.animations.canvas.PixelCompositor;
import org.hypher.gradientea.artnet.player.animations.params.AnimationParameter;
import org.hypher.gradientea.artnet.player.animations.params.ConfigurableAnimation;
import org.hypher.gradientea.animation.shared.function.DefinedAnimation;
import org.hypher.gradientea.animation.shared.pixel.PixelValue;
import org.hypher.gradientea.animation.shared.RenderableAnimation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class BaseAnimation implements ConfigurableAnimation {
	private final AnimationContext context;
	private final HashMap<ParameterId, Parameter> parameters = Maps.newLinkedHashMap();
	private final PixelCanvas pixelCanvas;

	protected double animationDuration;

	public BaseAnimation(final AnimationContext context, final PixelCompositor compositor) {
		this.context = context;
		this.pixelCanvas = new PixelCanvas(context.getPixelCount(), compositor);

		addParameter(SpeedParam, 4, 24);
	}

	@Override
	public void init() {
		for (ParameterId id : parameters.keySet()) {
			onParameterChanged(id, parameters.get(id).getValue());
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Protected Methods

	protected void addParameter(ParameterId id, Parameter param) {
		parameters.put(id, param);
	}

	protected void addParameter(ParameterId id, int defaultValue, int maxValue) {
		addParameter(id, new Parameter(id, defaultValue, maxValue));
	}

	protected int getParamValue(ParameterId id) {
		return parameters.get(id).getValue();
	}

	protected double getFractionalParamValue(ParameterId id) {
		return (double)parameters.get(id).getValue() / parameters.get(id).getMaxValue();
	}


	public static double rotate(double value, final double by) {
		value = value + by;
		while (value < 0) value += 1;
		while (value > 1) value -= 1;

		return value;
	}

	protected static double compress(final double value, final double by) {
		return (value % (1.0/by)) * by;
	}

	protected int pixelCount() {
		return context.getPixelCount();
	}

	protected void onParameterChanged(ParameterId id, int newValue) {
		if (id == SpeedParam) {
			animationDuration = 1000 + (1-getFractionalParamValue(id))*10000;
		}
	}

	protected double pixelFraction(final int index) {
		return (double)index / pixelCount();
	}

	protected String pad(double v, int zeroPadding, int decimals) {
		String s = String.valueOf(v);

		int dot = s.indexOf('.');
		s = s.substring(0, Math.min(dot + decimals + 1, s.length()));

		for (int i=0; i<decimals - dot + 1; i ++) {
			s = "0" + s;
		}

		return s;
	}

	//endregion


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Abstract Methods
	protected abstract void draw(PixelCanvas canvas, double fraction);
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation
	@Override
	public void play(final ArtNetAnimationPlayer player) {
		player.playAnimations(Collections.<RenderableAnimation>singletonList(new RenderableAnimation(
			new DefinedAnimation() {
				@Override
				public List<PixelValue> render(final double fraction) {
					draw(pixelCanvas, (System.currentTimeMillis() % animationDuration) / animationDuration);
					return pixelCanvas.render();
				}
			},
			// TODO: Configure this
			10
		)));
	}

	@Override
	public void stop() {}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods
	@Override
	public Collection<? extends AnimationParameter> getParameters() {
		return parameters.values();
	}
	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion

	public class Parameter implements AnimationParameter {
		private ParameterId id;
		private int value;
		private int maxValue;

		public Parameter(final ParameterId id, final int value, final int maxValue) {
			this.id = id;
			this.value = value;
			this.maxValue = maxValue;
		}

		@Override
		public String getName() {
			return id.name();
		}

		@Override
		public int getMaxValue() {
			return maxValue;
		}

		@Override
		public void setValue(final int value) {
			this.value = value;
			onParameterChanged(id, value);
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * Interface for enumeration of ids
	 */
	public interface ParameterId {
		String name();
	}

	protected ParameterId SpeedParam = new ParameterId() {
		@Override
		public String name() {
			return "Speed";
		}
	};
}
