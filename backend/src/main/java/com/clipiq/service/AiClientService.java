package com.clipiq.service;

import com.clipiq.dto.SentimentRequest;
import com.clipiq.dto.SentimentResponse;
import com.clipiq.dto.SummarizeRequest;
import com.clipiq.dto.SummarizeResponse;
import com.clipiq.dto.TranscribeResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiClientService {

    private final WebClient webClient;

    public AiClientService(@Qualifier("aiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String transcribe(byte[] audio, String filename) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return filename;
            }
        }).contentType(MediaType.parseMediaType("audio/mpeg"));

        return webClient.post()
                .uri("/transcribe")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("AI transcribe error: " + body))
                )
                .bodyToMono(TranscribeResponse.class)
                .map(TranscribeResponse::getTranscription)
                .block();
    }

    public String summarize(String text) {
        return webClient.post()
                .uri("/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SummarizeRequest(text))
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("AI summarize error: " + body))
                )
                .bodyToMono(SummarizeResponse.class)
                .map(SummarizeResponse::getSummary)
                .block();
    }

    public String getSentiment(String text) {
        return webClient.post()
                .uri("/sentiment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SentimentRequest(text))
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("AI sentiment error: " + body))
                )
                .bodyToMono(SentimentResponse.class)
                .map(SentimentResponse::getSentiment)
                .block();
    }
}
