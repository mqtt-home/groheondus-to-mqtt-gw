package de.rnd7.groheondustomqtt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class ConfigParser {
	private static final String MESSAGE_INTERVAL = "message-interval";

	private ConfigParser() {

	}

	public static Config parse(final File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			return parse(in);
		}
	}

	public static Config parse(final InputStream in) throws IOException {
		final Config config = new Config();

		final JSONObject jsonObject = new JSONObject(IOUtils.toString(in, StandardCharsets.UTF_8));

		config.setGroheUsername(jsonObject.getString("grohe-username"));
		config.setGrohePassword(jsonObject.getString("grohe-password"));

		config.setMqttBroker(jsonObject.getString("mqtt-url"));
		config.setPollingInterval(Duration.ofSeconds(jsonObject.getInt(MESSAGE_INTERVAL)));

		return config;

	}
}
