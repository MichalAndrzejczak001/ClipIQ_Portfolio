package com.clipiq.repository;

import com.clipiq.model.Analysis;
import com.clipiq.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
class AnalysisRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @Autowired
    private AnalysisRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void findByUuid_existingUuid_returnsAnalysis() {
        Analysis analysis = new Analysis();
        analysis.setUuid("test-uuid-1");
        analysis.setStatus(Status.IN_PROGRESS);
        repository.save(analysis);

        Optional<Analysis> result = repository.findByUuid("test-uuid-1");

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo("test-uuid-1");
        assertThat(result.get().getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void findByUuid_unknownUuid_returnsEmpty() {
        Optional<Analysis> result = repository.findByUuid("does-not-exist");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByUuidIn_returnsMatchingAnalyses() {
        Analysis a1 = new Analysis();
        a1.setUuid("uuid-a");
        a1.setStatus(Status.SUCCESS);
        Analysis a2 = new Analysis();
        a2.setUuid("uuid-b");
        a2.setStatus(Status.FAILED);
        Analysis a3 = new Analysis();
        a3.setUuid("uuid-c");
        a3.setStatus(Status.IN_PROGRESS);
        repository.saveAll(List.of(a1, a2, a3));

        List<Analysis> result = repository.findAllByUuidIn(List.of("uuid-a", "uuid-b"));

        assertThat(result).hasSize(2)
                .extracting(Analysis::getUuid)
                .containsExactlyInAnyOrder("uuid-a", "uuid-b");
    }

    @Test
    void findAllByUuidIn_unknownUuids_returnsEmptyList() {
        List<Analysis> result = repository.findAllByUuidIn(List.of("non-existent-1", "non-existent-2"));

        assertThat(result).isEmpty();
    }
}
