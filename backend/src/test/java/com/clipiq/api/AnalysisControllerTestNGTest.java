package com.clipiq.api;

import com.clipiq.repository.AnalysisRepository;
import com.clipiq.service.AnalysisService;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Epic("ClipIQ REST API")
@Feature("Analysis Controller — TestNG")
public class AnalysisControllerTestNGTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalysisService analysisService;

    @MockBean
    private AnalysisRepository analysisRepository;

    @Test(groups = {"smoke"})
    @Story("Health endpoint returns 200 with status ok")
    @Severity(SeverityLevel.BLOCKER)
    public void health_smoke_returns200() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test(groups = {"smoke"})
    @Story("Register valid MP3 file returns analysisUuid")
    @Severity(SeverityLevel.CRITICAL)
    public void registerFile_smoke_validMp3_returns200() throws Exception {
        when(analysisService.registerFile(any(), any())).thenReturn("uuid-smoke-mp3");

        MockMultipartFile file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "data".getBytes());
        mockMvc.perform(multipart("/register/file").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisUuid").value("uuid-smoke-mp3"));
    }

    @Test(groups = {"regression"})
    @Story("Register valid YouTube URL returns analysisUuid")
    @Severity(SeverityLevel.CRITICAL)
    public void registerUrl_regression_validYoutube_returns200() throws Exception {
        when(analysisService.registerUrl(any())).thenReturn("uuid-regression-yt");

        mockMvc.perform(post("/register/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://youtube.com/watch?v=dQw4w9WgXcQ\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisUuid").value("uuid-regression-yt"));
    }

    @Test(groups = {"regression"})
    @Story("Register valid TikTok URL returns analysisUuid")
    @Severity(SeverityLevel.NORMAL)
    public void registerUrl_regression_validTiktok_returns200() throws Exception {
        when(analysisService.registerUrl(any())).thenReturn("uuid-regression-tt");

        mockMvc.perform(post("/register/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://tiktok.com/@user/video/123456789\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisUuid").value("uuid-regression-tt"));
    }

    @Test(groups = {"regression"})
    @Story("Register MP4 file returns analysisUuid")
    @Severity(SeverityLevel.NORMAL)
    public void registerFile_regression_validMp4_returns200() throws Exception {
        when(analysisService.registerFile(any(), any())).thenReturn("uuid-regression-mp4");

        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "data".getBytes());
        mockMvc.perform(multipart("/register/file").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisUuid").value("uuid-regression-mp4"));
    }
}
