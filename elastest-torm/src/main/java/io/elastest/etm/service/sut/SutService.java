package io.elastest.etm.service.sut;

import java.util.List;

import javax.xml.ws.http.HTTPException;

import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.api.model.SutSpecification;
import io.elastest.etm.dao.SutExecutionRepository;
import io.elastest.etm.dao.SutRepository;

@Service
public class SutService {

	private final SutRepository sutRepository;
	private final SutExecutionRepository sutExecutionRepository;

	public SutService(SutRepository sutRepository, SutExecutionRepository sutExecutionRepository) {
		super();
		this.sutRepository = sutRepository;
		this.sutExecutionRepository = sutExecutionRepository;
	}

	public SutSpecification createSutSpecification(SutSpecification sutSpecification) {
		return sutRepository.save(sutSpecification);
	}

	public void deleteSut(Long sutId) {
		SutSpecification sut = sutRepository.findOne(sutId);
		sutRepository.delete(sut);
	}

	public List<SutSpecification> getAllSutSpecification() {

		return sutRepository.findAll();
	}

	public SutSpecification getSutSpecById(Long id) {
		return sutRepository.findOne(id);
	}

	public SutSpecification modifySut(SutSpecification sut) {
		if (sutRepository.findOne(sut.getId()) != null) {
			return sutRepository.save(sut);
		} else {
			throw new HTTPException(405);
		}
	}

	public void undeploySut(Long sutId, Long sutExecId) {
		SutSpecification sut = sutRepository.findOne(sutId);
		SutExecution sutExec = sutExecutionRepository.findByIdAndSutSpecification(sutExecId, sut);
		sutExec.setDeployStatus(SutExecution.DeployStatusEnum.UNDEPLOYED);
		sutExecutionRepository.save(sutExec);
	}

	public SutExecution createSutExecutionById(Long sutId) {
		SutSpecification sut = sutRepository.findOne(sutId);
		SutExecution sutExecution = new SutExecution();
		sutExecution.setSutSpecification(sut);

		return sutExecutionRepository.save(sutExecution);
	}

	public SutExecution createSutExecutionBySut(SutSpecification sut) {
		SutExecution sutExecution = new SutExecution();
		sutExecution.setSutSpecification(sut);
		sutExecution.setUrl("");
		sutExecution.setDeployStatus(SutExecution.DeployStatusEnum.DEPLOYING);
		return sutExecutionRepository.save(sutExecution);
	}

	public void deleteSutExec(Long sutExecId) {
		SutExecution sutExec = sutExecutionRepository.findOne(sutExecId);
		sutExecutionRepository.delete(sutExec);
	}

	public List<SutExecution> getAllSutExecBySutId(Long sutId) {
		SutSpecification sut = sutRepository.findOne(sutId);
		return getAllSutExecBySutSpec(sut);
	}

	public List<SutExecution> getAllSutExecBySutSpec(SutSpecification sut) {
		return sutExecutionRepository.findAll();
	}

	public SutExecution getSutExecutionById(Long id) {
		return sutExecutionRepository.findOne(id);
	}

	public SutExecution modifySutExec(SutExecution sutExec) {
		if (sutExecutionRepository.findOne(sutExec.getId()) != null) {
			return sutExecutionRepository.save(sutExec);
		} else {
			throw new HTTPException(405);
		}
	}

}
