package io.github.mrlevi1112.report_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "reports") 
public class Report {

    @Id
    private String id; 

    private String username; 
    
    private LocalDateTime eventDate;
    private String description;
    
    private List<String> imageUrls;
    
    private String status;
    
    private Double estimatedCost;

    // Damage Assessment fields
    private String imageId;
    private List<DamageArea> damageAreas;
    @DBRef
    private List<DamageRegion> damageRegions;
    private Double totalCost;
    private Boolean totalLoss;
    private LocalDateTime assessmentDate;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DamageArea {
        private String area;
        private Integer severity;
        private Double cost;
        private String description;
    }
}
