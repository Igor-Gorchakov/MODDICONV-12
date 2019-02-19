package org.folio.dao.many2many.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.folio.dao.many2many.LocationDao;
import org.folio.rest.jaxrs.model.Location;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.interfaces.Results;

import java.util.Optional;

import static org.folio.dataimport.util.DaoUtil.constructCriteria;

public class LocationDaoImpl implements LocationDao {

  private PostgresClient pgClient;

  public LocationDaoImpl(Vertx vertx, String tenantId) {
    pgClient = PostgresClient.getInstance(vertx, tenantId);
  }

  @Override
  public Future<String> save(Location location) {
    Future<String> future = Future.future();
    pgClient.save(TABLE, location.getId(), location, future.completer());
    return future;
  }
}
