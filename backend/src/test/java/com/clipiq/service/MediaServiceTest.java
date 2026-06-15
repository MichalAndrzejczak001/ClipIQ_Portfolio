package com.clipiq.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class MediaServiceTest {

    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService();
    }

    @Test
    void downloadFromUrl_processExitsNonZero_throwsRuntimeException() throws Exception {
        Process mockProcess = mock(Process.class);
        when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mockProcess.exitValue()).thenReturn(1);

        try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class, (mock, ctx) -> {
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
            when(mock.start()).thenReturn(mockProcess);
        })) {
            assertThatThrownBy(() -> mediaService.downloadFromUrl("https://youtube.com/watch?v=test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("yt-dlp failed");
        }
    }

    @Test
    void downloadFromUrl_processTimesOut_throwsRuntimeException() throws Exception {
        Process mockProcess = mock(Process.class);
        when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(false);

        try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class, (mock, ctx) -> {
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
            when(mock.start()).thenReturn(mockProcess);
        })) {
            assertThatThrownBy(() -> mediaService.downloadFromUrl("https://youtube.com/watch?v=test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("yt-dlp failed");
        }
    }

    @Test
    void convertMp4ToMp3_processExitsNonZero_throwsRuntimeException() throws Exception {
        Process mockProcess = mock(Process.class);
        when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mockProcess.exitValue()).thenReturn(1);

        try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class, (mock, ctx) -> {
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
            when(mock.start()).thenReturn(mockProcess);
        })) {
            assertThatThrownBy(() -> mediaService.convertMp4ToMp3(new byte[]{0, 0, 0, 0, 'f', 't', 'y', 'p', 0}))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ffmpeg");
        }
    }

    @Test
    void convertMp4ToMp3_processTimesOut_throwsRuntimeException() throws Exception {
        Process mockProcess = mock(Process.class);
        when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(false);

        try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class, (mock, ctx) -> {
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
            when(mock.start()).thenReturn(mockProcess);
        })) {
            assertThatThrownBy(() -> mediaService.convertMp4ToMp3(new byte[]{0, 0, 0, 0, 'f', 't', 'y', 'p', 0}))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ffmpeg");
        }
    }
}
