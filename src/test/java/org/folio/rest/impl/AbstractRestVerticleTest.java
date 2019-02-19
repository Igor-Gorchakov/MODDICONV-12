package org.folio.rest.impl;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.folio.rest.RestVerticle;
import org.folio.rest.client.TenantClient;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.UUID;

public abstract class AbstractRestVerticleTest {

  static final String TENANT_ID = "diku";
  private static final String GET_USER_URL = "/users?query=id==";
  static Vertx vertx;
  static RequestSpecification spec;
  private static String USER_ID = UUID.randomUUID().toString();
  private static String useExternalDatabase;
  private static int PORT = NetworkUtils.nextFreePort();
  private static int MOCK_PORT = NetworkUtils.nextFreePort();
  private static String BASE_URL = "http://localhost:";
  private static String OKAPI_URL = BASE_URL + PORT;
  private static String MOCK_URL = BASE_URL + MOCK_PORT;

  @BeforeClass
  public static void setUpClass(final TestContext context) throws Exception {
    Async async = context.async();
    vertx = Vertx.vertx();

    useExternalDatabase = System.getProperty(
      "org.folio.converter.storage.test.database",
      "embedded");

    switch (useExternalDatabase) {
      case "environment":
        System.out.println("Using environment settings");
        break;
      case "external":
        String postgresConfigPath = System.getProperty(
          "org.folio.converter.storage.test.config",
          "/postgres-conf-local.json");
        PostgresClient.setConfigFilePath(postgresConfigPath);
        break;
      case "embedded":
        PostgresClient.setIsEmbedded(true);
        PostgresClient.getInstance(vertx).startEmbeddedPostgres();
        break;
      default:
        String message = "No understood database choice made." +
          "Please set org.folio.converter.storage.test.database" +
          "to 'external', 'environment' or 'embedded'";
        throw new Exception(message);
    }

    TenantClient tenantClient = new TenantClient(OKAPI_URL, "diku", "dummy-token");
    DeploymentOptions restVerticleDeploymentOptions = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", PORT));
    vertx.deployVerticle(RestVerticle.class.getName(), restVerticleDeploymentOptions, res -> {
      try {
        tenantClient.postTenant(null, res2 -> async.complete());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    spec = new RequestSpecBuilder()
      .setContentType(ContentType.JSON)
      .setBaseUri(OKAPI_URL)
      .addHeader(RestVerticle.OKAPI_HEADER_TENANT, TENANT_ID)
      .addHeader(RestVerticle.OKAPI_USERID_HEADER, USER_ID)
      .addHeader(RestVerticle.OKAPI_HEADER_PREFIX + "-url", MOCK_URL)
      .build();
  }

  @AfterClass
  public static void tearDownClass(final TestContext context) {
    Async async = context.async();
    vertx.close(context.asyncAssertSuccess(res -> {
      if (useExternalDatabase.equals("embedded")) {
        PostgresClient.stopEmbeddedPostgres();
      }
      async.complete();
    }));
  }

  @Before
  public abstract void clearTables(TestContext context);

}