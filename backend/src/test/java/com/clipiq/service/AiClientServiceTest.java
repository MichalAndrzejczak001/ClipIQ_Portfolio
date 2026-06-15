package com.clipiq.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiClientServiceTest {

    private MockWebServer server;
    private AiClientService aiClientService;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .build();
        aiClientService = new AiClientService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void transcribe_success_returnsTranscription() {
        server.enqueue(new MockResponse()
                .setBody("{\"transcription\":\"hello world\"}")
                .addHeader("Content-Type", "application/json"));

        String result = aiClientService.transcribe(new byte[]{1, 2, 3}, "audio.mp3");

        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void transcribe_serverError_throwsException() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("Internal error"));

        assertThatThrownBy(() -> aiClientService.transcribe(new byte[]{1}, "audio.mp3"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void summarize_success_returnsSummary() {
        server.enqueue(new MockResponse()
                .setBody("{\"summary\":\"short summary\"}")
                .addHeader("Content-Type", "application/json"));

        String result = aiClientService.summarize("long transcript text");

        assertThat(result).isEqualTo("short summary");
    }

    @Test
    void summarize_serverError_throwsException() {
        server.enqueue(new MockResponse().setResponseCode(422).setBody("Unprocessable"));

        assertThatThrownBy(() -> aiClientService.summarize("text"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getSentiment_success_returnsSentiment() {
        server.enqueue(new MockResponse()
                .setBody("{\"sentiment\":\"positive\"}")
                .addHeader("Content-Type", "application/json"));

        String result = aiClientService.getSentiment("great content");

        assertThat(result).isEqualTo("positive");
    }

    @Test
    void getSentiment_serverError_throwsException() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("error"));

        assertThatThrownBy(() -> aiClientService.getSentiment("text"))
                .isInstanceOf(RuntimeException.class);
    }
}
