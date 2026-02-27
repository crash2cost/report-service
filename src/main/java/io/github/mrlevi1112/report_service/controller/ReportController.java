package io.github.mrlevi1112.report_service.controller;

import io.github.mrlevi1112.report_service.common.Constants;
import io.github.mrlevi1112.report_service.dto.CreateReportDTO;
import io.github.mrlevi1112.report_service.dto.MlAssessmentRequest;
import io.github.mrlevi1112.report_service.model.Report;
import io.github.mrlevi1112.report_service.service.MlAssessmentService;
import io.github.mrlevi1112.report_service.service.ReportService;
import io.github.mrlevi1112.report_service.util.JwtUtil; 
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<List<Report>> getUserReports(
            @RequestHeader("Authorization") String token,
            @PathVariable String username
    ) {
        String actualToken = token.replace(Constants.BEARER_PREFIX, "");
        String tokenUsername = jwtUtil.extractUsername(actualToken);
        if (!username.equals(tokenUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
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
    public ResponseEntity<Report> assessDamage(
            @RequestHeader("Authorization") String token,
            @RequestBody MlAssessmentRequest request
    ) {
        String actualToken = token.replace(Constants.BEARER_PREFIX, "");
        String username = jwtUtil.extractUsername(actualToken);
        return ResponseEntity.ok(mlAssessmentService.assessDamage(token, request, username));
    }

    @GetMapping("/damage-assessments")
    public ResponseEntity<List<Report>> getUserDamageReports(
            @RequestHeader("Authorization") String token
    ) {
        String actualToken = token.replace(Constants.BEARER_PREFIX, "");
        String username = jwtUtil.extractUsername(actualToken);
        return ResponseEntity.ok(reportService.getUserDamageReports(username));
    }
    
    @GetMapping("/damage-assessments/all")
    public ResponseEntity<List<Report>> getAllDamageReports(
            @RequestHeader("Authorization") String token
    ) {
        String actualToken = token.replace(Constants.BEARER_PREFIX, "");
        String role = jwtUtil.extractRole(actualToken);
        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return ResponseEntity.ok(reportService.getAllDamageReports());
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(
            @RequestHeader("Authorization") String token,
            @PathVariable String reportId
    ) {
        String actualToken = token.replace(Constants.BEARER_PREFIX, "");
        String username = jwtUtil.extractUsername(actualToken);
        Report report = reportService.getReportById(reportId);
        if (report == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found");
        }
        if (!username.equals(report.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
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
