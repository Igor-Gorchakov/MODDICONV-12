package org.folio.dao.one2one;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Passport;

public interface PassportDao {
  public static final String TABLE = "passports";

  Future<String> save(Passport entity);
}
