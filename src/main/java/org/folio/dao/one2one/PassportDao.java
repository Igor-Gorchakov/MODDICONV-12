package org.folio.dao.one2one;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Passport;

import java.util.Optional;

public interface PassportDao {
  String TABLE = "passports";

  Future<Optional<Passport>> getById(String id);

  Future<String> save(Passport entity);
}
