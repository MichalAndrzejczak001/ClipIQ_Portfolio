package com.clipiq.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);
    private static final int TIMEOUT_MINUTES = 10;
    private static final int LOG_TAIL_LINES = 20;

    public byte[] downloadFromUrl(String url) throws IOException, InterruptedException {
        Path tmpDir = Files.createTempDirectory("clipiq-");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "yt-dlp", "-x", "--audio-format", "mp3",
                    "-o", tmpDir.resolve("audio.%(ext)s").toString(),
                    url
            );
            if (!runProcess(pb, tmpDir.resolve("yt-dlp.log"), "yt-dlp")) {
                throw new RuntimeException("yt-dlp failed for URL: " + url);
            }
            return Files.readAllBytes(tmpDir.resolve("audio.mp3"));
        } finally {
            deleteDirectory(tmpDir.toFile());
        }
    }

    public byte[] convertMp4ToMp3(byte[] mp4Data) throws IOException, InterruptedException {
        Path input = Files.createTempFile("clipiq-in-", ".mp4");
        Path output = Files.createTempFile("clipiq-out-", ".mp3");
        Path processLog = Files.createTempFile("clipiq-ffmpeg-", ".log");
        try {
            Files.write(input, mp4Data);
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", input.toString(),
                    "-vn", "-acodec", "libmp3lame", "-y",
                    output.toString()
            );
            if (!runProcess(pb, processLog, "ffmpeg")) {
                throw new RuntimeException("ffmpeg conversion failed");
            }
            return Files.readAllBytes(output);
        } finally {
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
            Files.deleteIfExists(processLog);
        }
    }

    /**
     * Runs the process with stdout+stderr redirected to a file, so the pipe buffer can never
     * fill up and block the process. On failure the tail of that file is logged.
     */
    private boolean runProcess(ProcessBuilder pb, Path processLog, String name)
            throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        pb.redirectOutput(processLog.toFile());
        Process process = pb.start();
        boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            log.error("{} timed out after {} minutes. Output:\n{}", name, TIMEOUT_MINUTES, readTail(processLog));
            return false;
        }
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("{} exited with code {}. Output:\n{}", name, exitCode, readTail(processLog));
            return false;
        }
        return true;
    }

    private String readTail(Path file) {
        try {
            if (!Files.exists(file)) {
                return "<no output>";
            }
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            return String.join("\n", lines.subList(Math.max(0, lines.size() - LOG_TAIL_LINES), lines.size()));
        } catch (IOException e) {
            return "<could not read output: " + e.getMessage() + ">";
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteDirectory(file);
            }
        }
        if (!dir.delete()) {
            log.warn("Could not delete temporary file: {}", dir.getAbsolutePath());
        }
    }
}
