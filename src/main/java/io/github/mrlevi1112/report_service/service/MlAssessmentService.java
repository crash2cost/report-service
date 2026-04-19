package io.github.mrlevi1112.report_service.service;

import io.github.mrlevi1112.report_service.common.Constants;
import io.github.mrlevi1112.report_service.common.ReportStatus;
import io.github.mrlevi1112.report_service.common.AssessmentSource;
import io.github.mrlevi1112.report_service.dto.MlAssessmentRequest;
import io.github.mrlevi1112.report_service.dto.PythonAssessmentResponse;
import io.github.mrlevi1112.report_service.exception.MlServiceUnavailableException;
import io.github.mrlevi1112.report_service.model.Report;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MlAssessmentService {
    private final RestTemplate restTemplate;
    private final ReportService reportService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${auth.service.url:http://localhost:8002}")
    private String authServiceUrl;

    @Value("${ml.service.url:http://localhost:8004}")
    private String mlServiceUrl;

    private static final Map<String, String> CAR_SEGMENT_MAP = Map.of(
            "small", "Micro",
            "sedan", "Family",
            "family_suv", "SUV",
            "truck", "SUV",
            "minivan", "Family",
            "sports", "Executive",
            "luxury", "Luxury",
            "electric", "Executive"
    );

    public Report assessDamage(String authHeader, MlAssessmentRequest request, String username) {
        if (request == null || request.getImageId() == null || request.getImageId().isBlank()) {
            throw new IllegalArgumentException("imageId is required");
        }

        String carSegment = Optional.ofNullable(request.getCarSegment()).orElse(Constants.DamageAssessment.DEFAULT_CAR_SEGMENT);

        ResponseEntity<byte[]> imageResponse = fetchImage(authHeader, request.getImageId());
        PythonAssessmentResponse pythonResponse;
        AssessmentSource source = AssessmentSource.ML;
        String fallbackReason = null;
        try {
            pythonResponse = callMlService(imageResponse, carSegment);
        } catch (MlServiceUnavailableException e) {
            log.warn("Falling back to heuristic assessment for image {}: {}", request.getImageId(), e.getMessage());
            source = AssessmentSource.FALLBACK;
            fallbackReason = e.getMessage();
            pythonResponse = PythonAssessmentResponse.builder()
                    .damageType("suspected damage (fallback)")
                    .confidence(0.15)
                    .part("Unknown Area")
                    .severity(2)
                    .carSegment(mapCarSegment(carSegment))
                    .estimatedCost(1500)
                    .currency("ILS")
                    .build();
        }

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
                .eventDate(LocalDateTime.now())
                .assessmentDate(LocalDateTime.now())
                .status(ReportStatus.ASSESSED)
                .assessmentSource(source)
                .fallbackReason(fallbackReason)
                .build();

        return reportService.saveDamageAssessmentReport(report);
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
            String carSegment
    ) {
        byte[] imageBytes = imageResponse.getBody();
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalStateException("Empty image content");
        }

        String filename = Constants.MlApi.DEFAULT_FILENAME;
        ContentDisposition contentDisposition = imageResponse.getHeaders().getContentDisposition();
        if (contentDisposition != null && contentDisposition.getFilename() != null) {
            filename = contentDisposition.getFilename();
        }
        final String finalFilename = filename;

        String mlSegment = mapCarSegment(carSegment);
        String url = UriComponentsBuilder.fromHttpUrl(mlServiceUrl + Constants.MlApi.ENDPOINT_ASSESS)
                .queryParam("car_segment", mlSegment)
                .toUriString();

        try {
            String safeFilename = (finalFilename == null || finalFilename.isBlank()
                    ? Constants.MlApi.DEFAULT_FILENAME
                    : finalFilename).replace('"', '_');

            String boundary = "----Crash2CostBoundary" + System.currentTimeMillis();
            ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
            bodyStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            bodyStream.write(("Content-Disposition: form-data; name=\"" + Constants.MlApi.FIELD_FILE + "\"; filename=\"" + safeFilename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            bodyStream.write("Content-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            bodyStream.write(imageBytes);
            bodyStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
            bodyStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bodyStream.toByteArray()))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new MlServiceUnavailableException("ML service returned " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), PythonAssessmentResponse.class);
        } catch (RestClientException e) {
            log.error("ML service call failed: {}", e.getMessage());
            throw new MlServiceUnavailableException("ML service unavailable: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new MlServiceUnavailableException("ML service unavailable: " + e.getMessage(), e);
        }
    }

    private String mapCarSegment(String frontendSegment) {
        return CAR_SEGMENT_MAP.getOrDefault(frontendSegment, Constants.DamageAssessment.DEFAULT_CAR_SEGMENT);
    }
}
