package io.elastest.etm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SharedAsyncModel;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.external.ExternalElasticsearch;
import io.elastest.etm.model.external.ExternalMonitoringDBForLogs;
import io.elastest.etm.model.external.ExternalMonitoringDBForMetrics;
import io.elastest.etm.model.external.ExternalPrometheus;
import io.elastest.etm.prometheus.client.PrometheusQueryData;
import io.elastest.etm.utils.UtilsService;

@Service
public class SutService {

    private static final Logger logger = LoggerFactory
            .getLogger(SutService.class);

    private final SutRepository sutRepository;
    private final SutExecutionRepository sutExecutionRepository;
    private final EimService eimService;
    private AbstractMonitoringService monitoringService;
    private final UtilsService utilsService;
    private final TracesService tracesService;
    private final ExternalMonitoringDBService externalMonitoringDBService;

    public SutService(SutRepository sutRepository,
            SutExecutionRepository sutExecutionRepository,
            EimService eimService, AbstractMonitoringService monitoringService,
            UtilsService utilsService, TracesService tracesService,
            ExternalMonitoringDBService externalMonitoringDBService) {
        super();
        this.sutRepository = sutRepository;
        this.sutExecutionRepository = sutExecutionRepository;
        this.eimService = eimService;
        this.monitoringService = monitoringService;
        this.utilsService = utilsService;
        this.tracesService = tracesService;
        this.externalMonitoringDBService = externalMonitoringDBService;
    }

    public SutSpecification createSutSpecification(
            SutSpecification sutSpecification) {
        sutSpecification = prepareSutToSave(sutSpecification);
        return sutRepository.save(sutSpecification);
    }

    public SutSpecification prepareSutToSave(SutSpecification sut) {
        sut = this.prepareEimMonitoringConfig(sut);
        if (sut.getId() == 0) { // If is a new Sut, set
            sut = sutRepository.save(sut); // Save first
            SutExecution sutExec = createSutExecutionBySut(sut);
            if (sut.isDeployedOutside()) {
                sut.setCurrentSutExec(sutExec.getId());
                String[] index = { sut.getSutMonitoringIndex() };

                monitoringService.createMonitoringIndex(index);

                if (sut.isInstrumentalize()) {
                    sut = this.instrumentalizeSut(sut, true);
                }
            }
        } else {
            SutSpecification savedSut = sutRepository.getOne(sut.getId());
            if (sut.isInstrumentedByElastest()) {
                if (!savedSut.isInstrumentalize()
                        && !savedSut.isInstrumentalized()
                        && sut.isInstrumentalize()) { // Instrumentalize
                    sut = this.instrumentalizeSut(sut, true);
                } else if (savedSut.isInstrumentalize()
                        && savedSut.isInstrumentalized()
                        && !sut.isInstrumentalize()) { // Deinstrumentalize
                    logger.debug("Starting 'Deinstrumentalizing SuT \"{}\"'",
                            sut.getName());
                    this.deinstrumentalizeSut(sut, true);
                } else {
                    logger.debug("SuT is already instrumentalized. No changes");
                }
            } else {
                if (savedSut.isInstrumentalize()
                        && savedSut.isInstrumentalized()) {
                    this.deinstrumentalizeSut(sut, true);
                }
            }
        }
        return sut;
    }

    /* ******************************************************************* */
    /* ******************************* EIM ******************************* */
    /* ******************************************************************* */

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

    public SutSpecification instrumentalizeSut(SutSpecification sut,
            boolean async) {
        SutExecution sutExec = createSutExecutionBySut(sut);
        sut.setCurrentSutExec(sutExec.getId());

        String[] index = { sut.getSutMonitoringIndex() };
        monitoringService.createMonitoringIndex(index);

        sutExec.setUrl(sut.getSpecification());

        logger.debug("Starting 'Instrumentalizing SuT \"{}\"'", sut.getName());
        try {
            if (async) {
                this.eimService.instrumentalizeAsync(sut.getEimConfig());
            } else {
                this.eimService.instrumentalize(sut.getEimConfig());
            }
            sut.setInstrumentalized(true);
        } catch (Exception e) {
            logger.error("Error on instrumentalizing SuT {}", sut.getName(), e);
            sut.setInstrumentalize(false);
            sut.setInstrumentalized(false);

            try {
                this.eimService.deinstrumentalizeAsync(sut.getEimConfig());
            } catch (Exception e1) {
            }
        }

        return sut;
    }

