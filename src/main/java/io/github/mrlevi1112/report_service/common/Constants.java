package io.github.mrlevi1112.report_service.common;

public class Constants {
    private Constants() {}

    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_ASSESSED = "ASSESSED";
    public static final String BEARER_PREFIX = "Bearer ";

    public static class DamageAssessment {
        private DamageAssessment() {}

        public static final int DEFAULT_SEVERITY = 3;
        public static final int TOTAL_LOSS_SEVERITY_THRESHOLD = 5;
        public static final String DEFAULT_CAR_SEGMENT = "Family";
    }

    public static class MlApi {
        private MlApi() {}

        public static final String FIELD_FILE = "file";
        public static final String FIELD_SEVERITY = "severity";
        public static final String FIELD_CAR_SEGMENT = "carSegment";
        public static final String ENDPOINT_ASSESS = "/assess";
        public static final String DEFAULT_FILENAME = "image";
    }
}
