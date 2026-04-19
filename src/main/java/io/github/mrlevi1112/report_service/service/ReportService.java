package io.github.mrlevi1112.report_service.service;

import io.github.mrlevi1112.report_service.common.ReportStatus;
import io.github.mrlevi1112.report_service.common.AssessmentSource;
import io.github.mrlevi1112.report_service.dto.CreateReportDTO;
import io.github.mrlevi1112.report_service.dto.DamageAssessmentReportDTO;
import io.github.mrlevi1112.report_service.model.DamageRegion;
import io.github.mrlevi1112.report_service.model.Report;
import io.github.mrlevi1112.report_service.repository.DamageRegionRepository;
import io.github.mrlevi1112.report_service.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final DamageRegionRepository damageRegionRepository;

    public Report createReport(CreateReportDTO dto) {
        Report report = Report.builder()
                .username(dto.getUsername())
                .description(dto.getDescription())
                .imageUrls(dto.getImageUrls())
                .eventDate(LocalDateTime.now())
                .status(ReportStatus.RECEIVED)
                .build();

        return reportRepository.save(report);
    }

    public List<Report> getUserReports(String username) {
        return reportRepository.findByUsername(username);
    }

    @Transactional
    public Report createDamageAssessmentReport(DamageAssessmentReportDTO dto, String username) {
        Report report = Report.builder()
                .username(username)
                .imageId(dto.getImageId())
                .totalCost(dto.getTotalCost())
                .totalLoss(dto.getTotalLoss())
                .eventDate(LocalDateTime.now())
                .assessmentDate(LocalDateTime.now())
                .status(ReportStatus.ASSESSED)
                .assessmentSource(AssessmentSource.MANUAL)
                .build();

        if (dto.getDamageAreas() != null) {
            report.setDamageAreas(dto.getDamageAreas().stream()
                    .map(da -> Report.DamageArea.builder()
                            .area(da.getArea())
                            .severity(da.getSeverity())
                            .cost(da.getCost())
                            .description(da.getDescription())
                            .build())
                    .toList());
        }

        final Report savedReport = reportRepository.save(report);

        if (dto.getDamageRegions() != null && !dto.getDamageRegions().isEmpty()) {
            List<DamageRegion> regions = dto.getDamageRegions().stream()
                    .map(dr -> DamageRegion.builder()
                            .reportId(savedReport.getId())
                            .part(dr.getPart())
                            .severity(dr.getSeverity())
                            .confidence(dr.getConfidence())
                            .bbox(dr.getBbox() != null ? DamageRegion.BoundingBox.builder()
                                    .x(dr.getBbox().getX())
                                    .y(dr.getBbox().getY())
                                    .width(dr.getBbox().getWidth())
                                    .height(dr.getBbox().getHeight())
                                    .build() : null)
                            .build())
                    .toList();
            List<DamageRegion> savedRegions = damageRegionRepository.saveAll(regions);
            savedReport.setDamageRegions(savedRegions);
            return reportRepository.save(savedReport);
        }

        return savedReport;
    }

    @Transactional
    Report saveDamageAssessmentReport(Report report) {
        return reportRepository.save(report);
    }

    public Page<Report> getUserDamageReports(String username, Pageable pageable) {
        return reportRepository.findByUsernameAndStatusAndImageIdNotNull(username, ReportStatus.ASSESSED, pageable);
    }

    public Page<Report> getAllDamageReports(Pageable pageable) {
        return reportRepository.findByStatusAndImageIdNotNull(ReportStatus.ASSESSED, pageable);
    }

    @Transactional
    public void deleteReport(String reportId, String username) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        if (!report.getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        damageRegionRepository.deleteByReportId(reportId);
        reportRepository.deleteById(reportId);
    }

    @Transactional
    public void deleteAllUserReports(String username) {
        List<Report> reports = reportRepository.findByUsername(username);
        List<String> reportIds = reports.stream().map(Report::getId).toList();
        damageRegionRepository.deleteByReportIdIn(reportIds);
        reportRepository.deleteAll(reports);
    }
}
