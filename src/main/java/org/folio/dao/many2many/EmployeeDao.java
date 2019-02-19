package org.folio.dao.many2many;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Employee;

import java.util.Optional;

public interface EmployeeDao {
  public static final String TABLE = "employees";

  Future<String> save(Employee entity);
}
