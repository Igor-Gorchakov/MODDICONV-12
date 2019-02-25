package org.folio.dao.one2one;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Person;

import java.util.Optional;

public interface PersonDao {
  String TABLE = "persons";

  Future<Optional<Person>> getById(String id);

  Future<String> save(Person entity);
}
