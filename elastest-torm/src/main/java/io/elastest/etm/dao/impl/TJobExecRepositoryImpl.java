package io.elastest.etm.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.extension.TJobExecRepositoryExtension;

public class TJobExecRepositoryImpl implements TJobExecRepositoryExtension {
	
	@PersistenceContext
	public EntityManager em;

	@Override
	public TJobExecution save2(TJobExecution tJobExec) {
		// TODO Auto-generated method stub
		System.out.println("*****************************************************************************");
		em.merge(tJobExec);		
		return tJobExec;
	}

}
