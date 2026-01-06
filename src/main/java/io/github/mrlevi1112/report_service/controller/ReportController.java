package io.github.mrlevi1112.report_service.controller;

import io.github.mrlevi1112.report_service.common.Constants;
import io.github.mrlevi1112.report_service.dto.CreateReportDTO;
import io.github.mrlevi1112.report_service.dto.MlAssessmentRequest;
import io.github.mrlevi1112.report_service.dto.MlAssessmentResponse;
import io.github.mrlevi1112.report_service.model.Report;
import io.github.mrlevi1112.report_service.service.MlAssessmentService;
import io.github.mrlevi1112.report_service.service.ReportService;
import io.github.mrlevi1112.report_service.util.JwtUtil; 
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final MlAssessmentService mlAssessmentService;
    private final JwtUtil jwtUtil; 

    @PostMapping
    public ResponseEntity<Report> createReport(
            @RequestHeader("Authorization") String token, 
            @RequestBody CreateReportDTO dto
    ) {
        String actualToken = token.replace(Constants.BEARER_PREFIX, "");
        
        String username = jwtUtil.extractUsername(actualToken);
        
        dto.setUsername(username);

        return ResponseEntity.ok(reportService.createReport(dto));
    }
    
    @GetMapping("/{username}")
    public ResponseEntity<List<Report>> getUserReports(@PathVariable String username) {
        return ResponseEntity.ok(reportService.getUserReports(username));
    }

    @PostMapping("/damage-assessment")
    public ResponseEntity<Report> createDamageAssessmentReport(
            @RequestHeader("Authorization") String token,
            @RequestBody Report report
    ) {
        String actualToken = token.replace(Constants.BEARER_PREFIX, "");
        String username = jwtUtil.extractUsername(actualToken);
        report.setUsername(username);
        return ResponseEntity.ok(reportService.createDamageAssessmentReport(report));
    }

    @PostMapping("/ai-assessments")
    public ResponseEntity<MlAssessmentResponse> assessDamage(
            @RequestHeader("Authorization") String token,
            @RequestBody MlAssessmentRequest request
    ) {
        return ResponseEntity.ok(mlAssessmentService.assessDamage(token, request));
    }

    @GetMapping("/damage-assessments")
    public ResponseEntity<List<Report>> getUserDamageReports(
            @RequestHeader("Authorization") String token
    ) {
        String actualToken = token.replace(Constants.BEARER_PREFIX, "");
        String username = jwtUtil.extractUsername(actualToken);
        return ResponseEntity.ok(reportService.getUserDamageReports(username));
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable String reportId
    ) {
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllReports(
            @RequestHeader("Authorization") String token
    ) {
        String actualToken = token.replace(Constants.BEARER_PREFIX, "");
        String username = jwtUtil.extractUsername(actualToken);
        reportService.deleteAllUserReports(username);
        return ResponseEntity.noContent().build();
    }
}
