package org.folio.dao.one2one.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.folio.dao.one2one.PassportDao;
import org.folio.rest.jaxrs.model.Passport;
import org.folio.rest.persist.PostgresClient;

public class PassportDaoImpl implements PassportDao {

  private PostgresClient pgClient;

  public PassportDaoImpl(Vertx vertx, String tenantId) {
    pgClient = PostgresClient.getInstance(vertx, tenantId);
  }

  @Override
  public Future<String> save(Passport entity) {
    Future<String> future = Future.future();
    pgClient.save(TABLE, entity.getId(), entity, future.completer());
    return future;
  }
}
