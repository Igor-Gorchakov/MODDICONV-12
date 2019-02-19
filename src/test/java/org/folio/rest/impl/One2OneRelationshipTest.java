package org.folio.rest.impl;

import com.jayway.restassured.RestAssured;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.http.HttpStatus;
import org.folio.dao.one2one.PassportDao;
import org.folio.dao.one2one.PersonDao;
import org.folio.rest.jaxrs.model.Passport;
import org.folio.rest.jaxrs.model.Person;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.hamcrest.Matchers.is;

/**
 * Testing o2o relationship built on a top of RMB.
 * Cases: happy path, referential integrity, uniqueness.
 */
@RunWith(VertxUnitRunner.class)
public class One2OneRelationshipTest extends AbstractRestVerticleTest {

  private static final String PASSPORT_SERVICE_URL = "/resource/passport";
  private static final String PERSON_SERVICE_URL = "/resource/person";

  /**
   * Testing o2o happy path.
   * Testing fk referential integrity.
   * Person should be returned with the corresponding Passport id.
   * 1. Create Passport and Person that references to the Passport
   * 2. Save Passport
   * 3. Try to save Person
   */
  @Test
  public void shouldSavePersonWithCorrespondingPassportId() {
    // given
    Passport passport = new Passport()
      .withId(UUID.randomUUID().toString())
      .withSeries("EE4519");
    Person person = new Person()
      .withId(UUID.randomUUID().toString())
      .withHeight(170)
      .withWeight(80)
      .withPassportId(passport.getId());

    // when
    savePassport(passport);
    savePerson(person);

    //then
    RestAssured.given()
      .spec(spec)
      .when()
      .get(PERSON_SERVICE_URL + "/" + person.getId())
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("id", is(person.getId()))
      .body("passportId", is(passport.getId()));
  }

  /**
   * Testing fk referential integrity.
   * Saves Person if reference to the Passport is null and there is no Passport data stored.
   * 1. Create Person with null reference to Passport
   * 2. Try to save Person
   */
  @Test
  public void shouldSavePerson_IfPassportReferenceIsNull() {
    // given
    Person person = new Person()
      .withId(UUID.randomUUID().toString())
      .withHeight(150)
      .withWeight(65)
      .withPassportId(null);

    // when
    savePerson(person);
  }

  /**
   * Testing fk referential integrity.
   * Saves Person if reference to the Passport is null and Passport data is stored.
   * 1. Create Passport and Person with null Passport's UUID
   * 2. Save Passport
   * 3. Try to save Person
   */
  @Test
  public void shouldSavePerson_IfPassportReferenceIsNullButPassportExist() {
    // given
    String passportReference = null;
    Person person = new Person()
      .withId(UUID.randomUUID().toString()).withHeight(175).withWeight(72)
      .withPassportId(passportReference);
    Passport passport = new Passport().withId(UUID.randomUUID().toString()).withSeries("EE009122");

    // when
    savePassport(passport);
    //then
    savePerson(person);
  }

  /**
   * Testing fk referential integrity.
   * Does not save Person if reference follows to non existing Passport entity (aka wrong reference).
   * Backend throws GenericDatabaseException with message "/Key (passportid)=(UUID) is not present in table "passports"/"
   * and return 422 response (Unprocessable entity).
   * 1. Create Passport and Person with wrong Passport's UUID
   * 2. Save Passport
   * 3. Try to save Person
   */
  @Test
  public void shouldReturn422Response_IfReferenceToPassportIsWrong() {
    // given
    String wrongPassportId = UUID.randomUUID().toString();
    Person person = new Person()
      .withId(UUID.randomUUID().toString()).withHeight(180).withWeight(75)
      .withPassportId(wrongPassportId);
    Passport passport = new Passport().withId(UUID.randomUUID().toString()).withSeries("EE0011209");

    // when
    savePassport(passport);

    //then
    RestAssured.given()
      .spec(spec)
      .body(person)
      .when()
      .post(PERSON_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
  }

  /**
   * Testing fk uniqueness.
   * Person should not be saved if reference to the Passport is not unique.
   * 1. Create 1 Passport and 2 Persons with the same reference to the Password
   * 2. Save Passport
   * 3. Save 1st Person
   * 4. Try to save 2nd Person
   */
  @Test
  public void shouldReturn422Response_If2PersonsReferTo1Passport() {
    // given
    String passportId = UUID.randomUUID().toString();
    Person person1 = new Person().withId(UUID.randomUUID().toString()).withHeight(160).withWeight(55).withPassportId(passportId);
    Person person2 = new Person().withId(UUID.randomUUID().toString()).withHeight(175).withWeight(74).withPassportId(passportId);
    Passport passport = new Passport().withId(passportId).withSeries("ZA780012");

    // when
    savePassport(passport);
    savePerson(person1);

    //then
    RestAssured.given()
      .spec(spec)
      .body(person2)
      .when()
      .post(PERSON_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
  }

  private void savePerson(Person person) {
    RestAssured.given()
      .spec(spec)
      .body(person)
      .when()
      .post(PERSON_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);
  }

  private void savePassport(Passport passport) {
    RestAssured.given()
      .spec(spec)
      .body(passport)
      .when()
      .post(PASSPORT_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_CREATED);
  }

  @Override
  public void clearTables(TestContext context) {
    PostgresClient.getInstance(vertx, TENANT_ID).delete(PersonDao.TABLE, new Criterion(), personTableDeleteEvent -> {
      if (personTableDeleteEvent.failed()) {
        context.fail(personTableDeleteEvent.cause());
      } else {
        PostgresClient.getInstance(vertx, TENANT_ID).delete(PassportDao.TABLE, new Criterion(), passportTableDeleteEvent -> {
          if (passportTableDeleteEvent.failed()) {
            context.fail(passportTableDeleteEvent.cause());
          }
        });
      }
    });
  }
}
