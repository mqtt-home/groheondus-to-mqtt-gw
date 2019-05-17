package de.rnd7.groheondustomqtt;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import de.rnd7.groheondustomqtt.config.Config;
import de.rnd7.groheondustomqtt.config.ConfigParser;
import de.rnd7.groheondustomqtt.grohe.GroheAPI;
import de.rnd7.groheondustomqtt.grohe.GroheDevice;
import de.rnd7.groheondustomqtt.mqtt.GwMqttClient;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private final EventBus eventBus = new EventBus();

	private GroheAPI groheAPI;

	public Main(final Config config) {
		this.eventBus.register(new GwMqttClient(config));

		try {
			this.groheAPI = new GroheAPI(config.getGroheUsername(), config.getGrohePassword());

			final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
			executor.scheduleAtFixedRate(this::exec, 0, config.getPollingInterval().getSeconds(), TimeUnit.SECONDS);

			while (true) {
				this.sleep();
			}
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (final InterruptedException e) {
			LOGGER.debug(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
	}

	private void exec() {
		try {
			for (final GroheDevice device : this.groheAPI.fetchDevices()) {
				this.eventBus.post(device.toMessage());
			}
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public static void main(final String[] args) {
		if (args.length != 1) {
			LOGGER.error("Expected configuration file as argument");
			return;
		}

		try {
			new Main(ConfigParser.parse(new File(args[0])));
		} catch (final IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
