package se.thced;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class fetches the configuration
 *
 * @author thced
 */
public final class ConfigurationRetriever {

  private static final Logger log = LoggerFactory.getLogger(ConfigurationRetriever.class);

  public static final String CONFIG_TYPE_FILE = "file";
  public static final String CONFIG_TYPE_ENV = "env";
  public static final String CONFIG_FORMAT_PROPERTIES = "properties";
  public static final String CONFIG_FILE_PATH = "path";

  /** '0' means no scanning */
  private static final int SCAN_PERIOD = 0;

  private ConfigurationRetriever() {
    // No instantiation
  }

  /**
   * Populate the application configuration
   *
   * @param vertx The vertx instance is needed for filesystem access
   * @return The future containing the configuration, or a failure
   */
  public static Future<JsonObject> configuration(Vertx vertx) {
    return ConfigRetriever
        .create(vertx, getConfigRetrieverOptions())
        .getConfig()
        .onSuccess(config -> log.debug("Configuration: {}", config.encodePrettily()));
  }

  private static ConfigRetrieverOptions getConfigRetrieverOptions() {
    final ConfigRetrieverOptions options = new ConfigRetrieverOptions();

    // -- Local values : exhaustive list with sane defaults
    options.addStore(
        new ConfigStoreOptions()
            .setType(CONFIG_TYPE_FILE)
            .setFormat(CONFIG_FORMAT_PROPERTIES)
            .setConfig(new JsonObject().put(CONFIG_FILE_PATH, "conf/config.properties")));

    // -- Container / PaaS friendly to override defaults
    JsonArray whiteListedKeys = new JsonArray();
    for (ConfigurationKeys key : ConfigurationKeys.values()) {
      whiteListedKeys.add(key.name());
    }
    if (!whiteListedKeys.isEmpty()) {
      options.addStore(
          new ConfigStoreOptions()
              .setType(CONFIG_TYPE_ENV)
              .setConfig(new JsonObject().put("keys", whiteListedKeys))
              .setOptional(true));
    }

    return options.setScanPeriod(SCAN_PERIOD);
  }
}
