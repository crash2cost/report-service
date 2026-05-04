package io.github.mrlevi1112.report_service.common;

public class Constants {
    private Constants() {}

    public static class DamageAssessment {
        private DamageAssessment() {}

        public static final int TOTAL_LOSS_SEVERITY_THRESHOLD = 5;
        public static final double TOTAL_LOSS_MIN_COST_ILS = 12000.0;
        public static final String DEFAULT_CAR_SEGMENT = "Family";
    }

    public static class MlApi {
        private MlApi() {}

        public static final String FIELD_FILE = "file";
        public static final String ENDPOINT_ASSESS = "/assess";
        public static final String DEFAULT_FILENAME = "image";
    }
}
