package io.github.mrlevi1112.report_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DamageAssessmentReportDTO {

    @NotBlank(message = "Image ID is required")
    private String imageId;

    @Valid
    @Size(max = 50, message = "Damage areas cannot exceed 50 entries")
    private List<DamageAreaDTO> damageAreas;

    @Size(max = 50, message = "Damage regions cannot exceed 50 entries")
    private List<DamageRegionDTO> damageRegions;

    @NotNull(message = "Total cost is required")
    @PositiveOrZero(message = "Total cost must be zero or positive")
    private Double totalCost;

    private Boolean totalLoss;

    @Data
    public static class DamageAreaDTO {
        private String area;
        private Integer severity;
        private Double cost;
        private String description;
    }

    @Data
    public static class DamageRegionDTO {
        private String part;
        private Integer severity;
        private Double confidence;
        private BoundingBoxDTO bbox;
    }

    @Data
    public static class BoundingBoxDTO {
        private Double x;
        private Double y;
        private Double width;
        private Double height;
    }
}
