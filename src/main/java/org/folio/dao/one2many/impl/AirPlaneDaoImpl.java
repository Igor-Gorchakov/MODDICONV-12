package org.folio.dao.one2many.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.folio.dao.one2many.AirPlaneDao;
import org.folio.rest.jaxrs.model.AirPlane;
import org.folio.rest.persist.PostgresClient;

public class AirPlaneDaoImpl implements AirPlaneDao {

  private PostgresClient pgClient;

  public AirPlaneDaoImpl(Vertx vertx, String tenantId) {
    pgClient = PostgresClient.getInstance(vertx, tenantId);
  }

  @Override
  public Future<String> save(AirPlane airplane) {
    Future<String> future = Future.future();
    pgClient.save(TABLE, airplane.getId(), airplane, future.completer());
    return future;
  }
}
