package com.clipiq.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "analyses")
public class Analysis {

    @Id
    private String id;

    @Indexed(unique = true)
    private String uuid;

    private String name;
    private Instant startDate;
    private Instant finishDate;
    private Status status;
    private FileType fileType;
    private String link;
    private byte[] rawFile;
    private String fullTranscription;
    private String videoSummary;
    private AuthorAttitude authorAttitude;
}
