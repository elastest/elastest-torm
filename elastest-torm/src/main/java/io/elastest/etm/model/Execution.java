package io.elastest.etm.model;

import java.util.List;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;

import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTJobExecution;

/**
 * This class stores the whole context of a TJob Execution.
 *
 */
public class Execution extends DockerServiceStatus {
    private TJobExecution tJobExec;
    private TJob tJob;
    private ExternalTJob externalTJob;
    private ExternalTJobExecution externalTJobExec;
    private SutSpecification sut;
    private SutExecution sutExec;
    private List<ReportTestSuite> reportTestSuite;

    public Execution() {
    }

    /* *** For TJob *** */
    public Execution(TJobExecution tJobExec, boolean withSut) {
        this.tJobExec = tJobExec;
        this.updateFromTJobExec(tJobExec);
    }

    public Execution(TJobExecution tJobExec) {
        this.tJobExec = tJobExec;
        this.updateFromTJobExec(tJobExec);
    }

    /* *** For External TJob *** */
    public Execution(ExternalTJobExecution externalTJobExec, boolean withSut) {
        this.externalTJobExec = externalTJobExec;
        this.updateFromExternalTJobExec(externalTJobExec);
    }

    public Execution(ExternalTJobExecution externalTJobExec) {
        this.externalTJobExec = externalTJobExec;
        this.updateFromExternalTJobExec(externalTJobExec);
    }

    /* Getters and Setters */
    public Long getExecutionId() {
        if (isExternal()) {
            return externalTJobExec.getId();
        } else {
            return tJobExec.getId();
        }
    }

    public boolean isExternal() {
        return tJobExec == null ? true : false;
    }

    public TJob gettJob() {
        return tJob;
    }

    public void settJob(TJob tJob) {
        this.tJob = tJob;
    }

    public TJobExecution getTJobExec() {
        return tJobExec;
    }

    public void setTJobExec(TJobExecution tJobExec) {
        this.updateFromTJobExec(tJobExec);
    }

    public void updateFromTJobExec(TJobExecution tJobExec) {
        this.tJobExec = tJobExec;
        this.tJob = tJobExec.getTjob();
        this.sut = this.tJob.getSut();
    }

    public ExternalTJob getExternalTJob() {
        return externalTJob;
    }

    public void setExternalTJob(ExternalTJob externalTJob) {
        this.externalTJob = externalTJob;
    }

    public ExternalTJobExecution getExternalTJobExec() {
        return externalTJobExec;
    }

    public void setExternalTJobExec(ExternalTJobExecution externalTJobExec) {
        this.updateFromExternalTJobExec(externalTJobExec);
    }

    public void updateFromExternalTJobExec(ExternalTJobExecution externalTJobExec) {
        this.externalTJobExec = externalTJobExec;
        this.externalTJob = externalTJobExec.getExTJob();
        this.sut = this.externalTJob.getSut();
    }

    public SutSpecification getSut() {
        return sut;
    }

    public void setSut(SutSpecification sut) {
        this.sut = sut;
    }

    public boolean isWithSut() {
        return isExternal() ? externalTJobExec.isWithSut() : tJobExec.isWithSut();
    }

    public SutExecution getSutExec() {
        return sutExec;
    }

    public void setSutExec(SutExecution sutExec) {
        this.sutExec = sutExec;
    }

    public void updateTJobExecutionStatus(ResultEnum resultCode, String resultMsg) {
        if (isExternal()) {
            externalTJobExec.setResult(resultCode);
            externalTJobExec.setResultMsg(resultMsg);
        } else {
            tJobExec.setResult(resultCode);
            tJobExec.setResultMsg(resultMsg);
        }

        setStatusMsg(resultMsg);
    }

    public String getTJobType() {
        if (isExternal()) {
            return "External TJob";
        } else {
            return "TJob";
        }
    }

    public String getTJobExecType() {
        if (isExternal()) {
            return "External TJob Execution";
        } else {
            return "TJob Execution";
        }
    }

    public String getKeyIdByExecutionType() {
        if (isExternal()) {
            return "extexec";
        } else {
            return "exec";
        }
    }

    public List<ReportTestSuite> getReportTestSuite() {
        return reportTestSuite;
    }

    public void setReportTestSuite(List<ReportTestSuite> reportTestSuite) {
        this.reportTestSuite = reportTestSuite;
    }

    public boolean hasJobAndExecution() {
        if (isExternal()) {
            return getExternalTJobExec() != null && getExternalTJob() != null;
        } else {
            return getTJobExec() != null && gettJob() != null;
        }
    }

    @Override
    public String toString() {
        return "Execution [isExternal=" + isExternal() + ", tJobExec=" + tJobExec + ", tJob=" + tJob
                + ", externalTJob=" + externalTJob + ", externalTJobExec=" + externalTJobExec
                + ", sut=" + sut + ", sutExec=" + sutExec + ", withSut=" + isWithSut() + "]";
    }

}
