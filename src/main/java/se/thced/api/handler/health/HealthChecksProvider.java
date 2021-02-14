package se.thced.api.handler.health;

import io.vertx.core.Vertx;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.spi.cluster.hazelcast.ClusterHealthCheck;

/**
 * A provider to gather all the health checks
 *
 * @author thced
 */
public final class HealthChecksProvider {

  private HealthChecksProvider() {
    // Hidden
  }

  public static HealthChecks create(Vertx vertx) {
    return HealthChecks.create(vertx)
        .register("hazelcast", ClusterHealthCheck.createProcedure(vertx));
  }

}
