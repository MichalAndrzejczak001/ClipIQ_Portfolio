package com.clipiq.api;

import com.clipiq.dto.AnalysisResponse;
import com.clipiq.dto.RegisterResponse;
import com.clipiq.model.Analysis;
import com.clipiq.repository.AnalysisRepository;
import com.clipiq.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AnalysisController {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("mp3", "mp4");
    private static final Pattern ALLOWED_URL_PATTERN = Pattern.compile(
            "^https://(www\\.)?(youtube\\.com|youtu\\.be|tiktok\\.com)/[A-Za-z0-9/_.\\-?=&#@%+~!$'()*,;:]+$",
            Pattern.CASE_INSENSITIVE
    );

    private final AnalysisService analysisService;
    private final AnalysisRepository analysisRepository;

    @PostMapping("/register/file")
    public ResponseEntity<RegisterResponse> registerFile(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (!ALLOWED_EXTENSIONS.contains(getExtension(filename))) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String uuid = analysisService.registerFile(filename, file.getBytes());
            return ResponseEntity.ok(new RegisterResponse(uuid));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/register/url")
    public ResponseEntity<RegisterResponse> registerUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (!ALLOWED_URL_PATTERN.matcher(url).matches() || url.contains("..")) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String uuid = analysisService.registerUrl(url);
            return ResponseEntity.ok(new RegisterResponse(uuid));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analyse/{uuid}")
    public ResponseEntity<AnalysisResponse> getAnalysis(@PathVariable String uuid) {
        return analysisRepository.findByUuid(uuid)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/analyse")
    public ResponseEntity<List<AnalysisResponse>> getAnalyses(@RequestParam String uuids) {
        List<String> uuidList = Arrays.stream(uuids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (uuidList.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<AnalysisResponse> responses = analysisRepository.findAllByUuidIn(uuidList)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }

    private AnalysisResponse toResponse(Analysis analysis) {
        return new AnalysisResponse(
                analysis.getUuid(),
                analysis.getName(),
                analysis.getStartDate(),
                analysis.getFinishDate(),
                analysis.getStatus(),
                analysis.getFileType(),
                analysis.getLink(),
                analysis.getFullTranscription(),
                analysis.getVideoSummary(),
                analysis.getAuthorAttitude()
        );
    }
}
