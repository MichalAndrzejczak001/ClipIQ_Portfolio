package com.clipiq.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);
    private static final int TIMEOUT_MINUTES = 10;

    public byte[] downloadFromUrl(String url) throws IOException, InterruptedException {
        Path tmpDir = Files.createTempDirectory("clipiq-");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "yt-dlp", "-x", "--audio-format", "mp3",
                    "-o", tmpDir.resolve("audio.%(ext)s").toString(),
                    url
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished || process.exitValue() != 0) {
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
        try {
            Files.write(input, mp4Data);
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", input.toString(),
                    "-vn", "-acodec", "libmp3lame", "-y",
                    output.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished || process.exitValue() != 0) {
                throw new RuntimeException("ffmpeg conversion failed");
            }
            return Files.readAllBytes(output);
        } finally {
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
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
