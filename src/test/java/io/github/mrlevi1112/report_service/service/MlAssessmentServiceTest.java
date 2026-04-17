package test.java.io.github.mrlevi1112.report_service.service;

import main.java.io.github.mrlevi1112.report_service.common.AssessmentSource;
import io.github.mrlevi1112.report_service.dto.MlAssessmentRequest;
import io.github.mrlevi1112.report_service.model.Report;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MlAssessmentServiceTest {

    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private MlAssessmentService mlAssessmentService;

    @Test
    void assessDamage_usesFallbackWhenMlUnavailable() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("damage.png").build());
        ResponseEntity<byte[]> imageResponse = ResponseEntity.ok()
                .headers(headers)
                .body(new byte[]{1, 2, 3});

        when(restTemplate.exchange(
                eq("http://localhost:8002/api/images/img-1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(imageResponse);

        when(reportService.saveDamageAssessmentReport(any(Report.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(mlAssessmentService, "authServiceUrl", "http://localhost:8002");
        ReflectionTestUtils.setField(mlAssessmentService, "mlServiceUrl", "http://127.0.0.1:1");

        MlAssessmentRequest request = MlAssessmentRequest.builder()
                .imageId("img-1")
                .carSegment("sedan")
                .build();

        Report result = mlAssessmentService.assessDamage("Bearer token", request, "tester");

        assertNotNull(result);
        assertEquals(AssessmentSource.FALLBACK, result.getAssessmentSource());
        assertNotNull(result.getFallbackReason());
        assertFalse(result.getFallbackReason().isBlank());

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportService).saveDamageAssessmentReport(captor.capture());
        assertEquals(AssessmentSource.FALLBACK, captor.getValue().getAssessmentSource());
    }

    @Test
    void assessDamage_requiresImageId() {
        MlAssessmentRequest request = MlAssessmentRequest.builder()
                .imageId(" ")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> mlAssessmentService.assessDamage("Bearer token", request, "tester"));
    }
}
