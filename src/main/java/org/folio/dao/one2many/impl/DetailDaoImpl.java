package org.folio.dao.one2many.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.folio.dao.one2many.DetailDao;
import org.folio.rest.jaxrs.model.Detail;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.interfaces.Results;

import java.util.Optional;

import static org.folio.dataimport.util.DaoUtil.constructCriteria;

public class DetailDaoImpl implements DetailDao {

  private static final String ID_FIELD = "'id'";
  private PostgresClient pgClient;

  public DetailDaoImpl(Vertx vertx, String tenantId) {
    pgClient = PostgresClient.getInstance(vertx, tenantId);
  }

  @Override
  public Future<Optional<Detail>> getById(String id) {
    Future<Results<Detail>> future = Future.future();
    try {
      Criteria idCrit = constructCriteria(ID_FIELD, id);
      pgClient.get(TABLE, Detail.class, new Criterion(idCrit), true, future.completer());
    } catch (Exception e) {
      future.fail(e);
    }
    return future
      .map(Results::getResults)
      .map(entities -> entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0)));
  }

  @Override
  public Future<String> save(Detail detail) {
    Future<String> future = Future.future();
    pgClient.save(TABLE, detail.getId(), detail, future.completer());
    return future;
  }
}
