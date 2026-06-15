package com.clipiq.cucumber;

import com.clipiq.repository.AnalysisRepository;
import com.clipiq.service.AnalysisService;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {

    @MockBean
    AnalysisService analysisService;

    @MockBean
    AnalysisRepository analysisRepository;
}
