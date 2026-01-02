package io.github.mrlevi1112.report_service.service;

import io.github.mrlevi1112.report_service.dto.CreateReportDTO;
import io.github.mrlevi1112.report_service.model.DamageRegion;
import io.github.mrlevi1112.report_service.model.Report;
import io.github.mrlevi1112.report_service.repository.DamageRegionRepository;
import io.github.mrlevi1112.report_service.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                .status("RECEIVED") 
                .build();

        return reportRepository.save(report);
    }

    public List<Report> getUserReports(String username) {
        return reportRepository.findByUsername(username);
    }

    public Report createDamageAssessmentReport(Report report) {
        List<DamageRegion> damageRegions = report.getDamageRegions();
        report.setDamageRegions(null);
        report.setEventDate(LocalDateTime.now());
        report.setAssessmentDate(LocalDateTime.now());
        report.setStatus("ASSESSED");
        Report savedReport = reportRepository.save(report);
        if (damageRegions != null && !damageRegions.isEmpty()) {
            for (DamageRegion region : damageRegions) {
                region.setReportId(savedReport.getId());
            }
            damageRegions = damageRegionRepository.saveAll(damageRegions);
            savedReport.setDamageRegions(damageRegions);
            savedReport = reportRepository.save(savedReport);
        }
        return savedReport;
    }

    public List<Report> getUserDamageReports(String username) {
        return reportRepository.findByUsername(username);
    }

    public void deleteReport(String reportId) {
        damageRegionRepository.deleteByReportId(reportId);
        reportRepository.deleteById(reportId);
    }

    public void deleteAllUserReports(String username) {
        List<Report> reports = reportRepository.findByUsername(username);
        for (Report report : reports) {
            damageRegionRepository.deleteByReportId(report.getId());
        }
        reportRepository.deleteAll(reports);
    }
}
