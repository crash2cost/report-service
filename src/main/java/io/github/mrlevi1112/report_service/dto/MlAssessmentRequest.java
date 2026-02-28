package io.github.mrlevi1112.report_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlAssessmentRequest {
    @NotBlank(message = "Image ID is required")
    private String imageId;
    private Integer severity;
    private String carSegment;
}
