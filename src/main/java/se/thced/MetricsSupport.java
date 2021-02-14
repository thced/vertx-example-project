package se.thced;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Slf4jReporter;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import java.util.concurrent.TimeUnit;

/** @author thced */
public final class MetricsSupport {

  private MetricsSupport() {
    // We dont instantiate this
  }

  // -- Metrics constants
  public static final String DEFAULT_REGISTRY_NAME = "registry";

  public static void addMetricsConfiguration(VertxOptions vertxOptions) {
    MetricRegistry registry = SharedMetricRegistries.getOrCreate(DEFAULT_REGISTRY_NAME);
    SharedMetricRegistries.setDefault(DEFAULT_REGISTRY_NAME);

    Slf4jReporter.forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build()
        .start(30, TimeUnit.SECONDS);

    DropwizardMetricsOptions metricsOptions =
        new DropwizardMetricsOptions()
            .setEnabled(true)
            .setMetricRegistry(SharedMetricRegistries.getOrCreate(DEFAULT_REGISTRY_NAME));

    vertxOptions.setMetricsOptions(metricsOptions);
  }
}
