package com.clipiq.service;

import com.clipiq.model.Analysis;
import com.clipiq.model.AuthorAttitude;
import com.clipiq.model.FileType;
import com.clipiq.model.Status;
import com.clipiq.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final AnalysisRepository analysisRepository;
    private final AiClientService aiClientService;
    private final MediaService mediaService;
    private final SimpMessagingTemplate messagingTemplate;

    public String registerFile(String filename, byte[] fileBytes) {
        return createAndSave(analysis -> {
            analysis.setName(filename);
            analysis.setFileType(FileType.RAW);
            analysis.setRawFile(fileBytes);
        });
    }

    public String registerUrl(String url) {
        FileType fileType = url.contains("tiktok.com") ? FileType.TIKTOK : FileType.YOUTUBE;
        return createAndSave(analysis -> {
            analysis.setName(url);
            analysis.setFileType(fileType);
            analysis.setLink(url);
        });
    }

    private String createAndSave(Consumer<Analysis> initializer) {
        Analysis analysis = new Analysis();
        analysis.setUuid(UUID.randomUUID().toString());
        analysis.setStartDate(Instant.now());
        analysis.setStatus(Status.IN_PROGRESS);
        initializer.accept(analysis);
        analysisRepository.save(analysis);
        return analysis.getUuid();
    }

    @Async
    public void process(String uuid) {
        Analysis analysis = analysisRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Analysis not found: " + uuid));
        try {
            sendProgress(uuid, "0");

            byte[] audioBytes = resolveAudio(analysis);
            sendProgress(uuid, "20");

            String transcription = aiClientService.transcribe(audioBytes, "audio.mp3");
            analysis.setFullTranscription(transcription);
            analysis.setRawFile(null);
            sendProgress(uuid, "40");

            String summary = aiClientService.summarize(transcription);
            analysis.setVideoSummary(summary);
            sendProgress(uuid, "60");

            String sentiment = aiClientService.getSentiment(transcription);
            analysis.setAuthorAttitude(AuthorAttitude.valueOf(sentiment));
            sendProgress(uuid, "80");

            analysis.setStatus(Status.SUCCESS);
            analysis.setFinishDate(Instant.now());
            analysisRepository.save(analysis);
            sendProgress(uuid, "100");

            messagingTemplate.convertAndSend("/topic/analysis/" + uuid + "/done", "");

        } catch (Exception e) {
            log.error("Analysis {} failed: {}", uuid, e.getMessage(), e);
            analysis.setStatus(Status.FAILED);
            analysis.setFinishDate(Instant.now());
            analysisRepository.save(analysis);
            messagingTemplate.convertAndSend(
                    "/topic/analysis/" + uuid + "/failed",
                    "Przetwarzanie nie powiodło się. Spróbuj ponownie."
            );
        }
    }

    private byte[] resolveAudio(Analysis analysis) throws Exception {
        if (analysis.getLink() != null) {
            return mediaService.downloadFromUrl(analysis.getLink());
        }
        byte[] raw = analysis.getRawFile();
        if (isMp4(raw)) {
            return mediaService.convertMp4ToMp3(raw);
        }
        return raw;
    }

    private boolean isMp4(byte[] bytes) {
        return bytes != null && bytes.length > 8
                && bytes[4] == 'f' && bytes[5] == 't'
                && bytes[6] == 'y' && bytes[7] == 'p';
    }

    private void sendProgress(String uuid, String value) {
        messagingTemplate.convertAndSend("/topic/analysis/" + uuid + "/progress", value);
    }
}
