package io.github.mrlevi1112.report_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "damage_regions")
public class DamageRegion {

    @Id
    private String id;

    private String reportId;
    private String part;
    private Integer severity;
    private BoundingBox bbox;
    private Double confidence;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BoundingBox {
        private Double x;
        private Double y;
        private Double width;
        private Double height;
    }
}