    public SutSpecification deinstrumentalizeSut(SutSpecification sut,
            boolean async) {
        // Todo queue if there are tjob execs
        try {
            if (async) {
                this.eimService.deinstrumentalizeAsync(sut.getEimConfig());
            } else {
                this.eimService.deinstrumentalize(sut.getEimConfig());
            }
            sut.setInstrumentalize(false);
            sut.setInstrumentalized(false);
        } catch (Exception e) {
            logger.error("Error on deinstrumentalizing SuT {}", sut.getName(),
                    e);
        }

        return sut;
    }

    public SutSpecification deployEimSutBeats(SutSpecification sut,
            boolean async) throws Exception {
        // Deploy beats
        EimMonitoringConfig eimMonitoringConfig = sut.getEimMonitoringConfig();
        eimMonitoringConfig.setExec(sut.getSutMonitoringIndex());
        sut.setEimMonitoringConfig(eimMonitoringConfig);

        logger.debug("Starting 'Deploying EIM Beats in SuT \"{}\"'",
                sut.getName());
        try {
            if (async) {
                this.eimService.deployBeatsAsync(sut.getEimConfig(),
                        sut.getEimMonitoringConfig());
            } else {
                this.eimService.deployBeats(sut.getEimConfig(),
                        sut.getEimMonitoringConfig());
            }
        } catch (Exception e) {
            String msg = "Error on 'Deploying EIM Beats in SuT " + sut.getName()
                    + "'";
            logger.error(msg, e);

            try {
                this.eimService.undeployBeatsAsync(sut.getEimConfig(),
                        sut.getEimMonitoringConfig());
            } catch (Exception e1) {
            }

            throw new Exception(msg, e);
        }
        return sut;

    }

    public SutSpecification undeployEimSutBeats(SutSpecification sut,
            boolean async) throws Exception {
        try {
            if (async) {
                this.eimService.undeployBeatsAsync(sut.getEimConfig(),
                        sut.getEimMonitoringConfig());
            } else {
                this.eimService.unDeployBeats(sut.getEimConfig(),
                        sut.getEimMonitoringConfig());
            }

        } catch (Exception e) {
            String msg = "Error on 'Undeploying EIM Beats in SuT "
                    + sut.getName() + "'";
            logger.error(msg, e);
            throw new Exception(msg, e);
        }
        return sut;
    }

