package io.elastest.etm.platform.service;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TJobExecution;

public interface PlatformService {
    
    public TJobExecution deployTJobExecution(TJobExecution tJobExecution);
    public TJobExecution unDeployTJobExecution(TJobExecution tJobExecution);
    public SupportServiceInstance deployTSS(SupportService supportService);
    public String deployTSSs(TJobExecution tJobExecution);
    public String undeployTSS();
    public String deploySUT();
    public String undeploySUT();
    
    
}
