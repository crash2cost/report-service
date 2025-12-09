package io.github.mrlevi1112.report_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateReportDTO {
    private String username;
    private String description;
    private List<String> imageUrls; 
}