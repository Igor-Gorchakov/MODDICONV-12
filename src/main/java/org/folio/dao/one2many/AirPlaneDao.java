package org.folio.dao.one2many;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.AirPlane;

public interface AirPlaneDao {
  String TABLE = "airplanes";

  Future<String> save(AirPlane entity);
}
