package io.elastest.etm.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.eus.api.model.ExecutionData;

public class EusExecutionData {
    @JsonProperty("type")
    String type;

    @JsonProperty("tJobId")
    Long tJobId;

    @JsonProperty("tJobExecId")
    Long tJobExecId;

    @JsonProperty("monitoringIndex")
    String monitoringIndex;

    @JsonProperty("webRtcStatsActivated")
    boolean webRtcStatsActivated = false;

    @JsonProperty("folderPath")
    String folderPath;

    @JsonProperty("useSutNetwork")
    boolean useSutNetwork = false;

    @JsonProperty("sutContainerPrefix")
    String sutContainerPrefix;

    public EusExecutionData() {
    }

    public EusExecutionData(String type, Long tJobId, Long tJobExecId,
            String monitoringIndex, boolean webRtcStatsActivated,
            String folderPath, boolean useSutNetwork,
            String sutContainerPrefix) {
        super();
        this.type = type;
        this.tJobId = tJobId;
        this.tJobExecId = tJobExecId;
        this.monitoringIndex = monitoringIndex;
        this.webRtcStatsActivated = webRtcStatsActivated;
        this.folderPath = folderPath;
        this.useSutNetwork = useSutNetwork;
        this.sutContainerPrefix = sutContainerPrefix;
    }

    public EusExecutionData(TJobExecution tJobExec, String folderPath,
            boolean useSutNetwork, String sutContainerPrefix) {
        this.type = "tJob";
        this.tJobId = tJobExec.getTjob().getId();
        this.tJobExecId = tJobExec.getId();
        this.monitoringIndex = tJobExec.getOnlyTJobExecMonitoringIndex();
        this.folderPath = folderPath;
        initWebRtcStatsActivated(tJobExec);
        this.useSutNetwork = useSutNetwork;
        this.sutContainerPrefix = sutContainerPrefix;
    }

    public EusExecutionData(TJobExecution tJobExec, String folderPath) {
        this.type = "tJob";
        this.tJobId = tJobExec.getTjob().getId();
        this.tJobExecId = tJobExec.getId();
        this.monitoringIndex = tJobExec.getOnlyTJobExecMonitoringIndex();
        this.folderPath = folderPath;
        initWebRtcStatsActivated(tJobExec);
        this.useSutNetwork = false;
    }

    public EusExecutionData(ExternalTJobExecution exTJobExec,
            String folderPath) {
        this.type = "externalTJob";
        this.tJobId = exTJobExec.getExTJob().getId();
        this.tJobExecId = exTJobExec.getId();
        this.monitoringIndex = exTJobExec.getExternalTJobExecMonitoringIndex();
        this.folderPath = folderPath;
        // initWebRtcStatsActivated(exTJobExec);
        this.webRtcStatsActivated = false;
        this.useSutNetwork = false;

    }

    public EusExecutionData(ExternalTJobExecution exTJobExec, String folderPath,
            boolean useSutNetwork, String sutContainerPrefix) {
        this.type = "externalTJob";
        this.tJobId = exTJobExec.getExTJob().getId();
        this.tJobExecId = exTJobExec.getId();
        this.monitoringIndex = exTJobExec.getExternalTJobExecMonitoringIndex();
        this.folderPath = folderPath;
        // initWebRtcStatsActivated(exTJobExec);
        this.webRtcStatsActivated = false;
        this.useSutNetwork = useSutNetwork;
        this.sutContainerPrefix = sutContainerPrefix;
    }

    private void initWebRtcStatsActivated(TJobExecution tJobExec) {
        this.webRtcStatsActivated = false;
        try {
            Map<String, String> vars = tJobExec.getTjob()
                    .getTJobTSSConfigEnvVars("EUS");
            String statsKey = "ET_CONFIG_WEB_RTC_STATS";
            if (vars.containsKey(statsKey) && vars.get(statsKey) != null) {
                this.webRtcStatsActivated = "true".equals(vars.get(statsKey));
            }

        } catch (Exception e) {
        }

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long gettJobId() {
        return tJobId;
    }

    public void settJobId(Long tJobId) {
        this.tJobId = tJobId;
    }

    public Long gettJobExecId() {
        return tJobExecId;
    }

    public void settJobExecId(Long tJobExecId) {
        this.tJobExecId = tJobExecId;
    }

    public String getMonitoringIndex() {
        return monitoringIndex;
    }

    public void setMonitoringIndex(String monitoringIndex) {
        this.monitoringIndex = monitoringIndex;
    }

    public boolean isWebRtcStatsActivated() {
        return webRtcStatsActivated;
    }

    public void setWebRtcStatsActivated(boolean webRtcStatsActivated) {
        this.webRtcStatsActivated = webRtcStatsActivated;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getSutContainerPrefix() {
        return sutContainerPrefix;
    }

    public void setSutContainerPrefix(String sutContainerPrefix) {
        this.sutContainerPrefix = sutContainerPrefix;
    }

    public ExecutionData getAsExecutionData() {
        return new ExecutionData(this.type, this.tJobId, this.tJobExecId,
                this.monitoringIndex, this.webRtcStatsActivated,
                this.folderPath, this.useSutNetwork, this.sutContainerPrefix);
    }

    @Override
    public String toString() {
        return "EusExecutionData [type=" + type + ", tJobId=" + tJobId
                + ", tJobExecId=" + tJobExecId + ", monitoringIndex="
                + monitoringIndex + ", webRtcStatsActivated="
                + webRtcStatsActivated + ", folderPath=" + folderPath
                + ", sutContainerPrefix=" + sutContainerPrefix + "]";
    }

    public String getKey() {
        return type + "_" + tJobId + "_" + tJobExecId;
    }
}
