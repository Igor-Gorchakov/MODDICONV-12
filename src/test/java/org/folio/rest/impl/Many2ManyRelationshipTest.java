package org.folio.rest.impl;

import com.jayway.restassured.RestAssured;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.http.HttpStatus;
import org.folio.rest.jaxrs.model.Employee;
import org.folio.rest.jaxrs.model.Location;
import org.folio.rest.jaxrs.model.Ticket;
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
   * Testing fk referential integrity.
   * 1. Create Employee
   * 2. Create Location
   * 3. Create Ticket linked to Employee and Location
   * 4. Save Employee and Location
   * 5. Try to save Ticket
   */
  @Test
  public void shouldSaveTicketWithEmployeeAndLocation() {
    // given
    Employee employee = new Employee().withId(UUID.randomUUID().toString()).withName("Carl");
    Location location = new Location().withId(UUID.randomUUID().toString()).withCaption("California");
    Ticket ticket = new Ticket().withId(UUID.randomUUID().toString()).withEmployeeId(employee.getId()).withLocationId(location.getId());

    // when
    RestAssured.given()
      .spec(spec)
      .body(employee)
      .when()
      .post(EMPLOYEE_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    RestAssured.given()
      .spec(spec)
      .body(location)
      .when()
      .post(LOCATION_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

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
      .body("employeeId", is(employee.getId()))
      .body("locationId", is(location.getId()));
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
    RestAssured.given()
      .spec(spec)
      .body(employee)
      .when()
      .post(EMPLOYEE_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    RestAssured.given()
      .spec(spec)
      .body(location)
      .when()
      .post(LOCATION_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

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
    RestAssured.given()
      .spec(spec)
      .body(employee)
      .when()
      .post(EMPLOYEE_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    RestAssured.given()
      .spec(spec)
      .body(location)
      .when()
      .post(LOCATION_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    // then
    RestAssured.given()
      .spec(spec)
      .body(ticket)
      .when()
      .post(TICKET_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
  }

  @Override
  public void clearTables(TestContext context) {

  }
}
