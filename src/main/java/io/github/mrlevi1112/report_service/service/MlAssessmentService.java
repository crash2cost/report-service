package io.github.mrlevi1112.report_service.service;

import io.github.mrlevi1112.report_service.common.Constants;
import io.github.mrlevi1112.report_service.dto.MlAssessmentRequest;
import io.github.mrlevi1112.report_service.dto.PythonAssessmentResponse;
import io.github.mrlevi1112.report_service.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MlAssessmentService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ReportService reportService;

    @Value("${auth.service.url:http://localhost:8001}")
    private String authServiceUrl;

    @Value("${ml.service.url:http://localhost:8004}")
    private String mlServiceUrl;

    public Report assessDamage(String authHeader, MlAssessmentRequest request, String username) {
        if (request == null || request.getImageId() == null || request.getImageId().isBlank()) {
            throw new IllegalArgumentException("imageId is required");
        }
        if (!request.getImageId().matches("^[a-fA-F0-9]{24}$")) {
            throw new IllegalArgumentException("Invalid imageId format");
        }

        int severity = Optional.ofNullable(request.getSeverity()).orElse(Constants.DamageAssessment.DEFAULT_SEVERITY);
        String carSegment = Optional.ofNullable(request.getCarSegment()).orElse(Constants.DamageAssessment.DEFAULT_CAR_SEGMENT);

        ResponseEntity<byte[]> imageResponse = fetchImage(authHeader, request.getImageId());
        PythonAssessmentResponse pythonResponse = callMlService(
                imageResponse,
                severity,
                carSegment
        );

        Double estimatedCost = pythonResponse.getEstimatedCost() == null
                ? null
                : pythonResponse.getEstimatedCost().doubleValue();

        Report.DamageArea damageArea = Report.DamageArea.builder()
                .area(pythonResponse.getPart())
                .severity(pythonResponse.getSeverity())
                .cost(estimatedCost)
                .description("Detected " + pythonResponse.getDamageType())
                .build();

        boolean totalLoss = pythonResponse.getSeverity() != null
                && pythonResponse.getSeverity() >= Constants.DamageAssessment.TOTAL_LOSS_SEVERITY_THRESHOLD;

        Report report = Report.builder()
                .username(username)
                .imageId(request.getImageId())
                .damageAreas(List.of(damageArea))
                .totalCost(estimatedCost)
                .totalLoss(totalLoss)
                .assessmentDate(LocalDateTime.now())
                .status(Constants.STATUS_ASSESSED)
                .build();

        return reportService.createDamageAssessmentReport(report);
    }

    private ResponseEntity<byte[]> fetchImage(String authHeader, String imageId) {
        String imageUrl = authServiceUrl + "/api/images/" + imageId;
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(imageUrl, HttpMethod.GET, request, byte[].class);
    }

    private PythonAssessmentResponse callMlService(
            ResponseEntity<byte[]> imageResponse,
            int severity,
            String carSegment
    ) {
        byte[] imageBytes = imageResponse.getBody();
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalStateException("Empty image content");
        }

        String filename = Constants.MlApi.DEFAULT_FILENAME;
        ContentDisposition contentDisposition = imageResponse.getHeaders().getContentDisposition();
        if (contentDisposition != null && contentDisposition.getFilename() != null) {
            String rawFilename = contentDisposition.getFilename()
                    .replace("..", "")
                    .replace("/", "")
                    .replace("\\", "");
            if (!rawFilename.isBlank()) {
                filename = rawFilename;
            }
        }
        final String finalFilename = filename;

        MediaType contentType = imageResponse.getHeaders().getContentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return finalFilename;
            }
        };

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(contentType);
        HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(resource, fileHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(Constants.MlApi.FIELD_FILE, fileEntity);
        body.add(Constants.MlApi.FIELD_SEVERITY, String.valueOf(severity));
        body.add(Constants.MlApi.FIELD_CAR_SEGMENT, carSegment);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<PythonAssessmentResponse> response = restTemplate.postForEntity(
                mlServiceUrl + Constants.MlApi.ENDPOINT_ASSESS,
                request,
                PythonAssessmentResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("ML service response invalid");
        }

        return response.getBody();
    }
}
