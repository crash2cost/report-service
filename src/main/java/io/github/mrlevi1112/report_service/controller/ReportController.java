package io.github.mrlevi1112.report_service.controller;

import io.github.mrlevi1112.report_service.common.Constants;
import io.github.mrlevi1112.report_service.dto.CreateReportDTO;
import io.github.mrlevi1112.report_service.model.Report;
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
}