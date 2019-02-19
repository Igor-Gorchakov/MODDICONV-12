package org.folio.dao.many2many.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.folio.dao.many2many.TicketDao;
import org.folio.rest.jaxrs.model.Ticket;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.interfaces.Results;

import java.util.Optional;

import static org.folio.dataimport.util.DaoUtil.constructCriteria;

public class TicketDaoImpl implements TicketDao {

  private static final String ID_FIELD = "'id'";
  private PostgresClient pgClient;

  public TicketDaoImpl(Vertx vertx, String tenantId) {
    pgClient = PostgresClient.getInstance(vertx, tenantId);
  }

  @Override
  public Future<Optional<Ticket>> getById(String id) {
    Future<Results<Ticket>> future = Future.future();
    try {
      Criteria idCrit = constructCriteria(ID_FIELD, id);
      pgClient.get(TABLE, Ticket.class, new Criterion(idCrit), true, future.completer());
    } catch (Exception e) {
      future.fail(e);
    }
    return future
      .map(Results::getResults)
      .map(entities -> entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0)));
  }

  @Override
  public Future<String> save(Ticket ticket) {
    Future<String> future = Future.future();
    pgClient.save(TABLE, ticket.getId(), ticket, future.completer());
    return future;
  }
}
