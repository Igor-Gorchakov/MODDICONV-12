package org.folio.dao.one2many;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.AirPlane;

import java.util.Optional;

public interface AirPlaneDao {
  public static final String TABLE = "airplanes";

  Future<String> save(AirPlane entity);
}
