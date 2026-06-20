package com.clipiq.api;

import com.clipiq.repository.AnalysisRepository;
import com.clipiq.service.AnalysisService;
import com.github.javafaker.Faker;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Epic("ClipIQ REST API")
@Feature("Analysis API — REST Assured")
public class AnalysisApiRestAssuredTest extends AbstractTestNGSpringContextTests {

    private final Faker faker = new Faker();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalysisService analysisService;

    @MockBean
    private AnalysisRepository analysisRepository;

    @BeforeMethod(alwaysRun = true)
    public void setUpBeforeEach() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        Mockito.reset(analysisService, analysisRepository);
    }

    @Test(groups = {"regression"})
    @Story("Register URL response matches JSON schema")
    @Severity(SeverityLevel.CRITICAL)
    public void registerUrl_responseMatchesSchema() {
        String uuid = faker.internet().uuid();
        String videoId = faker.regexify("[A-Za-z0-9_-]{11}");
        when(analysisService.registerUrl(any())).thenReturn(uuid);

        given()
            .contentType(ContentType.JSON)
            .body("{\"url\":\"https://youtube.com/watch?v=" + videoId + "\"}")
        .when()
            .post("/register/url")
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schema/register-response-schema.json"));
    }

    @Test(groups = {"regression"})
    @Story("Register file response contains non-null analysisUuid")
    @Severity(SeverityLevel.CRITICAL)
    public void registerFile_responseContainsUuid() {
        String uuid = faker.internet().uuid();
        String filename = faker.lorem().word() + ".mp3";
        when(analysisService.registerFile(any(), any())).thenReturn(uuid);

        given()
            .multiPart("file", filename, "data".getBytes(), "audio/mpeg")
        .when()
            .post("/register/file")
        .then()
            .statusCode(200)
            .body("analysisUuid", notNullValue());
    }

    @Test(groups = {"negative"})
    @Story("Register URL with invalid domain returns 400")
    @Severity(SeverityLevel.NORMAL)
    public void registerUrl_invalidDomain_returns400() {
        String randomPath = faker.lorem().word();

        given()
            .contentType(ContentType.JSON)
            .body("{\"url\":\"https://invalid-domain.com/" + randomPath + "\"}")
        .when()
            .post("/register/url")
        .then()
            .statusCode(400);
    }

    @Test(groups = {"negative"})
    @Story("Register URL with path traversal returns 400")
    @Severity(SeverityLevel.NORMAL)
    public void registerUrl_pathTraversal_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"url\":\"https://youtube.com/../etc/passwd\"}")
        .when()
            .post("/register/url")
        .then()
            .statusCode(400);
    }

    @Test(groups = {"negative"})
    @Story("Register file with unsupported extension returns 400")
    @Severity(SeverityLevel.NORMAL)
    public void registerFile_unsupportedExtension_returns400() {
        String filename = faker.lorem().word() + ".avi";

        given()
            .multiPart("file", filename, "data".getBytes(), "video/x-msvideo")
        .when()
            .post("/register/file")
        .then()
            .statusCode(400);
    }

    @Test(groups = {"negative"})
    @Story("Register URL with missing url field returns 400")
    @Severity(SeverityLevel.MINOR)
    public void registerUrl_missingUrlField_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/register/url")
        .then()
            .statusCode(400);
    }
}
