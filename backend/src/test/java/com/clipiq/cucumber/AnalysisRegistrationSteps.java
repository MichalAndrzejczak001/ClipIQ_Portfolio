package com.clipiq.cucumber;

import com.clipiq.service.AnalysisService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AnalysisRegistrationSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalysisService analysisService;

    private MockMultipartFile pendingFile;
    private String pendingJsonBody;
    private MockHttpServletResponse lastResponse;

    @Before
    public void resetState() {
        Mockito.reset(analysisService);
        pendingFile = null;
        pendingJsonBody = null;
        lastResponse = null;
    }

    @Given("a multipart file named {string} with type {string}")
    public void aMultipartFileNamedWithType(String filename, String contentType) {
        when(analysisService.registerFile(any(), any())).thenReturn("cucumber-uuid-file");
        pendingFile = new MockMultipartFile("file", filename, contentType, "data".getBytes());
    }

    @Given("a JSON body with url {string}")
    public void aJsonBodyWithUrl(String url) {
        when(analysisService.registerUrl(any())).thenReturn("cucumber-uuid-url");
        pendingJsonBody = "{\"url\":\"" + url + "\"}";
    }

    @When("the user posts to {string}")
    public void theUserPostsTo(String path) throws Exception {
        if (pendingFile != null) {
            lastResponse = mockMvc.perform(multipart(path).file(pendingFile))
                    .andReturn().getResponse();
            pendingFile = null;
        } else if (pendingJsonBody != null) {
            lastResponse = mockMvc.perform(post(path)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(pendingJsonBody))
                    .andReturn().getResponse();
            pendingJsonBody = null;
        }
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expectedStatus) {
        assertThat(lastResponse.getStatus()).isEqualTo(expectedStatus);
    }

    @And("the response body contains field {string}")
    public void theResponseBodyContainsField(String field) throws Exception {
        assertThat(lastResponse.getContentAsString()).contains("\"" + field + "\"");
    }
}
