package com.clipiq.dto;

import com.clipiq.model.AuthorAttitude;
import com.clipiq.model.FileType;
import com.clipiq.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResponse {
    private String uuid;
    private String name;
    private Instant startDate;
    private Instant finishDate;
    private Status status;
    private FileType fileType;
    private String link;
    private String fullTranscription;
    private String videoSummary;
    private AuthorAttitude authorAttitude;
}
