package io.github.mrlevi1112.report_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateReportDTO {
    private String username;
    @NotBlank(message = "Description is required")
    private String description;
    private List<String> imageUrls;
}
