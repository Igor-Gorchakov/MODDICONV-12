package org.folio.rest.impl;

import com.jayway.restassured.RestAssured;
import io.vertx.ext.unit.Async;
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
   * 4. Get and assert Passport and Person
   */
  @Test
  public void shouldSavePersonWithCorrespondingPassportId() {
    // Create Passport and Person that references to the Passport
    Passport passport = new Passport()
      .withId(UUID.randomUUID().toString())
      .withSeries("EE4519");
    Person person = new Person()
      .withId(UUID.randomUUID().toString())
      .withHeight(170)
      .withWeight(80)
      .withPassportId(passport.getId());

    // Save Passport
    savePassport(passport);
    // Try to save Person
    savePerson(person);

    // Get and assert Passport and Person
    getAndAssertPerson(person);
    getAndAssertPassport(passport);
  }

  /**
   * Testing fk referential integrity.
   * Saves Person if reference to the Passport is null and there is no Passport data stored.
   * 1. Create Person with null reference to Passport
   * 2. Try to save Person
   * 3. Get and assert person
   */
  @Test
  public void shouldSavePerson_IfPassportReferenceIsNull() {
    // Create Person with null reference to Passport
    Person person = new Person()
      .withId(UUID.randomUUID().toString())
      .withHeight(150)
      .withWeight(65)
      .withPassportId(null);

    // Try to save Person
    savePerson(person);

    // Get and assert person
    getAndAssertPerson(person);
  }

  /**
   * Testing fk referential integrity.
   * Saves Person if reference to the Passport is null and Passport data is stored.
   * 1. Create Passport and Person with null Passport's UUID
   * 2. Save Passport
   * 3. Try to save Person
   * 4. Get and assert Passport and Person
   */
  @Test
  public void shouldSavePerson_IfPassportReferenceIsNullButPassportExists() {
    // Create Passport and Person with null Passport's UUID
    String passportReference = null;
    Person person = new Person()
      .withId(UUID.randomUUID().toString()).withHeight(175).withWeight(72)
      .withPassportId(passportReference);
    Passport passport = new Passport().withId(UUID.randomUUID().toString()).withSeries("EE009122");

    // Save Passport
    savePassport(passport);
    // Try to save Person
    savePerson(person);

    // Get and assert Passport and Person
    getAndAssertPassport(passport);
    getAndAssertPerson(person);
  }

  /**
   * Testing fk referential integrity.
   * Does not save Person if reference follows to non existing Passport entity (aka wrong reference).
   * Backend throws GenericDatabaseException with message "/Key (passportid)=(UUID) is not present in table "passports"/"
   * and return 422 response (Unprocessable entity).
   * 1. Create Passport and Person with wrong Passport's UUID
   * 2. Save Passport
   * 3. Try to save Person
   * 4. Get and assert Passport
   */
  @Test
  public void shouldReturn422Response_IfReferenceToPassportIsWrong() {
    // Create Passport and Person with wrong Passport's UUID
    String wrongPassportId = UUID.randomUUID().toString();
    Person person = new Person()
      .withId(UUID.randomUUID().toString()).withHeight(180).withWeight(75)
      .withPassportId(wrongPassportId);
    Passport passport = new Passport().withId(UUID.randomUUID().toString()).withSeries("EE0011209");

    // Save Passport
    savePassport(passport);

    // Try to save Person
    RestAssured.given()
      .spec(spec)
      .body(person)
      .when()
      .post(PERSON_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    // Get and assert Passport
    getAndAssertPassport(passport);
  }

  /**
   * Testing fk uniqueness.
   * Person should not be saved if reference to the Passport is not unique.
   * 1. Create 1 Passport and 2 Persons with the same reference to the Password
   * 2. Save Passport
   * 3. Save 1st Person
   * 4. Try to save 2nd Person
   * 5. Get and assert 1st Person
   */
  @Test
  public void shouldReturn422Response_If2PersonsReferTo1Passport() {
    // Create 1 Passport and 2 Persons with the same reference to the Password
    String passportId = UUID.randomUUID().toString();
    Person person1 = new Person().withId(UUID.randomUUID().toString()).withHeight(160).withWeight(55).withPassportId(passportId);
    Person person2 = new Person().withId(UUID.randomUUID().toString()).withHeight(175).withWeight(74).withPassportId(passportId);
    Passport passport = new Passport().withId(passportId).withSeries("ZA780012");

    // Save Passport
    savePassport(passport);
    // Save 1st Person
    savePerson(person1);

    // Try to save 2nd Person
    RestAssured.given()
      .spec(spec)
      .body(person2)
      .when()
      .post(PERSON_SERVICE_URL)
      .then()
      .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    // Get and assert 1st Person
    getAndAssertPerson(person1);
  }

  private void getAndAssertPerson(Person person) {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(PERSON_SERVICE_URL + "/" + person.getId())
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("id", is(person.getId()))
      .body("passportId", is(person.getPassportId()))
      .body("height", is(person.getHeight()))
      .body("weight", is(person.getWeight()));
  }

  private void getAndAssertPassport(Passport passport) {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(PASSPORT_SERVICE_URL + "/" + passport.getId())
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("id", is(passport.getId()))
      .body("series", is(passport.getSeries()));
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
    Async async = context.async();
    PostgresClient pgClient = PostgresClient.getInstance(vertx, TENANT_ID);
    pgClient.delete(PersonDao.TABLE, new Criterion(), personTableDeleteEvent -> {
      if (personTableDeleteEvent.failed()) {
        context.fail(personTableDeleteEvent.cause());
      } else {
        pgClient.delete(PassportDao.TABLE, new Criterion(), passportTableDeleteEvent -> {
          if (passportTableDeleteEvent.failed()) {
            context.fail(passportTableDeleteEvent.cause());
          } else {
            async.complete();
          }
        });
      }
    });
  }
}
