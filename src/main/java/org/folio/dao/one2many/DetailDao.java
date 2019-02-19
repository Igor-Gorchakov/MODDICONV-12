package org.folio.dao.one2many;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Detail;

import java.util.Optional;

public interface DetailDao {
  public static final String TABLE = "details";

  Future<Optional<Detail>> getById(String id);

  Future<String> save(Detail entity);
}