    public SutSpecification instrumentalizeSutAndDeployBeats(
            SutSpecification sut) {
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

    /* ******************************************************************* */
    /* ***************************** END EIM ***************************** */
    /* ******************************************************************* */

    public void deleteSut(Long sutId) {
        SutSpecification sut = sutRepository.findById(sutId).get();
        if (sut.isInstrumentalize() && sut.isInstrumentalized()) {
            this.eimService.deInstrumentalizeAndUnDeployBeats(
                    sut.getEimConfig(), sut.getEimMonitoringConfig());
        }

        monitoringService
                .deleteMonitoringDataByIndices(sut.getAllMonitoringIndices());

        sutRepository.delete(sut);
    }

    public SutSpecification duplicateSut(Long sutId) {
        SutSpecification sut = sutRepository.findById(sutId).get();
        SutSpecification newSut = new SutSpecification(sut);

        ExternalMonitoringDBForMetrics externalMonitoringDBForMetrics = externalMonitoringDBService
                .duplicateExternalMonitoringDBForMetrics(
                        newSut.getExternalMonitoringDBForMetrics());
        newSut.setExternalMonitoringDBForMetrics(
                externalMonitoringDBForMetrics);

        ExternalMonitoringDBForLogs externalMonitoringDBForLogs = externalMonitoringDBService
                .duplicateExternalMonitoringDBForLogs(
                        newSut.getExternalMonitoringDBForLogs());
        newSut.setExternalMonitoringDBForLogs(externalMonitoringDBForLogs);

        return this.createSutSpecification(newSut);
    }

    public List<SutSpecification> getAllSutSpecification() {

        return sutRepository.findAll();
    }

    public SutSpecification getSutSpecById(Long id) {
        return sutRepository.findById(id).get();
    }

    public List<SutSpecification> getSutsByName(String name) {
        return sutRepository.findByName(name);
    }

    public List<SutSpecification> getSutsByNameAndProject(String name,
            Project project) {
        return sutRepository.findByNameAndProject(name, project);
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
        if (sutExec.getSutSpecification().isDeployedOutside()) {
            String index = sutExec.getSutExecMonitoringIndex();
            monitoringService
                    .deleteMonitoringDataByIndices(Arrays.asList(index));
        }
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
    public Future<Void> manageSutExecutionUsingExternalElasticsearchForLogs(
            SutSpecification sut, String monitoringIndex, Date startDate,
            Map<String, SharedAsyncModel<Void>> sharedAsyncModelMap,
            String sharedAsyncModelKey, String endDateKey) {
        ExternalElasticsearch extES = (ExternalElasticsearch) sut
                .getExternalMonitoringDBForLogs().getExternalMonitoringDB();

        String esApiUrl = extES.getProtocol() + "://" + extES.getIp() + ":"
                + extES.getPort();

        try {
            ElasticsearchService esService = new ElasticsearchService(esApiUrl,
                    extES.getUser(), extES.getPass(), extES.getPath(),
                    utilsService);
            List<Map<String, Object>> traces = new ArrayList<>();

            Object[] searchAfter = null;
            boolean finish = false;

            // LOOP
            while (!finish) {
                Date endDate = null;
                if (sharedAsyncModelMap.containsKey(sharedAsyncModelKey)
                        && sharedAsyncModelMap.get(sharedAsyncModelKey)
                                .getData().containsKey(endDateKey)) {
                    try {
                        endDate = (Date) sharedAsyncModelMap
                                .get(sharedAsyncModelKey).getData()
                                .get(endDateKey);
                    } catch (Exception e) {
                    }
                }
                String indexes = extES.getIndices();

                if (extES.getUseESIndicesByExecution()) {
                    for (Parameter param : sut.getParameters()) {
                        if (param.getName()
                                .equals("EXT_ELASTICSEARCH_LOGS_INDICES")) {
                            indexes = param.getValue();
                            logger.debug("Indexes as String: {}", indexes);
                            break;
                        }
                    }
                }

                try {
                    traces = esService.searchTraces(indexes.split(","),
                            startDate, endDate, searchAfter, 10000,
                            extES.getFieldFilters());
                    if (traces.size() > 0) {
                        Map<String, Object> lastTrace = traces
                                .get(traces.size() - 1);

                        String sortFieldKet = "sort";
                        if (lastTrace.containsKey(sortFieldKet)) {
                            searchAfter = (Object[]) lastTrace
                                    .get(sortFieldKet);
                        }
                    }
                    for (Map<String, Object> trace : traces) {
                        trace = tracesService.convertExternalElasticsearchTrace(
                                trace, extES.getStreamFieldsAsList());
                        trace.put("exec", monitoringIndex);
                        trace.put("component", "sut");

                        // Only works in mini...
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

                if (endDate != null && traces.size() == 0) {
                    finish = true;
                }
            } // End Loop
        } catch (Exception e) {
            logger.error("Error on connect to external Elasticsearch", e);
        }

        stopManageSutByExternalElasticsearchForLogs(sharedAsyncModelMap,
                sharedAsyncModelKey);

        return new AsyncResult<Void>(null);
    }

    public void stopManageSutByExternalElasticsearchForLogs(
            Map<String, SharedAsyncModel<Void>> asyncExternalElasticsearchSutExecs,
            String mapKey) {
        if (!asyncExternalElasticsearchSutExecs.containsKey(mapKey)) {
            return;
        }
        Future<Void> asyncExec = asyncExternalElasticsearchSutExecs.get(mapKey)
                .getFuture();

        try {
            asyncExec.cancel(true);
            asyncExternalElasticsearchSutExecs.remove(mapKey);
            logger.info("Stopped Async Manage Sut by external Elasticsearch {}",
                    mapKey);
        } catch (Exception e) {
            logger.info(
                    "Error during stop Manage Sut by external Elasticsearch {}",
                    mapKey, e);
        }

    }

    @Async
    public Future<Void> manageSutExecutionUsingExternalPrometheusForMetrics(
            SutSpecification sut, String monitoringIndex, Date startDate,
            Map<String, SharedAsyncModel<Void>> sharedAsyncModelMap,
            String sharedAsyncModelKey, String endDateKey) {
        ExternalPrometheus prometheus = (ExternalPrometheus) sut
                .getExternalMonitoringDBForLogs().getExternalMonitoringDB();

        try {
            PrometheusService prometheusService = new PrometheusService(
                    prometheus.getProtocol().toString(), prometheus.getIp(),
                    prometheus.getPort(), prometheus.getUser(),
                    prometheus.getPass(), prometheus.getPath());
            List<PrometheusQueryData> labelsTraces = new ArrayList<>();

            boolean finish = false;

            // LOOP
            while (!finish) {
                Date endExecDate = null;
                if (sharedAsyncModelMap.containsKey(sharedAsyncModelKey)
                        && sharedAsyncModelMap.get(sharedAsyncModelKey)
                                .getData().containsKey(endDateKey)) {
                    try {
                        endExecDate = (Date) sharedAsyncModelMap
                                .get(sharedAsyncModelKey).getData()
                                .get(endDateKey);
                    } catch (Exception e) {
                    }
                }

                // TODO change for filter by labels?
                // String indexes = prometheus.getIndices();

                // if (prometheus.getUseESIndicesByExecution()) {
                // for (Parameter param : sut.getParameters()) {
                // if (param.getName()
                // .equals("EXT_ELASTICSEARCH_LOGS_INDICES")) {
                // indexes = param.getValue();
                // logger.debug("Indexes as String: {}", indexes);
                // break;
                // }
                // }
                // }

                try {
                    Date tmpEndDate = new Date();
                    labelsTraces = prometheusService.searchTraces(startDate,
                            tmpEndDate);

                    // On next iteration startDate from currentEndDate
                    startDate = tmpEndDate;

                    for (PrometheusQueryData labelTraces : labelsTraces) {
                        Map<String, Object> additionalFields = new HashMap<>();
                        additionalFields.put("exec", monitoringIndex);
                        additionalFields.put("component", "sut");

                        List<Map<String, Object>> traces = tracesService
                                .convertExternalPrometheusTrace(labelTraces,
                                        additionalFields);

                        // Only works in mini... TODO send to Logstash
                        tracesService.processBeatTracesList(traces, false);
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

                if (endExecDate != null && labelsTraces.size() == 0) {
                    finish = true;
                }
            } // End Loop
        } catch (Exception e) {
            logger.error("Error on connect to external Elasticsearch", e);
        }

        stopManageSutByExternalPrometheusForMetrics(sharedAsyncModelMap,
                sharedAsyncModelKey);

        return new AsyncResult<Void>(null);
    }

    public void stopManageSutByExternalPrometheusForMetrics(
            Map<String, SharedAsyncModel<Void>> asyncExternalElasticsearchSutExecs,
            String mapKey) {
        // if (!asyncExternalElasticsearchSutExecs.containsKey(mapKey)) {
        // return;
        // }
        // Future<Void> asyncExec =
        // asyncExternalElasticsearchSutExecs.get(mapKey)
        // .getFuture();
        //
        // try {
        // asyncExec.cancel(true);
        // asyncExternalElasticsearchSutExecs.remove(mapKey);
        // logger.info("Stopped Async Manage Sut by external Elasticsearch {}",
        // mapKey);
        // } catch (Exception e) {
        // logger.info(
        // "Error during stop Manage Sut by external Elasticsearch {}",
        // mapKey, e);
        // }

    }

}
