package io.github.mrlevi1112.report_service.repository;

import io.github.mrlevi1112.report_service.common.ReportStatus;
import io.github.mrlevi1112.report_service.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findByUsername(String username);

    Page<Report> findByUsernameAndStatusAndImageIdNotNull(String username, ReportStatus status, Pageable pageable);

    Page<Report> findByStatusAndImageIdNotNull(ReportStatus status, Pageable pageable);
}