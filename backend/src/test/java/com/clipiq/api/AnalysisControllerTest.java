package com.clipiq.api;

import com.clipiq.model.Analysis;
import com.clipiq.model.Status;
import com.clipiq.repository.AnalysisRepository;
import com.clipiq.service.AnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalysisControllerTest {

    private MockMvc mockMvc;

    @Mock private AnalysisService analysisService;
    @Mock private AnalysisRepository analysisRepository;

    @InjectMocks
    private AnalysisController analysisController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analysisController).build();
    }

    @Test
    void registerFile_mp3_returns200WithUuid() throws Exception {
        when(analysisService.registerFile(any(), any())).thenReturn("uuid-123");
        MockMultipartFile file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "data".getBytes());

        mockMvc.perform(multipart("/register/file").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisUuid").value("uuid-123"));
    }

    @Test
    void registerFile_mp4_returns200() throws Exception {
        when(analysisService.registerFile(any(), any())).thenReturn("uuid-456");
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "data".getBytes());

        mockMvc.perform(multipart("/register/file").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void registerFile_unsupportedExtension_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.avi", "video/x-msvideo", "data".getBytes());

        mockMvc.perform(multipart("/register/file").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUrl_validYoutubeUrl_returns200WithUuid() throws Exception {
        when(analysisService.registerUrl(any())).thenReturn("uuid-789");

        mockMvc.perform(post("/register/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://youtube.com/watch?v=dQw4w9WgXcQ\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisUuid").value("uuid-789"));
    }

    @Test
    void registerUrl_validTiktokUrl_returns200() throws Exception {
        when(analysisService.registerUrl(any())).thenReturn("uuid-tiktok");

        mockMvc.perform(post("/register/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://tiktok.com/@user/video/123456\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void registerUrl_invalidDomain_returns400() throws Exception {
        mockMvc.perform(post("/register/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://evil.com/malware\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUrl_pathTraversal_returns400() throws Exception {
        mockMvc.perform(post("/register/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://youtube.com/../etc/passwd\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAnalysis_found_returns200WithBody() throws Exception {
        Analysis analysis = new Analysis();
        analysis.setUuid("uuid-abc");
        analysis.setStatus(Status.SUCCESS);
        when(analysisRepository.findByUuid("uuid-abc")).thenReturn(Optional.of(analysis));

        mockMvc.perform(get("/analyse/uuid-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("uuid-abc"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getAnalysis_notFound_returns404() throws Exception {
        when(analysisRepository.findByUuid(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/analyse/unknown-uuid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAnalyses_validUuids_returns200() throws Exception {
        when(analysisRepository.findAllByUuidIn(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyse").param("uuids", "uuid1,uuid2"))
                .andExpect(status().isOk());
    }

    @Test
    void getAnalyses_blankUuids_returns400() throws Exception {
        mockMvc.perform(get("/analyse").param("uuids", " , "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAnalysis_existingUuid_returns204() throws Exception {
        when(analysisRepository.deleteByUuid("uuid-abc")).thenReturn(1L);

        mockMvc.perform(delete("/analyse/uuid-abc"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAnalysis_unknownUuid_returns404() throws Exception {
        when(analysisRepository.deleteByUuid("unknown-uuid")).thenReturn(0L);

        mockMvc.perform(delete("/analyse/unknown-uuid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void health_returns200WithStatusOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }
}
