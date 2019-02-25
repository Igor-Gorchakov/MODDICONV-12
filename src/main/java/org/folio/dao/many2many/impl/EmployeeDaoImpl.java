package org.folio.dao.many2many.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.folio.dao.many2many.EmployeeDao;
import org.folio.rest.jaxrs.model.Employee;
import org.folio.rest.persist.PostgresClient;

public class EmployeeDaoImpl implements EmployeeDao {

  private PostgresClient pgClient;

  public EmployeeDaoImpl(Vertx vertx, String tenantId) {
    pgClient = PostgresClient.getInstance(vertx, tenantId);
  }

  @Override
  public Future<String> save(Employee employee) {
    Future<String> future = Future.future();
    pgClient.save(TABLE, employee.getId(), employee, future.completer());
    return future;
  }
}
