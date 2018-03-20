package io.elastest.etm.service;

import java.util.List;

import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.elastest.etm.dao.SutExecutionRepository;
import io.elastest.etm.dao.SutRepository;
import io.elastest.etm.model.EimMonitoringConfig;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;

@Service
public class SutService {

	private static final Logger logger = LoggerFactory.getLogger(SutService.class);

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
			if (!sut.isInstrumentedByElastest()) {
				sut.setEimMonitoringConfig(null);
			} else {
				EimMonitoringConfig savedEimMonitoringConfig = this.eimService
						.createEimMonitoringConfigAndChilds(sut.getEimMonitoringConfig());
				sut.setEimMonitoringConfig(savedEimMonitoringConfig);
			}
			sut = sutRepository.save(sut); // Save first
			SutExecution sutExec = createSutExecutionBySut(sut);
			sut.setCurrentSutExec(sutExec.getId());
			if (sut.isInstrumentalize()) {
				sut = this.instrumentalizeSut(sut);
			}
		} else {
			SutSpecification savedSut = sutRepository.getOne(sut.getId());
			if (sut.isInstrumentedByElastest()) {
				if (!savedSut.isInstrumentalize() && sut.isInstrumentalize()) { // Instrumentalize
					sut = this.instrumentalizeSut(sut);
				} else if (savedSut.isInstrumentalize() && !sut.isInstrumentalize()) { // Deinstrumentalize
					logger.debug("Deinstrumentalizing SuT \"" + sut.getName() + "\"");
					this.eimService.deInstrumentalizeAndUnDeployBeats(sut.getEimConfig());
				} else {
					logger.debug("SuT is already instrumentalized. No changes");
				}
			} else {
				if (savedSut.isInstrumentalize()) {
					this.eimService.deInstrumentalizeAndUnDeployBeats(sut.getEimConfig());
				}
			}
		}
		return sut;
	}

	public SutSpecification instrumentalizeSut(SutSpecification sut) {
		SutExecution sutExec = createSutExecutionBySut(sut);
		sut.setCurrentSutExec(sutExec.getId());
		sutExec.setUrl(sut.getSpecification());

		// Deploy beats
		EimMonitoringConfig eimMonitoringConfig = sut.getEimMonitoringConfig();
		eimMonitoringConfig.setExec(sut.getSutMonitoringIndex());
		sut.setEimMonitoringConfig(eimMonitoringConfig);

		logger.debug("Instrumentalizing SuT \"" + sut.getName() + "\"");
		this.eimService.instrumentalizeAndDeployBeats(sut.getEimConfig(), sut.getEimMonitoringConfig());

		return sut;
	}

	public void deleteSut(Long sutId) {
		SutSpecification sut = sutRepository.findOne(sutId);
		if (sut.isInstrumentalize()) {
			this.eimService.deInstrumentalizeAndUnDeployBeats(sut.getEimConfig());
		}
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
