package io.elastest.etm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import io.elastest.etm.dao.SutExecutionRepository;
import io.elastest.etm.dao.SutRepository;
import io.elastest.etm.model.EimMonitoringConfig;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.external.ExternalElasticsearch;
import io.elastest.etm.utils.UtilsService;

@Service
public class SutService {

    private static final Logger logger = LoggerFactory
            .getLogger(SutService.class);

    private final SutRepository sutRepository;
    private final SutExecutionRepository sutExecutionRepository;
    private final EimService eimService;
    private MonitoringServiceInterface monitoringService;
    private final UtilsService utilsService;
    private final TracesService tracesService;

    public SutService(SutRepository sutRepository,
            SutExecutionRepository sutExecutionRepository,
            EimService eimService, MonitoringServiceInterface monitoringService,
            UtilsService utilsService, TracesService tracesService) {
        super();
        this.sutRepository = sutRepository;
        this.sutExecutionRepository = sutExecutionRepository;
        this.eimService = eimService;
        this.monitoringService = monitoringService;
        this.utilsService = utilsService;
        this.tracesService = tracesService;
    }

    public SutSpecification createSutSpecification(
            SutSpecification sutSpecification) {
        sutSpecification = prepareSutToSave(sutSpecification);
        return sutRepository.save(sutSpecification);
    }

    public SutSpecification prepareSutToSave(SutSpecification sut) {
        sut = this.prepareEimMonitoringConfig(sut);
        if (sut.getId() == 0) { // If is a new Sut, set
            logger.debug("asd {}", sut);
            sut = sutRepository.save(sut); // Save first
            SutExecution sutExec = createSutExecutionBySut(sut);
            if (sut.isDeployedOutside()) {
                sut.setCurrentSutExec(sutExec.getId());
                String[] index = { sut.getSutMonitoringIndex() };

                monitoringService.createMonitoringIndex(index);

                if (sut.isInstrumentalize()) {
                    sut = this.instrumentalizeSut(sut);
                }
            }
        } else {
            SutSpecification savedSut = sutRepository.getOne(sut.getId());
            if (sut.isInstrumentedByElastest()) {
                if (!savedSut.isInstrumentalize() && sut.isInstrumentalize()) { // Instrumentalize
                    sut = this.instrumentalizeSut(sut);
                } else if (savedSut.isInstrumentalize()
                        && !sut.isInstrumentalize()) { // Deinstrumentalize
                    logger.debug("Starting 'Deinstrumentalizing SuT \""
                            + sut.getName() + "\"'");
                    this.eimService.deInstrumentalizeAndUnDeployBeats(
                            sut.getEimConfig(), sut.getEimMonitoringConfig());
                } else {
                    logger.debug("SuT is already instrumentalized. No changes");
                }
            } else {
                if (savedSut.isInstrumentalize()) {
                    this.eimService.deInstrumentalizeAndUnDeployBeats(
                            sut.getEimConfig(), sut.getEimMonitoringConfig());
                }
            }
        }
        return sut;
    }

    public SutSpecification prepareEimMonitoringConfig(SutSpecification sut) {
        if (!sut.isInstrumentedByElastest()) {
            sut.setEimMonitoringConfig(null);
        } else {
            EimMonitoringConfig savedEimMonitoringConfig = this.eimService
                    .createEimMonitoringConfigAndChilds(
                            sut.getEimMonitoringConfig());
            sut.setEimMonitoringConfig(savedEimMonitoringConfig);
        }
        return sut;
    }

    public SutSpecification instrumentalizeSut(SutSpecification sut) {
        SutExecution sutExec = createSutExecutionBySut(sut);
        sut.setCurrentSutExec(sutExec.getId());

        String[] index = { sut.getSutMonitoringIndex() };
        monitoringService.createMonitoringIndex(index);

        sutExec.setUrl(sut.getSpecification());

        // Deploy beats
        EimMonitoringConfig eimMonitoringConfig = sut.getEimMonitoringConfig();
        eimMonitoringConfig.setExec(sut.getSutMonitoringIndex());
        sut.setEimMonitoringConfig(eimMonitoringConfig);

        logger.debug(
                "Starting 'Instrumentalizing SuT \"" + sut.getName() + "\"'");
        try {
            this.eimService.instrumentalizeAndDeployBeats(sut.getEimConfig(),
                    sut.getEimMonitoringConfig());
        } catch (Exception e) {
            logger.error("Error on instrumentalizing SuT {}", sut.getName(), e);
            sut.setInstrumentalize(false);
        }

        return sut;
    }

