package io.github.mrlevi1112.report_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PythonAssessmentResponse {
    private String damageType;
    private Double confidence;
    private String part;
    private Integer severity;
    private String carSegment;
    private Integer estimatedCost;
    private String currency;
}
