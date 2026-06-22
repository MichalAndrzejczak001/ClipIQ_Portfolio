package com.clipiq.service;

import com.clipiq.model.Analysis;
import com.clipiq.model.AuthorAttitude;
import com.clipiq.model.FileType;
import com.clipiq.model.Status;
import com.clipiq.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock private AnalysisRepository analysisRepository;
    @Mock private AiClientService aiClientService;
    @Mock private MediaService mediaService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void registerFile_shouldCreateAnalysisWithFileTypeRaw() {
        when(analysisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        analysisService.registerFile("audio.mp3", new byte[]{1, 2, 3});

        ArgumentCaptor<Analysis> captor = ArgumentCaptor.forClass(Analysis.class);
        verify(analysisRepository).save(captor.capture());
        Analysis saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("audio.mp3");
        assertThat(saved.getFileType()).isEqualTo(FileType.RAW);
        assertThat(saved.getRawFile()).isEqualTo(new byte[]{1, 2, 3});
        assertThat(saved.getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(saved.getUuid()).isNotNull();
    }

    @Test
    void registerUrl_youtube_shouldSetYoutubeFileType() {
        when(analysisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        analysisService.registerUrl("https://youtube.com/watch?v=abc");

        ArgumentCaptor<Analysis> captor = ArgumentCaptor.forClass(Analysis.class);
        verify(analysisRepository).save(captor.capture());
        assertThat(captor.getValue().getFileType()).isEqualTo(FileType.YOUTUBE);
    }

    @Test
    void registerUrl_tiktok_shouldSetTiktokFileType() {
        when(analysisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        analysisService.registerUrl("https://tiktok.com/@user/video/123");

        ArgumentCaptor<Analysis> captor = ArgumentCaptor.forClass(Analysis.class);
        verify(analysisRepository).save(captor.capture());
        assertThat(captor.getValue().getFileType()).isEqualTo(FileType.TIKTOK);
    }

    @Test
    void process_mp3Upload_success_shouldSetStatusSuccess() {
        String uuid = "test-uuid-123";
        Analysis analysis = new Analysis();
        analysis.setUuid(uuid);
        analysis.setRawFile(new byte[]{1, 2, 3});

        when(analysisRepository.findByUuid(uuid)).thenReturn(Optional.of(analysis));
        when(aiClientService.transcribe(any(), any())).thenReturn("transcript text");
        when(aiClientService.summarize("transcript text")).thenReturn("summary text");
        when(aiClientService.getSentiment("transcript text")).thenReturn("positive");
        when(analysisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        analysisService.process(uuid);

        ArgumentCaptor<Analysis> captor = ArgumentCaptor.forClass(Analysis.class);
        verify(analysisRepository).save(captor.capture());
        Analysis saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(saved.getFullTranscription()).isEqualTo("transcript text");
        assertThat(saved.getVideoSummary()).isEqualTo("summary text");
        assertThat(saved.getAuthorAttitude()).isEqualTo(AuthorAttitude.positive);
        assertThat(saved.getRawFile()).isNull();
        assertThat(saved.getFinishDate()).isNotNull();
    }

    @Test
    void process_aiServiceThrows_shouldSetStatusFailed() {
        String uuid = "test-uuid-456";
        Analysis analysis = new Analysis();
        analysis.setUuid(uuid);
        analysis.setRawFile(new byte[]{1, 2, 3});

        when(analysisRepository.findByUuid(uuid)).thenReturn(Optional.of(analysis));
        when(aiClientService.transcribe(any(), any())).thenThrow(new RuntimeException("AI unavailable"));
        when(analysisRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        analysisService.process(uuid);

        ArgumentCaptor<Analysis> captor = ArgumentCaptor.forClass(Analysis.class);
        verify(analysisRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.FAILED);
        assertThat(captor.getValue().getFinishDate()).isNotNull();
    }

    @Test
    void process_whenSavingFailedStatusThrows_stillSendsFailedMessage() {
        String uuid = "test-uuid-789";
        Analysis analysis = new Analysis();
        analysis.setUuid(uuid);
        analysis.setRawFile(new byte[]{1, 2, 3});

        when(analysisRepository.findByUuid(uuid)).thenReturn(Optional.of(analysis));
        when(aiClientService.transcribe(any(), any())).thenThrow(new RuntimeException("AI unavailable"));
        when(analysisRepository.save(any())).thenThrow(new RuntimeException("Mongo unavailable"));

        analysisService.process(uuid);

        verify(messagingTemplate).convertAndSend(eq("/topic/analysis/" + uuid + "/failed"), anyString());
    }

    @Test
    void process_unknownUuid_sendsFailedWithoutSaving() {
        String uuid = "does-not-exist";
        when(analysisRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        analysisService.process(uuid);

        verify(analysisRepository, never()).save(any());
        verify(messagingTemplate).convertAndSend(eq("/topic/analysis/" + uuid + "/failed"), anyString());
    }
}
