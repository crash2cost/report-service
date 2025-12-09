package io.github.mrlevi1112.report_service.service;

import io.github.mrlevi1112.report_service.dto.CreateReportDTO;
import io.github.mrlevi1112.report_service.model.Report;
import io.github.mrlevi1112.report_service.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

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
}