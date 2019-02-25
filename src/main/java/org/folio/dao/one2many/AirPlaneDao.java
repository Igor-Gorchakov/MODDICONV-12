package org.folio.dao.one2many;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.AirPlane;

import java.util.Optional;

public interface AirPlaneDao {
  String TABLE = "airplanes";

  Future<Optional<AirPlane>> getById(String id);

  Future<String> save(AirPlane entity);
}
