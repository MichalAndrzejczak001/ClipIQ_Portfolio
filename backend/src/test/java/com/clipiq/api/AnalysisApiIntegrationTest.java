package com.clipiq.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

/**
 * Full-stack integration tests: real Spring context, real MongoDB (Testcontainers),
 * real {@code AnalysisService}/{@code AnalysisRepository} — no mocks. Verifies the
 * actual register → persist → retrieve pipeline over HTTP, which the mocked
 * controller/REST Assured/BDD test suites cannot exercise.
 */
@Epic("REST API")
@Feature("Analysis — full-stack integration (no mocks)")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AnalysisApiIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    @Story("Register URL then retrieve by uuid")
    @Severity(SeverityLevel.CRITICAL)
    void registerUrl_thenGetByUuid_persistsAndReturnsRealAnalysis() {
        String uuid =
                given()
                        .contentType(ContentType.JSON)
                        .body("{\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"}")
                .when()
                        .post("/register/url")
                .then()
                        .statusCode(200)
                        .body(matchesJsonSchemaInClasspath("schema/register-response-schema.json"))
                        .extract().path("analysisUuid");

        given()
        .when()
                .get("/analyse/" + uuid)
        .then()
                .statusCode(200)
                .body("uuid", equalTo(uuid))
                .body("fileType", equalTo("YOUTUBE"))
                .body("status", equalTo("IN_PROGRESS"))
                .body(matchesJsonSchemaInClasspath("schema/analysis-response-schema.json"));
    }

    @Test
    @Story("Register audio file then retrieve by uuid")
    @Severity(SeverityLevel.CRITICAL)
    void registerFile_thenGetByUuid_persistsAndReturnsRealAnalysis() {
        String uuid =
                given()
                        .multiPart("file", "real-integration.mp3", new byte[]{1, 2, 3}, "audio/mpeg")
                .when()
                        .post("/register/file")
                .then()
                        .statusCode(200)
                        .extract().path("analysisUuid");

        given()
        .when()
                .get("/analyse/" + uuid)
        .then()
                .statusCode(200)
                .body("uuid", equalTo(uuid))
                .body("fileType", equalTo("RAW"))
                .body("status", equalTo("IN_PROGRESS"));
    }

    @Test
    @Story("Get analyses by multiple uuids returns persisted documents")
    @Severity(SeverityLevel.NORMAL)
    void getAnalyses_byMultipleUuids_returnsAllPersisted() {
        String uuid1 = registerTiktokUrl();
        String uuid2 = registerTiktokUrl();

        given()
                .queryParam("uuids", uuid1 + "," + uuid2)
        .when()
                .get("/analyse")
        .then()
                .statusCode(200)
                .body("uuid", hasItems(uuid1, uuid2));
    }

    @Test
    @Story("Delete analysis removes the persisted document")
    @Severity(SeverityLevel.NORMAL)
    void deleteAnalysis_existingUuid_removesDocumentFromDatabase() {
        String uuid = registerTiktokUrl();

        given()
        .when()
                .delete("/analyse/" + uuid)
        .then()
                .statusCode(204);

        given()
        .when()
                .get("/analyse/" + uuid)
        .then()
                .statusCode(404);
    }

    private String registerTiktokUrl() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"url\":\"https://www.tiktok.com/@user/video/7234567890123456789\"}")
        .when()
                .post("/register/url")
        .then()
                .statusCode(200)
                .extract().path("analysisUuid");
    }
}
