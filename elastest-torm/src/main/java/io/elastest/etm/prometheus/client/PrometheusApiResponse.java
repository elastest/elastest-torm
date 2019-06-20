package io.elastest.etm.prometheus.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class PrometheusApiResponse<T> {

    private String status;
    private T data;

    // Only set if status is "error". The data field may still hold
    // additional data.
    private String errorType;
    private String error;

    // Only if there were warnings while executing the request.
    // There will still be data in the data field.
    private String warnings;

    public PrometheusApiResponse() {
    }

    public PrometheusApiResponse(String status, T data) {
        super();
        this.status = status;
        this.data = data;
    }

    public PrometheusApiResponse(String status, T data, String errorType,
            String error, String warnings) {
        this(status, data);
        this.errorType = errorType;
        this.error = error;
        this.warnings = warnings;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    @Override
    public String toString() {
        return "PrometheusApiResponse [status=" + status + ", data=" + data
                + ", errorType=" + errorType + ", error=" + error
                + ", warnings=" + warnings + "]";
    }

    public enum PrometheusApiResponseStatus {
        SUCCESS("success");

        private String value;

        PrometheusApiResponseStatus(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static PrometheusApiResponseStatus fromValue(String text) {
            for (PrometheusApiResponseStatus b : PrometheusApiResponseStatus
                    .values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

    }
}
