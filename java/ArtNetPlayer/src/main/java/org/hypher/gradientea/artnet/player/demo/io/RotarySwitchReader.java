package org.hypher.gradientea.artnet.player.demo.io;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RotarySwitchReader {
	private final Pin scrollClockPin;
	private final Pin scrollDataPin;
	private final Pin scrollButtonPin;
	private final RotarySwitchCallback callback;

	public RotarySwitchReader(
		final Pin scrollClockPin,
		final Pin scrollDataPin,
		final Pin scrollButtonPin,
		final RotarySwitchCallback callback
	) {
		this.scrollClockPin = scrollClockPin;
		this.scrollDataPin = scrollDataPin;
		this.scrollButtonPin = scrollButtonPin;
		this.callback = callback;

		init();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Protected Methods

	private void init() {
		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalInput pinScrollClock = gpio.provisionDigitalInputPin(scrollClockPin);
		final GpioPinDigitalInput pinScrollData = gpio.provisionDigitalInputPin(scrollDataPin);

		GpioPinDigitalInput pinScrollPush = gpio.provisionDigitalInputPin(scrollButtonPin);

		pinScrollClock.setPullResistance(PinPullResistance.PULL_DOWN);
		pinScrollData.setPullResistance(PinPullResistance.PULL_DOWN);

		pinScrollClock.addListener(
			new GpioPinListenerDigital() {
				@Override
				public void handleGpioPinDigitalStateChangeEvent(final GpioPinDigitalStateChangeEvent event) {
					if (event.getState().isHigh()) {
						if (pinScrollData.getState().isHigh()) {
							callback.onScrollLeft();
						} else {
							callback.onScrollRight();
						}
					}
				}
			}
		);

		pinScrollPush.setPullResistance(PinPullResistance.PULL_DOWN);
		pinScrollPush.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(final GpioPinDigitalStateChangeEvent event) {
				if (event.getState().isHigh()) {
					callback.onButtonPushed();
				}
			}
		});
	}

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters



	//endregion

	public interface RotarySwitchCallback {
		void onScrollLeft();
		void onScrollRight();
		void onButtonPushed();
	}
}
