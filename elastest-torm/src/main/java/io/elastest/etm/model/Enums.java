package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class Enums {

    public enum StreamType {
        LOG("log"),

        COMPOSED_METRICS("composed_metrics"),

        ATOMIC_METRIC("atomic_metric");

        private String value;

        StreamType(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static StreamType fromValue(String text) {
            for (StreamType b : StreamType.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum LevelEnum {
        ALERT("ALERT"),

        TRACE("TRACE"),

        DEBUG("DEBUG"),

        NOTICE("NOTICE"),

        INFO("INFO"),

        WARN("WARN"),

        WARNING("WARNING"),

        ERROR("ERROR"),

        CRITICAL("CRITICAL"),

        FATAL("FATAL"),

        SEVERE("SEVERE"),

        EMERGENCY("EMERGENCY");

        private String value;

        LevelEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static LevelEnum fromValue(String text) {
            for (LevelEnum b : LevelEnum.values()) {
                if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum ProtocolEnum {
        HTTP("http"),

        HTTPS("https");

        private String value;

        ProtocolEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ProtocolEnum fromValue(String text) {
            for (ProtocolEnum b : ProtocolEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        public boolean isHttp() {
            return ProtocolEnum.HTTP.equals(ProtocolEnum.fromValue(value));
        }

        public boolean isHttps() {
            return ProtocolEnum.HTTPS.equals(ProtocolEnum.fromValue(value));
        }
    }
}
