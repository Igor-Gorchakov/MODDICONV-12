package org.folio.dao.many2many;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Location;

import java.util.Optional;

public interface LocationDao {
  public static final String TABLE = "locations";

  Future<String> save(Location entity);
}
