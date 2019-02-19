package org.folio.rest.impl;

import com.jayway.restassured.RestAssured;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.http.HttpStatus;
import org.folio.dao.one2many.AirPlaneDao;
import org.folio.dao.one2many.DetailDao;
import org.folio.rest.jaxrs.model.AirPlane;
import org.folio.rest.jaxrs.model.Detail;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.hamcrest.Matchers.is;

/**
 * Testing o2m relationship built on a top of RMB.
 * Cases: happy path, referential integrity.
 */
@RunWith(VertxUnitRunner.class)
public class One2ManyRelationshipTest extends AbstractRestVerticleTest {

  private static final String AIRPLANE_SERVICE_URL = "/resource/airPlane";
  private static final String DETAIL_SERVICE_URL = "/resource/detail";

  /**
   * Testing o2m happy path.
   * Testing fk referential integrity.
   * 1. Create AirPlane
   * 2. Create Details with corresponding references to AirPlane
   * 3. Save Airplane
   * 4. Try to save Details
   */
  @Test
  public void shouldSaveAirPlaneWith2Details() {
    // given
    AirPlane a380 = new AirPlane().withId(UUID.randomUUID().toString()).withCaption("A380");
    Detail leftWing = new Detail().withId(UUID.randomUUID().toString()).withCaption("left wing").withAirPlaneId(a380.getId());
    Detail rightWing = new Detail().withId(UUID.randomUUID().toString()).withCaption("right wing").withAirPlaneId(a380.getId());

    // when
    saveAirPlane(a380);
    saveDetail(leftWing);
    saveDetail(rightWing);

    // then
    RestAssured.given()
      .spec(spec)
      .when()
      .get(DETAIL_SERVICE_URL + "/" + leftWing.getId())
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("id", is(leftWing.getId()))
      .body("airPlaneId", is(a380.getId()));

    RestAssured.given()
      .spec(spec)
      .when()
      .get(DETAIL_SERVICE_URL + "/" + rightWing.getId())
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("id", is(rightWing.getId()))
      .body("airPlaneId", is(a380.getId()));
  }

  /**
   * Testing fk referential integrity.
   * Returns 200 response (OK) if trying to save Detail with null reference to the AirPlane.
   * 1. Create Detail with null reference to the AirPlane
   * 2. Try to save Detail
   */
  @Test
  public void shouldSaveDetail_IfReferenceIsNull() {
    // given
    Detail detail = new Detail().withId(UUID.randomUUID().toString()).withCaption("wing").withAirPlaneId(null);

    // then
    saveDetail(detail);
  }

  /**
   * Testing fk referential integrity.
   * 1. Create AirPlane
   * 2. Create Detail with null reference to the AirPlane
   * 3. Save AirPlane
   * 4. Try to save Detail
   */
  @Test
  public void shouldSaveDetail_IfReferenceIsNullButAirPlaneExists() {
    // given
    AirPlane a370 = new AirPlane().withId(UUID.randomUUID().toString()).withCaption("A370");
    Detail tailSection = new Detail().withId(UUID.randomUUID().toString()).withCaption("tail section")
      .withAirPlaneId(null);

    // when
    saveAirPlane(a370);

    // then
    RestAssured.given()
      .spec(spec)
      .body(tailSection)
      .when()
      .post(DETAIL_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);
  }

  /**
   * Testing fk referential integrity.
   * Returns 422 response (Uprocessable entity) if trying to save Detail with wrong reference to the AirPlane.
   * 1. Create AirPlane
   * 2. Create detail with corresponding reference to the AirPlane
   * 3. Create detail with wrong UUID to the AirPlane
   * 4. Save AirPlane
   * 5. Try to save Detail from the 3rd step
   */
  @Test
  public void shouldReturn422Response_IfReferenceToAirPlaneIsWrong() {
    // given
    String wrongAirPlaneReference = UUID.randomUUID().toString();
    AirPlane a370 = new AirPlane().withId(UUID.randomUUID().toString()).withCaption("A370");
    Detail leftWing = new Detail().withId(UUID.randomUUID().toString()).withCaption("left wing").withAirPlaneId(a370.getId());
    Detail rightWing = new Detail().withId(UUID.randomUUID().toString()).withCaption("right wing")
      .withAirPlaneId(wrongAirPlaneReference);

    // when
    saveAirPlane(a370);
    saveDetail(leftWing);

    // then
    RestAssured.given()
      .spec(spec)
      .body(rightWing)
      .when()
      .post(DETAIL_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    RestAssured.given()
      .spec(spec)
      .when()
      .get(DETAIL_SERVICE_URL + "/" + leftWing.getId())
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("id", is(leftWing.getId()))
      .body("airPlaneId", is(a370.getId()));
  }

  private void saveDetail(Detail leftWing) {
    RestAssured.given()
      .spec(spec)
      .body(leftWing)
      .when()
      .post(DETAIL_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);
  }

  private void saveAirPlane(AirPlane airPlane) {
    RestAssured.given()
      .spec(spec)
      .body(airPlane)
      .when()
      .post(AIRPLANE_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);
  }

  @Override
  public void clearTables(TestContext context) {
    PostgresClient.getInstance(vertx, TENANT_ID).delete(DetailDao.TABLE, new Criterion(), detailTableDeleteEvent -> {
      if (detailTableDeleteEvent.failed()) {
        context.fail(detailTableDeleteEvent.cause());
      } else {
        PostgresClient.getInstance(vertx, TENANT_ID).delete(AirPlaneDao.TABLE, new Criterion(), airPlaneTableDeleteEvent -> {
          if (airPlaneTableDeleteEvent.failed()) {
            context.fail(airPlaneTableDeleteEvent.cause());
          }
        });
      }
    });
  }
}