    public void deleteSut(Long sutId) {
        SutSpecification sut = sutRepository.findById(sutId).get();
        if (sut.isInstrumentalize()) {
            this.eimService.deInstrumentalizeAndUnDeployBeats(
                    sut.getEimConfig(), sut.getEimMonitoringConfig());
        }
        sutRepository.delete(sut);
    }

    public List<SutSpecification> getAllSutSpecification() {

        return sutRepository.findAll();
    }

    public SutSpecification getSutSpecById(Long id) {
        return sutRepository.findById(id).get();
    }

    public SutSpecification modifySut(SutSpecification sut) {
        if (sutRepository.findById(sut.getId()) != null) {
            return sutRepository.save(sut);
        } else {
            throw new HTTPException(405);
        }
    }

    public void undeploySut(Long sutId, Long sutExecId) {
        SutSpecification sut = sutRepository.findById(sutId).get();
        SutExecution sutExec = sutExecutionRepository
                .findByIdAndSutSpecification(sutExecId, sut);
        sutExec.setDeployStatus(SutExecution.DeployStatusEnum.UNDEPLOYED);
        sutExecutionRepository.save(sutExec);
    }

    public SutExecution createSutExecutionById(Long sutId) {
        SutSpecification sut = sutRepository.findById(sutId).get();
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
        SutExecution sutExec = sutExecutionRepository.findById(sutExecId).get();
        sutExecutionRepository.delete(sutExec);
    }

    public List<SutExecution> getAllSutExecBySutId(Long sutId) {
        SutSpecification sut = sutRepository.findById(sutId).get();
        return getAllSutExecBySutSpec(sut);
    }

    public List<SutExecution> getAllSutExecBySutSpec(SutSpecification sut) {
        return sutExecutionRepository.findAll();
    }

    public SutExecution getSutExecutionById(Long id) {
        return sutExecutionRepository.findById(id).get();
    }

    public SutExecution modifySutExec(SutExecution sutExec) {
        if (sutExecutionRepository.findById(sutExec.getId()) != null) {
            return sutExecutionRepository.save(sutExec);
        } else {
            throw new HTTPException(405);
        }
    }

    @Async
    public Future<Void> manageSutExecutionUsingExternalElasticsearch(
            SutSpecification sut, String monitoringIndex) {
        ExternalElasticsearch extES = sut.getExternalElasticsearch();
        Date startDate = new Date();

        // TODO user/pass
        String esApiUrl = "http://" + extES.getIp() + ":" + extES.getPort();

        ElasticsearchService esService = new ElasticsearchService(esApiUrl,
                utilsService);

        List<Map<String, Object>> traces = new ArrayList<>();

        Object[] searchAfter = null;
        boolean finish = false;
        while (!finish) {
            try {
                traces = esService.searchTraces(
                        extES.getIndices().split(","), startDate, searchAfter,10000);
                if (traces.size() > 0) {
                    Map<String, Object> lastTrace = traces
                            .get(traces.size() - 1);

                    String sortFieldKet = "sort";
                    if (lastTrace.containsKey(sortFieldKet)) {
                        searchAfter = (Object[]) lastTrace.get(sortFieldKet);
                    }
                }

                for (Map<String, Object> trace : traces) {
                    trace.put("exec", monitoringIndex);
                    trace.put("component", "sut");
                    tracesService.processBeatTrace(trace, false);
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    logger.info(
                            "TJob Execution {}: Manage Sut by External Elasticsearch thread has been interrupted ",
                            monitoringIndex);
                    break;
                }

            } catch (Exception e) {
                logger.error(
                        "Error on getting traces from external Elasticsearch",
                        e);
            }
        }

        return new AsyncResult<Void>(null);
    }

}
