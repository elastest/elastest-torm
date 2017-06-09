package io.elastest.etm.dao.extension;

import io.elastest.etm.api.model.TJobExecution;

public interface TJobExecRepositoryExtension {
	
	public TJobExecution save2(TJobExecution tJobExec);

}
