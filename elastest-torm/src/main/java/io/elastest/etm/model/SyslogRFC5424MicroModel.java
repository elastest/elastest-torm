package io.elastest.etm.model;

import java.util.Map;

public class SyslogRFC5424MicroModel {
    int facility;
    int severity;
    String severityText;
    String timestamp;
    String host;
    String appName;
    Long procid;
    String msgid;
    Integer version;
    String message;
    boolean decodeErrors;

    public SyslogRFC5424MicroModel() {
    }

    public SyslogRFC5424MicroModel(Map<String, ?> map) {
        this.facility = map.containsKey("syslog_FACILITY")
                ? (int) map.get("syslog_FACILITY")
                : null;

        this.severity = map.containsKey("syslog_SEVERITY")
                ? (int) map.get("syslog_SEVERITY")
                : null;

        this.severityText = map.containsKey("syslog_SEVERITY_TEXT")
                ? (String) map.get("syslog_SEVERITY_TEXT")
                : null;

        this.timestamp = map.containsKey("syslog_TIMESTAMP")
                ? (String) map.get("syslog_TIMESTAMP")
                : null;

        this.host = map.containsKey("syslog_HOST")
                ? (String) map.get("syslog_HOST")
                : null;

        this.appName = map.containsKey("syslog_APP_NAME")
                ? (String) map.get("syslog_APP_NAME")
                : null;

        this.procid = map.containsKey("syslog_PROCID")
                ? Long.valueOf((String) map.get("syslog_PROCID"))
                : null;

        this.msgid = map.containsKey("syslog_MSGID")
                ? (String) map.get("syslog_MSGID")
                : null;

        this.version = map.containsKey("syslog_VERSION")
                ? (Integer) map.get("syslog_VERSION")
                : null;

        this.message = map.containsKey("syslog_MESSAGE")
                ? (String) map.get("syslog_MESSAGE")
                : null;

        this.decodeErrors = map.containsKey("syslog_DECODE_ERRORS")
                ? Boolean.valueOf((String) map.get("syslog_DECODE_ERRORS"))
                : null;
    }

    public SyslogRFC5424MicroModel(int facility, int severity,
            String severityText, String timestamp, String host, String appName,
            Long procid, String msgid, Integer version, String message,
            boolean decodeErrors) {
        super();
        this.facility = facility;
        this.severity = severity;
        this.severityText = severityText;
        this.timestamp = timestamp;
        this.host = host;
        this.appName = appName;
        this.procid = procid;
        this.msgid = msgid;
        this.version = version;
        this.message = message;
        this.decodeErrors = decodeErrors;
    }

    public int getFacility() {
        return facility;
    }

    public void setFacility(int facility) {
        this.facility = facility;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getSeverityText() {
        return severityText;
    }

    public void setSeverityText(String severityText) {
        this.severityText = severityText;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getProcid() {
        return procid;
    }

    public void setProcid(Long procid) {
        this.procid = procid;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDecodeErrors() {
        return decodeErrors;
    }

    public void setDecodeErrors(boolean decodeErrors) {
        this.decodeErrors = decodeErrors;
    }

    @Override
    public String toString() {
        return "SyslogRFC5424MicroModel [facility=" + facility + ", severity="
                + severity + ", severityText=" + severityText + ", timestamp="
                + timestamp + ", host=" + host + ", appName=" + appName
                + ", procid=" + procid + ", msgid=" + msgid + ", version="
                + version + ", message=" + message + ", decodeErrors="
                + decodeErrors + "]";
    }

}
