package org.folio.dao.many2many;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Location;

public interface LocationDao {
  String TABLE = "locations";

  Future<String> save(Location entity);
}
