package io.github.mrlevi1112.report_service.dto;

import io.github.mrlevi1112.report_service.model.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlAssessmentResponse {
    private String imageId;
    private List<Report.DamageArea> damageAreas;
    private Double totalCost;
    private Boolean totalLoss;
    private LocalDateTime assessmentDate;
}
