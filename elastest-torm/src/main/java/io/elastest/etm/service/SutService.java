package io.elastest.etm.service;

import java.util.List;

import javax.xml.ws.http.HTTPException;

import org.springframework.stereotype.Service;

import io.elastest.etm.dao.SutExecutionRepository;
import io.elastest.etm.dao.SutRepository;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;

@Service
public class SutService {

	private final SutRepository sutRepository;
	private final SutExecutionRepository sutExecutionRepository;
	private final EimService eimService;

	public SutService(SutRepository sutRepository, SutExecutionRepository sutExecutionRepository,
			EimService eimService) {
		super();
		this.sutRepository = sutRepository;
		this.sutExecutionRepository = sutExecutionRepository;
		this.eimService = eimService;
	}

	public SutSpecification createSutSpecification(SutSpecification sutSpecification) {
		sutSpecification = prepareSutToSave(sutSpecification);
		return sutRepository.save(sutSpecification);
	}

	public SutSpecification prepareSutToSave(SutSpecification sut) {
		if (sut.getId() == 0) { // If is a new Sut, set
			sut = sutRepository.save(sut); // Save first
			SutExecution sutExec = createSutExecutionBySut(sut);
			sut.setCurrentSutExec(sutExec.getId());
		} else if (sut.getSutType() == SutTypeEnum.DEPLOYED) {
			SutSpecification savedSut = sutRepository.getOne(sut.getId());
			if (!savedSut.isinstrumentalize() && sut.isinstrumentalize()) { // Instrumentalize
				SutExecution sutExec = createSutExecutionBySut(sut);
				sut.setCurrentSutExec(sutExec.getId());
//				if (sut.getInstrumentedBy() != InstrumentedByEnum.ADMIN) {
					sutExec.setUrl(sut.getSpecification());
//				}
				this.eimService.instrumentalizeSut(sut.getEimConfig());
			} else if (savedSut.isinstrumentalize() && !sut.isinstrumentalize()) { // Deinstrumentalize
				this.eimService.deinstrumentSut(sut.getEimConfig());
			}
		}
		return sut;
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
