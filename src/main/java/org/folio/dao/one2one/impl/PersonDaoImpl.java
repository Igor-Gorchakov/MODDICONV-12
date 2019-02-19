package org.folio.dao.one2one.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.folio.dao.one2one.PersonDao;
import org.folio.rest.jaxrs.model.Person;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.interfaces.Results;

import java.util.Optional;

import static org.folio.dataimport.util.DaoUtil.constructCriteria;

public class PersonDaoImpl implements PersonDao {

  private static final String ID_FIELD = "'id'";
  private PostgresClient pgClient;

  public PersonDaoImpl(Vertx vertx, String tenantId) {
    pgClient = PostgresClient.getInstance(vertx, tenantId);
  }

  @Override
  public Future<Optional<Person>> getById(String id) {
    Future<Results<Person>> future = Future.future();
    try {
      Criteria idCrit = constructCriteria(ID_FIELD, id);
      pgClient.get(TABLE, Person.class, new Criterion(idCrit), true, future.completer());
    } catch (Exception e) {
      future.fail(e);
    }
    return future
      .map(Results::getResults)
      .map(entities -> entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0)));
  }

  @Override
  public Future<String> save(Person entity) {
    Future<String> future = Future.future();
    pgClient.save(TABLE, entity.getId(), entity, future.completer());
    return future;
  }
}
