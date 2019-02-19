package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.folio.dao.many2many.EmployeeDao;
import org.folio.dao.many2many.LocationDao;
import org.folio.dao.many2many.TicketDao;
import org.folio.dao.many2many.impl.EmployeeDaoImpl;
import org.folio.dao.many2many.impl.LocationDaoImpl;
import org.folio.dao.many2many.impl.TicketDaoImpl;
import org.folio.dao.one2many.AirPlaneDao;
import org.folio.dao.one2many.DetailDao;
import org.folio.dao.one2many.impl.AirPlaneDaoImpl;
import org.folio.dao.one2many.impl.DetailDaoImpl;
import org.folio.dao.one2one.PassportDao;
import org.folio.dao.one2one.PersonDao;
import org.folio.dao.one2one.impl.PassportDaoImpl;
import org.folio.dao.one2one.impl.PersonDaoImpl;
import org.folio.dataimport.util.ExceptionHelper;
import org.folio.rest.jaxrs.model.AirPlane;
import org.folio.rest.jaxrs.model.Detail;
import org.folio.rest.jaxrs.model.Employee;
import org.folio.rest.jaxrs.model.Location;
import org.folio.rest.jaxrs.model.Passport;
import org.folio.rest.jaxrs.model.Person;
import org.folio.rest.jaxrs.model.Ticket;
import org.folio.rest.jaxrs.resource.Resource;
import org.folio.rest.tools.utils.TenantTool;

import javax.ws.rs.core.Response;
import java.util.Map;

public class ResourceImpl implements Resource {

  private EmployeeDao employeeDao;
  private LocationDao locationDao;
  private TicketDao ticketDao;
  private AirPlaneDao airPlaneDao;
  private DetailDao detailDao;
  private PassportDao passportDao;
  private PersonDao personDao;

  public ResourceImpl(Vertx vertx, String tenantId) {
    String calculatedTenantId = TenantTool.calculateTenantId(tenantId);
    this.employeeDao = new EmployeeDaoImpl(vertx, calculatedTenantId);
    this.locationDao = new LocationDaoImpl(vertx, calculatedTenantId);
    this.ticketDao = new TicketDaoImpl(vertx, calculatedTenantId);
    this.airPlaneDao = new AirPlaneDaoImpl(vertx, calculatedTenantId);
    this.detailDao = new DetailDaoImpl(vertx, calculatedTenantId);
    this.passportDao = new PassportDaoImpl(vertx, calculatedTenantId);
    this.personDao = new PersonDaoImpl(vertx, calculatedTenantId);
  }

  @Override
  public void postResourceEmployee(Employee entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext(v -> {
      try {
        employeeDao.save(entity)
          .map((Response) PostResourceEmployeeResponse
            .respond201WithApplicationJson(entity))
          .otherwise(ExceptionHelper::mapExceptionToResponse)
          .setHandler(asyncResultHandler);
      } catch (Exception e) {
        asyncResultHandler.handle(Future.succeededFuture(ExceptionHelper.mapExceptionToResponse(e)));
      }
    });
  }

  @Override
  public void postResourceLocation(Location entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    try {
      locationDao.save(entity)
        .map((Response) PostResourceLocationResponse
          .respond201WithApplicationJson(entity))
        .otherwise(ExceptionHelper::mapExceptionToResponse)
        .setHandler(asyncResultHandler);
    } catch (Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(ExceptionHelper.mapExceptionToResponse(e)));
    }
  }

  @Override
  public void postResourceTicket(Ticket entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    try {
      ticketDao.save(entity)
        .map((Response) PostResourceTicketResponse
          .respond201WithApplicationJson(entity))
        .otherwise(ExceptionHelper::mapExceptionToResponse)
        .setHandler(asyncResultHandler);
    } catch (Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(ExceptionHelper.mapExceptionToResponse(e)));
    }
  }

  @Override
  public void getResourceTicketById(String id, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext(c -> {
      try {
        ticketDao.getById(id)
          .map(ar -> (Response) GetResourceTicketByIdResponse.respond200WithApplicationJson(ar.get()))
          .otherwise(ExceptionHelper::mapExceptionToResponse)
          .setHandler(asyncResultHandler);
      } catch (Exception e) {
        asyncResultHandler.handle(Future.succeededFuture(
          ExceptionHelper.mapExceptionToResponse(e)));
      }
    });
  }

  @Override
  public void postResourceDetail(Detail entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    try {
      detailDao.save(entity)
        .map(ar -> (Response) PostResourceDetailResponse.respond201WithApplicationJson(entity))
        .otherwise(ExceptionHelper::mapExceptionToResponse)
        .setHandler(asyncResultHandler);
    } catch (Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(ExceptionHelper.mapExceptionToResponse(e)));
    }
  }

  @Override
  public void getResourceDetailById(String id, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext(c -> {
      try {
        detailDao.getById(id)
          .map(ar -> (Response) GetResourceDetailByIdResponse.respond200WithApplicationJson(ar.get()))
          .otherwise(ExceptionHelper::mapExceptionToResponse)
          .setHandler(asyncResultHandler);
      } catch (Exception e) {
        asyncResultHandler.handle(Future.succeededFuture(
          ExceptionHelper.mapExceptionToResponse(e)));
      }
    });
  }

  @Override
  public void postResourceAirPlane(AirPlane entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    try {
      airPlaneDao.save(entity)
        .map(ar -> (Response) PostResourceAirPlaneResponse.respond201WithApplicationJson(entity))
        .otherwise(ExceptionHelper::mapExceptionToResponse)
        .setHandler(asyncResultHandler);
    } catch (Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(ExceptionHelper.mapExceptionToResponse(e)));
    }
  }

  @Override
  public void postResourcePassport(Passport entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext(v -> {
      try {
        passportDao.save(entity)
          .map(ar -> (Response) PostResourcePassportResponse.respond201WithApplicationJson(entity))
          .otherwise(ExceptionHelper::mapExceptionToResponse)
          .setHandler(asyncResultHandler);
      } catch (Exception e) {
        asyncResultHandler.handle(Future.succeededFuture(ExceptionHelper.mapExceptionToResponse(e)));
      }
    });
  }

  @Override
  public void postResourcePerson(Person entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext(v -> {
      try {
        personDao.save(entity)
          .map(ar -> (Response) PostResourcePersonResponse.respond201WithApplicationJson(entity))
          .otherwise(ExceptionHelper::mapExceptionToResponse)
          .setHandler(asyncResultHandler);
      } catch (Exception e) {
        asyncResultHandler.handle(Future.succeededFuture(ExceptionHelper.mapExceptionToResponse(e)));
      }
    });
  }

  @Override
  public void getResourcePersonById(String id, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext(c -> {
      try {
        personDao.getById(id)
          .map(ar -> (Response) GetResourcePersonByIdResponse.respond200WithApplicationJson(ar.get()))
          .otherwise(ExceptionHelper::mapExceptionToResponse)
          .setHandler(asyncResultHandler);
      } catch (Exception e) {
        asyncResultHandler.handle(Future.succeededFuture(
          ExceptionHelper.mapExceptionToResponse(e)));
      }
    });
  }
}
