package com.clipiq.service;

import com.clipiq.dto.SentimentRequest;
import com.clipiq.dto.SentimentResponse;
import com.clipiq.dto.SummarizeRequest;
import com.clipiq.dto.SummarizeResponse;
import com.clipiq.dto.TranscribeResponse;
import java.util.function.Function;
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

        TranscribeResponse response = webClient.post()
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
                .block();

        if (response == null || response.getTranscription() == null) {
            throw new RuntimeException("AI transcribe returned empty response");
        }
        return response.getTranscription();
    }

    public String summarize(String text) {
        return postJson("/summarize", new SummarizeRequest(text),
                SummarizeResponse.class, SummarizeResponse::getSummary, "summarize");
    }

    public String getSentiment(String text) {
        return postJson("/sentiment", new SentimentRequest(text),
                SentimentResponse.class, SentimentResponse::getSentiment, "sentiment");
    }

    private <B, R> String postJson(String uri, B requestBody, Class<R> responseType,
                                   Function<R, String> extractor, String operationName) {
        R response = webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("AI " + operationName + " error: " + body))
                )
                .bodyToMono(responseType)
                .block();

        if (response == null || extractor.apply(response) == null) {
            throw new RuntimeException("AI " + operationName + " returned empty response");
        }
        return extractor.apply(response);
    }
}
