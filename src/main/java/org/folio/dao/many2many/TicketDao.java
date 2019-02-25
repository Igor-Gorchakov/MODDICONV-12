package org.folio.dao.many2many;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Ticket;

import java.util.Optional;

public interface TicketDao {
  String TABLE = "tickets";

  Future<Optional<Ticket>> getById(String id);

  Future<String> save(Ticket entity);
}
