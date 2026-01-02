package io.github.mrlevi1112.report_service.repository;

import io.github.mrlevi1112.report_service.model.DamageRegion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DamageRegionRepository extends MongoRepository<DamageRegion, String> {
    void deleteByReportId(String reportId);
}
