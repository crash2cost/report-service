package io.github.mrlevi1112.report_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", ex.getMessage(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(MlServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleMlUnavailable(MlServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "message", ex.getMessage(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
