package io.elastest.etm.test.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.elastest.etm.dao.SutExecutionRepository;
import io.elastest.etm.dao.SutRepository;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.service.SutService;
import io.elastest.etm.test.extensions.MockitoExtension;

@RunWith(JUnitPlatform.class)
@ExtendWith({MockitoExtension.class})
public class SutServiceTest {
	
	@Mock
	public SutRepository sutRepository;
	
	@Mock
	public SutExecutionRepository sutExecutionRepository;
	
	@InjectMocks
	public SutService sutService;
	
	public SutExecution sutExec;
	
	public SutSpecification sut;
	
	@BeforeEach
	public void setUp(){
		sut = new SutSpecification();
		sutExec = new SutExecution(1L, sut, null, null);				
	}

	
	@Test
	public void testUndeploySut(){

		when(sutRepository.findOne(Mockito.anyLong())).thenReturn(sut);
		when(sutExecutionRepository.findByIdAndSutSpecification(Mockito.anyLong(), Mockito.any(SutSpecification.class))).thenReturn(sutExec);
		
		sutService.undeploySut(1L, 1L);
		assertTrue(sutExec.getDeployStatus().equals(SutExecution.DeployStatusEnum.UNDEPLOYED));
		
	}
	
	@Test
	public void testCreateSutExecutionById(){
		when(sutRepository.findOne(Mockito.anyLong())).thenReturn(sut);
		when(sutExecutionRepository.save(Mockito.any(SutExecution.class))).thenReturn(sutExec);
		
		SutExecution sutExec =  sutService.createSutExecutionById(1L);
		assertTrue(sutExec.getSutSpecification().getId() == this.sut.getId());		
	}
	
	@Test
	public void testCreateSutExecutionBySut(){
		when(sutExecutionRepository.save(Mockito.any(SutExecution.class))).thenReturn(sutExec);
		
		SutExecution sutExec = sutService.createSutExecutionBySut(sut);
		assertTrue(sutExec.getSutSpecification().getId() == this.sut.getId());
		
	}
	
	@Test
	public void testDeleteSutExec(){
		when(sutExecutionRepository.findOne(Mockito.anyLong())).thenReturn(sutExec);
		
		sutService.deleteSutExec(sutExec.getId());
		assertTrue(true);
	}
	
	@Test
	public void testGetAllSutExecBySutId(){
		List<SutExecution> sutExecutions = new ArrayList<>();
		sutExecutions.add(new SutExecution(2L, sut, null, null));
		sutExecutions.add(new SutExecution(3L, sut, null, null));
		
		when(sutExecutionRepository.findAll()).thenReturn(sutExecutions);
		
		List<SutExecution> sutExecutionsResult = sutService.getAllSutExecBySutId(1L);		
		assertTrue(sutExecutionsResult.size() == 2);		
	}	

	
	@Test
	public void testGetSutExecutionById(){
		when(sutExecutionRepository.findOne(Mockito.anyLong())).thenReturn(sutExec);
		
		SutExecution sutExecLocal = sutService.getSutExecutionById(Mockito.anyLong());
		assertTrue(sutExecLocal.getId() == 1L);
	}
	
	@Test
	public void testModifySutExec(){
		when(sutExecutionRepository.findOne(Mockito.anyLong())).thenReturn(sutExec);
		when(sutExecutionRepository.save(Mockito.any(SutExecution.class))).thenReturn(sutExec);
		
		SutExecution sutExecResult = sutService.modifySutExec(sutExec);
		assertTrue(sutExecResult.getId() == sutExec.getId());
	}

}
