package io.elastest.epm.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class DockerServiceStatus {
    private String statusMsg;
    private EngineStatus status;

    public DockerServiceStatus() {
        this.initToDefault();
    }

    public enum EngineStatus {
        NOT_INITIALIZED("Not initialized"), INITIALIZING(
                "Initializing"), PULLING(
                        "Pulling"), STARTING("Starting"), READY("Ready");

        private String value;

        EngineStatus(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static EngineStatus fromValue(String text) {
            for (EngineStatus b : EngineStatus.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String msg) {
        this.statusMsg = msg;
    }

    public EngineStatus getStatus() {
        return status;
    }

    public void setStatus(EngineStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DockerServiceStatus [statusMsg=" + statusMsg + ", status="
                + status + "]";
    }

    public void initToDefault() {
        this.setStatus(EngineStatus.NOT_INITIALIZED);
        this.setStatusMsg(status.value);
    }
}
