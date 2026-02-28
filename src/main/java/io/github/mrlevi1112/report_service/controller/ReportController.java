package io.github.mrlevi1112.report_service.controller;

import io.github.mrlevi1112.report_service.dto.CreateReportDTO;
import io.github.mrlevi1112.report_service.dto.DamageAssessmentReportDTO;
import io.github.mrlevi1112.report_service.dto.MlAssessmentRequest;
import io.github.mrlevi1112.report_service.model.Report;
import io.github.mrlevi1112.report_service.service.MlAssessmentService;
import io.github.mrlevi1112.report_service.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final MlAssessmentService mlAssessmentService;

    @PostMapping
    public ResponseEntity<Report> createReport(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody CreateReportDTO dto
    ) {
        dto.setUsername(username);
        return ResponseEntity.ok(reportService.createReport(dto));
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<Report>> getUserReports(
            @AuthenticationPrincipal String authenticatedUsername,
            @PathVariable String username
    ) {
        if (!authenticatedUsername.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return ResponseEntity.ok(reportService.getUserReports(username));
    }

    @PostMapping("/damage-assessment")
    public ResponseEntity<Report> createDamageAssessmentReport(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody DamageAssessmentReportDTO dto
    ) {
        return ResponseEntity.ok(reportService.createDamageAssessmentReport(dto, username));
    }

    @PostMapping("/ai-assessments")
    public ResponseEntity<Report> assessDamage(
            @RequestHeader("Authorization") String token,
            @AuthenticationPrincipal String username,
            @Valid @RequestBody MlAssessmentRequest request
    ) {
        return ResponseEntity.ok(mlAssessmentService.assessDamage(token, request, username));
    }

    @GetMapping("/damage-assessments")
    public ResponseEntity<Page<Report>> getUserDamageReports(
            @AuthenticationPrincipal String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(reportService.getUserDamageReports(username, pageable));
    }

    @GetMapping("/damage-assessments/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Report>> getAllDamageReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(reportService.getAllDamageReports(pageable));
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(
            @AuthenticationPrincipal String username,
            @PathVariable String reportId
    ) {
        reportService.deleteReport(reportId, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllReports(
            @AuthenticationPrincipal String username
    ) {
        reportService.deleteAllUserReports(username);
        return ResponseEntity.noContent().build();
    }
}
