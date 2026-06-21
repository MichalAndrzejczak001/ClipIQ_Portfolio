package com.clipiq.websocket;

import com.clipiq.model.Analysis;
import com.clipiq.model.Status;
import com.clipiq.repository.AnalysisRepository;
import com.clipiq.service.AiClientService;
import com.clipiq.service.MediaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class AnalysisMessageControllerTest {

    @Autowired
    private AnalysisMessageController controller;

    @MockBean
    private AnalysisRepository analysisRepository;

    @MockBean
    private AiClientService aiClientService;

    @MockBean
    private MediaService mediaService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void analyse_runsOnAsyncExecutorThread_andEventuallyMarksSuccess() {
        String uuid = "async-uuid-1";
        Analysis analysis = new Analysis();
        analysis.setUuid(uuid);
        analysis.setRawFile(new byte[]{1, 2, 3});

        when(analysisRepository.findByUuid(uuid)).thenReturn(Optional.of(analysis));
        when(aiClientService.transcribe(any(), any())).thenReturn("transcript");
        when(aiClientService.summarize("transcript")).thenReturn("summary");
        when(aiClientService.getSentiment("transcript")).thenReturn("positive");
        when(analysisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AtomicReference<String> processingThreadName = new AtomicReference<>();
        doAnswer(invocation -> {
            processingThreadName.set(Thread.currentThread().getName());
            return null;
        }).when(messagingTemplate).convertAndSend(eq("/topic/analysis/" + uuid + "/done"), anyString());

        String callerThreadName = Thread.currentThread().getName();
        controller.analyse(uuid);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(processingThreadName.get()).isNotNull());

        assertThat(processingThreadName.get())
                .as("processing should run on the AsyncConfig thread pool, not the caller thread")
                .startsWith("analysis-")
                .isNotEqualTo(callerThreadName);

        verify(analysisRepository, timeout(5000))
                .save(argThat(a -> a.getStatus() == Status.SUCCESS));
    }
}
