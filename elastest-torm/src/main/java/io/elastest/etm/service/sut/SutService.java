package io.elastest.etm.service.sut;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.api.model.SutSpecification;
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.dao.SutExecutionRepository;
import io.elastest.etm.dao.SutRepository;

@Service
public class SutService {
	
	@Autowired
	SutRepository sutRepository;
	
	@Autowired
	SutExecutionRepository sutExecutionRepository;
	
	
	public SutSpecification createSutSpecification(SutSpecification sutSpecification){
		return sutRepository.save(sutSpecification);
	}
	
	public void deleteSut(Long sutId) {
		SutSpecification sut = sutRepository.findOne(sutId);
		sutRepository.delete(sut);
	}
	
	public List<SutSpecification> getAllSutSpecification(){
		
		return sutRepository.findAll();
	}
	
	public SutSpecification getSutSpecById(Long id){
		return sutRepository.findOne(id);
	}
	
	public SutExecution createSutExecution(Long sutId){
		SutSpecification sut = sutRepository.findOne(sutId);
		//TODO Start up instance for a given SUT
		SutExecution sutExecution = null;
		return sutExecutionRepository.save(sutExecution);
	}
	
	public void deleteSutExec(Long sutExecId) {
		SutExecution sutExec = sutExecutionRepository.findOne(sutExecId);
		sutExecutionRepository.delete(sutExec);
	}
	
	public List<SutExecution> getAllSutExecBySutSpec(SutSpecification sut){
		
		return sutExecutionRepository.findAll();
	}
	
	public SutExecution getSutExecutionById(Long id){
		
		return sutExecutionRepository.findOne(id);
	}
	
	

}
