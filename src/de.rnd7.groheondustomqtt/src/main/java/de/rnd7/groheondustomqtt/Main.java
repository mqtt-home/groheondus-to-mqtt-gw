package de.rnd7.groheondustomqtt;

import java.io.File;
import java.io.IOException;

import de.rnd7.groheondustomqtt.grohe.GroheService;
import de.rnd7.mqttgateway.GwMqttClient;
import de.rnd7.mqttgateway.config.ConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rnd7.groheondustomqtt.config.Config;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public Main(final Config config) {
        LOGGER.debug("Debug enabled");
        LOGGER.info("Info enabled");

        try {
            GwMqttClient.start(config.getMqtt()
                .setDefaultTopic("grohe"))
                .online();

            new GroheService(config.getGrohe())
                .start();

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
            new Main(ConfigParser.parse(new File(args[0]), Config.class));
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
