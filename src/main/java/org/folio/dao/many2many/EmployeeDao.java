package org.folio.dao.many2many;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Employee;

public interface EmployeeDao {
  String TABLE = "employees";

  Future<String> save(Employee entity);
}
