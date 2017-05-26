package io.elastest.etm.service.sut;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.api.model.SutSpecification;
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
	
	public List<SutSpecification> getAllSutSpecification(){
		
		return sutRepository.findAll();
	}
	
	public SutSpecification getSutSpecById(Long id){
		return sutRepository.findOne(id);
	}
	
	public SutExecution createSutExecution(SutExecution sutExecution){
		
		//TODO Start up instance for a given SUT
		
		return sutExecutionRepository.save(sutExecution);
	}
	
	public List<SutExecution> getAllSutExecBySutSpec(SutSpecification sut){
		
		return sutExecutionRepository.findAll();
	}
	
	public SutExecution getSutExecutionById(Long id){
		
		return sutExecutionRepository.findOne(id);
	}
	
	

}
