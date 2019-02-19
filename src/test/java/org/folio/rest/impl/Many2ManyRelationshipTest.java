package org.folio.rest.impl;

import com.jayway.restassured.RestAssured;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.http.HttpStatus;
import org.folio.dao.many2many.EmployeeDao;
import org.folio.dao.many2many.LocationDao;
import org.folio.dao.many2many.TicketDao;
import org.folio.rest.jaxrs.model.Employee;
import org.folio.rest.jaxrs.model.Location;
import org.folio.rest.jaxrs.model.Ticket;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.hamcrest.Matchers.is;

/**
 * Testing m2m relationship built on a top of RMB.
 * Cases: happy path, referential integrity.
 */
@RunWith(VertxUnitRunner.class)
public class Many2ManyRelationshipTest extends AbstractRestVerticleTest {

  private static final String EMPLOYEE_SERVICE_URL = "/resource/employee";
  private static final String LOCATION_SERVICE_URL = "/resource/location";
  private static final String TICKET_SERVICE_URL = "/resource/ticket";

  /**
   * Testing m2m happy path.
   * Employee has 2 Locations.
   * Testing fk referential integrity.
   * 1. Create Employee
   * 2. Create Location California
   * 3. Create Ticket1 linked to Employee and Location California
   * 4. Save Employee and Location
   * 5. Try to save Ticket1
   * 6. Create Location Colorado
   * 7. Create Ticket2 linked to Employee and Location Colorado
   * 8. Try to save Ticket2
   */
  @Test
  public void shouldSaveTicketWithEmployeeAndLocation() {
    // given
    Employee employee = new Employee().withId(UUID.randomUUID().toString()).withName("Carl");
    Location california = new Location().withId(UUID.randomUUID().toString()).withCaption("California");
    saveEmployee(employee);
    saveLocation(california);

    Ticket ticketToCalifornia = new Ticket().withId(UUID.randomUUID().toString()).withEmployeeId(employee.getId()).withLocationId(california.getId());
    saveTicket(ticketToCalifornia);

    Location boston = new Location().withId(UUID.randomUUID().toString()).withCaption("Boston");
    Ticket ticket = new Ticket().withId(UUID.randomUUID().toString()).withEmployeeId(employee.getId()).withLocationId(boston.getId());

    // when
    saveLocation(boston);

    // then
    saveTicket(ticket);
  }

  /**
   * Testing fk referential integrity.
   * 1. Create Ticket with null references to Employee and Location
   * 2. Try to save Ticket
   */
  @Test
  public void shouldSaveTicket_IfReferencesAreNull() {
    // given
    Ticket ticket = new Ticket().withId(UUID.randomUUID().toString()).withEmployeeId(null).withLocationId(null);

    // when
    RestAssured.given()
      .spec(spec)
      .body(ticket)
      .when()
      .post(TICKET_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    // then
    RestAssured.given()
      .spec(spec)
      .when()
      .get(TICKET_SERVICE_URL + "/" + ticket.getId())
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("id", is(ticket.getId()))
      .body("employeeId", is(ticket.getEmployeeId()))
      .body("locationId", is(ticket.getLocationId()));
  }

  /**
   * Testing fk referential integrity.
   * 1. Create Employee and Location
   * 2. Create Ticket with null references to Employee and Location
   * 3. Save Employee and Location
   * 4. Try to save Ticket
   */
  @Test
  public void shouldSaveTicket_IfReferencesAreNullButEmployeeAndLocationExist() {
    // given
    Employee employee = new Employee().withId(UUID.randomUUID().toString()).withName("Alice");
    Location location = new Location().withId(UUID.randomUUID().toString()).withCaption("HongKong");
    Ticket ticket = new Ticket().withId(UUID.randomUUID().toString()).withEmployeeId(null).withLocationId(null);

    // when
    saveEmployee(employee);
    saveLocation(location);

    // then
    RestAssured.given()
      .spec(spec)
      .body(ticket)
      .when()
      .post(TICKET_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);
  }

  /**
   * Testing fk referential integrity.
   * 1. Create Employee and Location
   * 2. Create Ticket with correct reference to the Employee but wrong to the Location
   * 3. Save Employee
   * 4. Save Location
   * 5. Try to save Ticket
   */
  @Test
  public void shouldReturn422Response_IfReferenceToLocationIsWrong() {
    // given
    Employee employee = new Employee().withId(UUID.randomUUID().toString()).withName("Mark");
    Location location = new Location().withId(UUID.randomUUID().toString()).withCaption("Colorado");
    Ticket ticket = new Ticket().withId(UUID.randomUUID().toString()).withEmployeeId(employee.getId())
      .withLocationId(UUID.randomUUID().toString());

    // when
    saveEmployee(employee);
    saveLocation(location);

    // then
    RestAssured.given()
      .spec(spec)
      .body(ticket)
      .when()
      .post(TICKET_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
  }

  private void saveLocation(Location location) {
    RestAssured.given()
      .spec(spec)
      .body(location)
      .when()
      .post(LOCATION_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);
  }

  private void saveEmployee(Employee employee) {
    RestAssured.given()
      .spec(spec)
      .body(employee)
      .when()
      .post(EMPLOYEE_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);
  }

  private void saveTicket(Ticket ticketToCalifornia) {
    RestAssured.given()
      .spec(spec)
      .body(ticketToCalifornia)
      .when()
      .post(TICKET_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);
  }

  @Override
  public void clearTables(TestContext context) {
    PostgresClient.getInstance(vertx, TENANT_ID).delete(TicketDao.TABLE, new Criterion(), ticketTableDeleteEvent -> {
      if (ticketTableDeleteEvent.failed()) {
        context.fail(ticketTableDeleteEvent.cause());
      } else {
        PostgresClient.getInstance(vertx, TENANT_ID).delete(EmployeeDao.TABLE, new Criterion(), employeeTableDeleteEvent -> {
          if (employeeTableDeleteEvent.failed()) {
            context.fail(employeeTableDeleteEvent.cause());
          } else {
            PostgresClient.getInstance(vertx, TENANT_ID).delete(LocationDao.TABLE, new Criterion(), locationTableDeleteEvent -> {
              if (locationTableDeleteEvent.failed()) {
                context.fail(locationTableDeleteEvent.cause());
              }
            });
          }
        });
      }
    });
  }
}
