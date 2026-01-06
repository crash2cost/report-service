package io.github.mrlevi1112.report_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlAssessmentRequest {
    private String imageId;
    private Integer severity;
    private String carSegment;
}
