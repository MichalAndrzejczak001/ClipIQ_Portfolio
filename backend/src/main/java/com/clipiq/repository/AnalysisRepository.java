package com.clipiq.repository;

import com.clipiq.model.Analysis;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends MongoRepository<Analysis, String> {

    Optional<Analysis> findByUuid(String uuid);

    List<Analysis> findAllByUuidIn(List<String> uuids);

    boolean existsByUuid(String uuid);

    void deleteByUuid(String uuid);
}
