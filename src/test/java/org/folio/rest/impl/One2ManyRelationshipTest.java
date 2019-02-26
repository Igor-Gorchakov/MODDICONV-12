package org.folio.rest.impl;

import com.jayway.restassured.RestAssured;
import io.vertx.ext.unit.Async;
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
   * 3. Save AirPlane
   * 4. Try to save Details
   * 5. Get and assert AirPlane and Details
   */
  @Test
  public void shouldSaveAirPlaneWith2Details() {
    // Create AirPlane, create Details with corresponding references to AirPlane
    AirPlane a380 = new AirPlane().withId(UUID.randomUUID().toString()).withModel("A380");
    Detail leftWing = new Detail().withId(UUID.randomUUID().toString()).withCaption("left wing").withAirPlaneId(a380.getId());
    Detail rightWing = new Detail().withId(UUID.randomUUID().toString()).withCaption("right wing").withAirPlaneId(a380.getId());

    // Save AirPlane, try to save Details
    saveAirPlane(a380);
    saveDetail(leftWing);
    saveDetail(rightWing);

    // Get and assert AirPlane and Details
    getAndAssertAirPlane(a380);
    getAndAssertDetail(leftWing);
    getAndAssertDetail(rightWing);
  }

  /**
   * Testing fk referential integrity.
   * Returns 200 response (OK) if trying to save Detail with null reference to the AirPlane.
   * 1. Create Detail with null reference to the AirPlane
   * 2. Try to save Detail
   * 3. Get and assert Detail
   */
  @Test
  public void shouldSaveDetail_IfReferenceIsNull() {
    // Create Detail with null reference to the AirPlane
    Detail wing = new Detail().withId(UUID.randomUUID().toString()).withCaption("wing").withAirPlaneId(null);

    // Try to save Detail
    saveDetail(wing);

    // Get and assert Detail
    getAndAssertDetail(wing);
  }

  /**
   * Testing fk referential integrity.
   * 1. Create AirPlane
   * 2. Create Detail with null reference to the AirPlane
   * 3. Save AirPlane
   * 4. Try to save Detail
   * 5. Get and assert AirPlane and Detail
   */
  @Test
  public void shouldSaveDetail_IfReferenceIsNullButAirPlaneExists() {
    // Create AirPlane, create Detail with null reference to the AirPlane
    AirPlane a370 = new AirPlane().withId(UUID.randomUUID().toString()).withModel("A370");
    Detail tailSection = new Detail().withId(UUID.randomUUID().toString()).withCaption("tail section")
      .withAirPlaneId(null);

    // Save AirPlane
    saveAirPlane(a370);

    // Try to save Detail
    RestAssured.given()
      .spec(spec)
      .body(tailSection)
      .when()
      .post(DETAIL_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    // Get and assert AirPlane and Detail
    getAndAssertAirPlane(a370);
    getAndAssertDetail(tailSection);
  }

  /**
   * Testing fk referential integrity.
   * Returns 422 response (Uprocessable entity) if trying to save Detail with wrong reference to the AirPlane.
   * 1. Create AirPlane
   * 2. Create detail with correct reference to the AirPlane
   * 3. Create detail with wrong UUID to the AirPlane
   * 4. Save AirPlane
   * 5. Save Detail with correct reference
   * 6. Try to save Detail with wrong reference
   * 7. Get and assert AirPlane and Detail with correct reference
   */
  @Test
  public void shouldReturn422Response_IfReferenceToAirPlaneIsWrong() {
    // Create AirPlane
    String wrongAirPlaneReference = UUID.randomUUID().toString();
    AirPlane a370 = new AirPlane().withId(UUID.randomUUID().toString()).withModel("A370");
    // Create detail with correct reference to the AirPlane
    Detail leftWing = new Detail().withId(UUID.randomUUID().toString()).withCaption("left wing")
      .withAirPlaneId(a370.getId());
    // Create detail with wrong UUID to the AirPlane
    Detail rightWing = new Detail().withId(UUID.randomUUID().toString()).withCaption("right wing")
      .withAirPlaneId(wrongAirPlaneReference);

    // Save AirPlane
    saveAirPlane(a370);
    // Save Detail with correct reference
    saveDetail(leftWing);

    // Try to save Detail with wrong reference
    RestAssured.given()
      .spec(spec)
      .body(rightWing)
      .when()
      .post(DETAIL_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    // Get and assert AirPlane and Detail with correct reference
    getAndAssertAirPlane(a370);
    getAndAssertDetail(leftWing);
  }

  private void getAndAssertAirPlane(AirPlane airPlane) {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(AIRPLANE_SERVICE_URL + "/" + airPlane.getId())
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("id", is(airPlane.getId()))
      .body("model", is(airPlane.getModel()));
  }

  private void getAndAssertDetail(Detail detail) {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(DETAIL_SERVICE_URL + "/" + detail.getId())
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("id", is(detail.getId()))
      .body("airPlaneId", is(detail.getAirPlaneId()));
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
    Async async = context.async();
    PostgresClient pgClient = PostgresClient.getInstance(vertx, TENANT_ID);
    PostgresClient.getInstance(vertx, TENANT_ID).delete(DetailDao.TABLE, new Criterion(), detailTableDeleteEvent -> {
      if (detailTableDeleteEvent.failed()) {
        context.fail(detailTableDeleteEvent.cause());
      } else {
        pgClient.delete(AirPlaneDao.TABLE, new Criterion(), airPlaneTableDeleteEvent -> {
          if (airPlaneTableDeleteEvent.failed()) {
            context.fail(airPlaneTableDeleteEvent.cause());
          } else {
            async.complete();
          }
        });
      }
    });
  }
}
