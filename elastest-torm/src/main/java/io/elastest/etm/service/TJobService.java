package io.elastest.etm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.http.HTTPException;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.xml.sax.SAXException;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.model.EimMonitoringConfig.BeatsStatusEnum;
import io.elastest.etm.model.Enums.MonitoringStorageType;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.MultiConfig;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJob.TJobCompleteView;
import io.elastest.etm.model.TJob.TJobMediumView;
import io.elastest.etm.model.TJob.TJobMinimalView;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobExecution.TypeEnum;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.utils.TestResultParser;
import io.elastest.etm.utils.UtilsService;

@Service
public class TJobService {
    private static final Logger logger = LoggerFactory
            .getLogger(TJobService.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${registry.contextPath}")
    private String registryContextPath;

    private final TJobRepository tJobRepo;
    private final TJobExecRepository tJobExecRepositoryImpl;
    public final TJobExecOrchestratorService tJobExecOrchestratorService;
    private final EsmService esmService;
    private DatabaseSessionManager dbmanager;
    private UtilsService utilsService;
    private AbstractMonitoringService monitoringService;

    Map<String, Future<Void>> asyncExecs = new HashMap<String, Future<Void>>();

    public TJobService(TJobRepository tJobRepo,
            TJobExecRepository tJobExecRepositoryImpl,
            TJobExecOrchestratorService epmIntegrationService,
            EsmService esmService, DatabaseSessionManager dbmanager,
            UtilsService utilsService,
            AbstractMonitoringService monitoringService) {
        super();
        this.tJobRepo = tJobRepo;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.tJobExecOrchestratorService = epmIntegrationService;
        this.esmService = esmService;
        this.dbmanager = dbmanager;
        this.utilsService = utilsService;
        this.monitoringService = monitoringService;
    }

    @PreDestroy
    private void preDestroy() {
        dbmanager.bindSession();
        this.stopAllRunningTJobs();
        dbmanager.unbindSession();
    }

    public void stopAllRunningTJobs() {
        logger.info("Stopping non-finished TJobExecutions ({} total)",
                asyncExecs.size());
        Map<String, Future<Void>> copyOfAsyncExecs = new HashMap<String, Future<Void>>(
                asyncExecs);
        for (HashMap.Entry<String, Future<Void>> currentExecMap : copyOfAsyncExecs
                .entrySet()) {
            Long currentExecId = getTJobExecByMapName(currentExecMap.getKey());
            logger.info("Stopping TJobExecution with id {}", currentExecId);
            this.stopTJobExec(currentExecId);
        }

        logger.info("End Stopping non-finished TJobExecutions");
    }

    public TJob createTJob(TJob tjob) {
        return tJobRepo.save(tjob);
    }

    public void deleteTJob(Long tJobId) {
        TJob tJob = tJobRepo.findById(tJobId).get();
        monitoringService.deleteMonitoringDataByIndicesAsync(
                tJob.getAllMonitoringIndices());
        tJobRepo.delete(tJob);
    }

    public List<TJob> getAllTJobs() {
        return tJobRepo.findAll();
    }

    public String getMapNameByTJobExec(TJobExecution tJobExec) {
        return tJobExec.getTjob().getId() + "_" + tJobExec.getId();
    }

    public Long getTJobExecByMapName(String mapName) {
        return Long.parseLong(mapName.split("_")[1]);
    }

    /* *** Execute TJob *** */

    public TJobExecution executeTJob(Long tJobId,
            List<Parameter> tJobParameters, List<Parameter> sutParameters,
            List<MultiConfig> multiConfigs) throws HttpClientErrorException {
        TJob tJob = tJobRepo.findById(tJobId).get();

        SutSpecification sut = tJob.getSut();

        // Checks if has Sut instrumented by ElasTest and is instrumentalizing
        // yet
        if (sut != null && sut.isInstrumentedByElastest()
                && sut.isInstrumentalize()
                && (sut.getEimConfig() == null || (sut.getEimConfig() != null
                        && sut.getEimConfig().getAgentId() == null))
        // && sut.getEimMonitoringConfig() != null
        // && sut.getEimMonitoringConfig()
        // .getBeatsStatus() == BeatsStatusEnum.ACTIVATING
        ) {
            throw new HttpClientErrorException(HttpStatus.ACCEPTED);
        }

        TJobExecution tJobExec = new TJobExecution();

        if (utilsService.isElastestMini()) {
            tJobExec.setMonitoringStorageType(MonitoringStorageType.MYSQL);
        } else {
            tJobExec.setMonitoringStorageType(
                    MonitoringStorageType.ELASTICSEARCH);
        }

        tJobExec.setStartDate(new Date());
        if (tJob.getSut() != null && sutParameters != null
                && !sutParameters.isEmpty()) {
            tJob.getSut().setParameters(sutParameters);
        }
        tJobExec.setTjob(tJob);
        if (tJobParameters != null && !tJobParameters.isEmpty()) {
            tJobExec.setParameters(tJobParameters);
        }

        tJobExec.setType(TypeEnum.SIMPLE);
        if (multiConfigs != null && multiConfigs.size() > 0 && tJob.isMulti()) {
            tJobExec.setMultiConfigurations(multiConfigs);
            tJobExec.setType(TypeEnum.PARENT);
        }

        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        // After first save, get real Id
        tJobExec.generateMonitoringIndex();
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        Future<Void> asyncExec;
        if (!tJob.isExternal()) {
            asyncExec = tJobExecOrchestratorService.executeTJob(tJobExec,
                    tJob.getSelectedServices());
            asyncExecs.put(getMapNameByTJobExec(tJobExec), asyncExec);
        } else {
            tJobExecOrchestratorService.executeExternalJob(tJobExec, false);
        }

        return tJobExec;
    }

    public void removeOldTJobExecs(TJob tJob) {
        if (tJob != null && tJob.getMaxExecutions() > 0) {
            List<TJobExecution> lastExecs = getLastNTJobExecs(tJob.getId(),
                    tJob.getMaxExecutions());
            if (lastExecs.size() == tJob.getMaxExecutions()) {
                List<TJobExecution> execsToRemove = tJobExecRepositoryImpl
                        .findByTJobIdAndIdLessThan(tJob.getId(),
                                lastExecs.get(lastExecs.size() - 1).getId());
                this.deleteTJobExecs(execsToRemove);
            }
        }
    }

    public void removeOldTJobExecs(Long tJobId) {
        TJob tJob = tJobRepo.findById(tJobId).get();
        removeOldTJobExecs(tJob);
    }

    @Async
    public void removeOldTJobExecsAsync(Long tJobId) {
        removeOldTJobExecs(tJobId);
    }

    /* *** Execute Jenkins TJob *** */

    public TJobExecution executeTJob(ExternalJob externalJob, Long tJobId,
            List<Parameter> parameters, List<Parameter> sutParameters)
            throws HttpClientErrorException {

        // TODO MultiTJob
        TJob tJob = tJobRepo.findById(tJobId).get();

        SutSpecification sut = tJob.getSut();
        // Checks if has sut instrumented by elastest and beats status is
        // activating yet
        if (sut != null && sut.isInstrumentedByElastest()
                && sut.getEimMonitoringConfig() != null
                && sut.getEimMonitoringConfig()
                        .getBeatsStatus() == BeatsStatusEnum.ACTIVATING) {
            throw new HttpClientErrorException(HttpStatus.ACCEPTED);
        }

        TJobExecution tJobExec = new TJobExecution();

        if (utilsService.isElastestMini()) {
            tJobExec.setMonitoringStorageType(MonitoringStorageType.MYSQL);
        } else {
            tJobExec.setMonitoringStorageType(
                    MonitoringStorageType.ELASTICSEARCH);
        }

        tJobExec.setStartDate(new Date());
        if (tJob.getSut() != null && sutParameters != null
                && !sutParameters.isEmpty()) {
            tJob.getSut().setParameters(sutParameters);
        }
        tJobExec.setTjob(tJob);
        if (parameters != null && !parameters.isEmpty()) {
            tJobExec.setParameters(parameters);
        }
        if (externalJob.getBuildUrl() != null
                && !externalJob.getBuildUrl().isEmpty()) {
            tJobExec.getExternalUrls().put("jenkins-build-url",
                    externalJob.getBuildUrl());
        }
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        // After first save, get real Id
        tJobExec.generateMonitoringIndex();
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        tJobExecOrchestratorService.executeExternalJob(tJobExec,
                externalJob.isFromIntegratedJenkins());
        return tJobExec;
    }

    public TJobExecution stopTJobExec(Long tJobExecId) {
        TJobExecution tJobExec = tJobExecRepositoryImpl.findById(tJobExecId)
                .get();
        String mapKey = getMapNameByTJobExec(tJobExec);

        if (tJobExec.isMultiExecutionChild()) {
            return stopTJobExec(tJobExec.getExecParent().getId());
        }

        Future<Void> asyncExec = asyncExecs.get(mapKey);

        boolean cancelExecuted = false;

        try {
            cancelExecuted = asyncExec.cancel(true);
            // If is not cancelled, stop async Exec and stop services
            if (cancelExecuted) {
                logger.info("Forcing Execution Stop");
                try {
                    tJobExec = tJobExecOrchestratorService
                            .forceEndExecution(tJobExec);
                    logger.info("Execution Stopped Successfully!");
                } catch (Exception e) {
                    logger.error("Error on forcing Execution stop");
                }
            } else { // If is already finished, gets TJobExec
                tJobExec = tJobExecRepositoryImpl.findById(tJobExecId).get();
            }
            asyncExecs.remove(mapKey);
        } catch (Exception e) {
            logger.info("Error during forcing stop", e);
        }
        return tJobExec;
    }

    public void endExternalTJobExecution(long tJobExecId, int result,
            List<String> testResultsReportsAsString) {
        logger.info("Finishing the external TJob.");
        TJobExecution tJobExec = this.getTJobExecById(tJobExecId);

        // Parsing test results
        List<ReportTestSuite> testResultsReports = new ArrayList<>();
        TestResultParser testResultParser = new TestResultParser();
        if (testResultsReportsAsString != null) {
            for (String testSuite : testResultsReportsAsString) {
                try {
                    testResultsReports.add(testResultParser
                            .testSuiteStringToReportTestSuite(testSuite));
                } catch (ParserConfigurationException | SAXException
                        | IOException e) {
                    // TODO Create a manual TestSuite with an error message
                    logger.error("Error on parse testSuite {}", e);
                }
            }

            tJobExecOrchestratorService.saveTestResults(testResultsReports,
                    tJobExec);
        }
        try {
            tJobExecOrchestratorService.deprovisionServices(tJobExec);
        } catch (Exception e) {
            logger.error(
                    "Exception during desprovisioning of the TSS associated with an External TJob.");
        }
        if (tJobExec.isWithSut()) {
            try {
                Execution dockerExec = new Execution(tJobExec);
                dockerExec.setSutExec(tJobExec.getSutExecution());
                tJobExecOrchestratorService.endDockbeatExec(dockerExec, true);
                tJobExecOrchestratorService.endSutExec(dockerExec, false);
            } catch (Exception e) {
                logger.error(
                        "Error desprovisioning a SUT used in a TJob run from Jenkins.");
            }
        }

        tJobExec.setResult(ResultEnum.values()[result]);
        tJobExec.setResultMsg("Finished: " + tJobExec.getResult());
        tJobExec.setEndDate(new Date());
        tJobExecOrchestratorService
                .updateManageSutByExtESEndDate(tJobExec.getEndDate(), tJobExec);

        tJobExecRepositoryImpl.save(tJobExec);
    }

    public void deleteTJobExec(TJobExecution tJobExec) {
        String index = tJobExec.getOnlyTJobExecMonitoringIndex();
        monitoringService
                .deleteMonitoringDataByIndicesAsync(Arrays.asList(index));
        tJobExecRepositoryImpl.delete(tJobExec);
    }

    public void deleteTJobExec(Long tJobExecId) {
        TJobExecution tJobExec = tJobExecRepositoryImpl.findById(tJobExecId)
                .get();
        deleteTJobExec(tJobExec);
    }

    public void deleteTJobExecs(List<TJobExecution> tJobExecs) {
        if (tJobExecs != null) {
            for (TJobExecution tJobExec : tJobExecs) {
                try {
                    deleteTJobExec(tJobExec);
                } catch (Exception e) {
                }
            }
        }
    }

    public TJob getTJobById(Long tJobId) {
        return tJobRepo.findById(tJobId).get();
    }

    public TJob getTJobByName(String name) {
        return tJobRepo.findByName(name);
    }

    public List<TJobExecution> getLastNTJobExecs(Long tJobId, Long number) {
        Pageable lastN = PageRequest.of(0, number.intValue(), Direction.DESC,
                "id");
        return tJobExecRepositoryImpl.findByTJobIdWithPageable(tJobId, lastN);
    }

    // All execs of a TJob

    public List<TJobExecution> getTJobExecsByPage(Long tJobId, int page,
            int pageSize, Direction direction, boolean withoutChilds) {
        Pageable range = PageRequest.of(page, pageSize, direction, "id");

        if (withoutChilds) {
            return tJobExecRepositoryImpl.findByTJobIdWithPageable(tJobId,
                    range);
        } else {
            return tJobExecRepositoryImpl
                    .findByTJobIdWithPageableWithoutChilds(tJobId, range);
        }
    }

    /* ****************************** */
    /* *** All execs of all TJobs *** */
    /* ****************************** */

    public List<TJobExecution> getAllTJobExecs() {
        return tJobExecRepositoryImpl.findAll();
    }

    /* ****** By ID ****** */

    public List<TJobExecution> getTJobsExecsByPage(int page, int pageSize,
            Direction direction, boolean withoutChilds) {
        Pageable range = PageRequest.of(page, pageSize, direction, "id");
        if (withoutChilds) {
            return tJobExecRepositoryImpl.findWithPageableWithoutChilds(range);
        } else {
            return tJobExecRepositoryImpl.findWithPageable(range);
        }
    }

    public List<TJobExecution> getLastNTJobsExecs(Long number,
            boolean withoutChilds) {
        return getTJobsExecsByPage(0, number.intValue(), Direction.DESC,
                withoutChilds);
    }

    /* ****** By results ****** */

    public List<TJobExecution> getTJobsExecsPageByResults(int page,
            int pageSize, Direction direction, List<ResultEnum> results,
            boolean withoutChilds) {
        Pageable range = PageRequest.of(page, pageSize, direction, "id");
        if (withoutChilds) {
            return tJobExecRepositoryImpl
                    .findByResultsWithPageableWithoutChilds(results, range);
        } else {
            return tJobExecRepositoryImpl.findByResultsWithPageable(results,
                    range);
        }
    }

    public List<TJobExecution> getTJobsExecsPageByResultsAndIdLessThan(int page,
            int pageSize, Direction direction, List<ResultEnum> results,
            Long id, boolean withoutChilds) {
        Pageable range = PageRequest.of(page, pageSize, direction, "id");
        if (withoutChilds) {
            return tJobExecRepositoryImpl
                    .findByResultsAndIdLessThanWithPageableWithoutChilds(
                            results, id, range);
        } else {
            return tJobExecRepositoryImpl
                    .findByResultsAndIdLessThanWithPageable(results, id, range);
        }
    }

    public List<TJobExecution> getTJobsExecsPageByResultsAndIdGreaterThan(
            int page, int pageSize, Direction direction,
            List<ResultEnum> results, Long id, boolean withoutChilds) {
        Pageable range = PageRequest.of(page, pageSize, direction, "id");
        if (withoutChilds) {
            return tJobExecRepositoryImpl
                    .findByResultsAndIdGreaterThanWithPageableWithoutChilds(
                            results, id, range);
        } else {
            return tJobExecRepositoryImpl
                    .findByResultsAndIdGreaterThanWithPageable(results, id,
                            range);
        }
    }

    /* *** Running *** */

    public List<TJobExecution> getAllRunningTJobsExecs(boolean withoutChilds) {
        List<ResultEnum> notFinishedOrExecutedResultList = ResultEnum
                .getNotFinishedOrExecutedResultList();
        if (withoutChilds) {
            return this.tJobExecRepositoryImpl.findByResultsWithoutChilds(
                    notFinishedOrExecutedResultList);
        } else {
            return this.tJobExecRepositoryImpl
                    .findByResults(notFinishedOrExecutedResultList);
        }
    }

    public List<TJobExecution> getAllRunningTJobsExecsByIdLessThan(Long id,
            boolean withoutChilds) {
        List<ResultEnum> notFinishedOrExecutedResultList = ResultEnum
                .getNotFinishedOrExecutedResultList();
        if (withoutChilds) {
            return this.tJobExecRepositoryImpl
                    .findByResultsAndIdLessThanWithoutChilds(
                            notFinishedOrExecutedResultList, id);
        } else {
            return this.tJobExecRepositoryImpl.findByResultsAndIdLessThan(
                    notFinishedOrExecutedResultList, id);
        }
    }

    public List<TJobExecution> getAllRunningTJobsExecsByIdGreaterThan(Long id,
            boolean withoutChilds) {
        List<ResultEnum> notFinishedOrExecutedResultList = ResultEnum
                .getNotFinishedOrExecutedResultList();
        if (withoutChilds) {
            return this.tJobExecRepositoryImpl
                    .findByResultsAndIdGreaterThanWithoutChilds(
                            notFinishedOrExecutedResultList, id);
        } else {
            return this.tJobExecRepositoryImpl.findByResultsAndIdGreaterThan(
                    notFinishedOrExecutedResultList, id);
        }
    }

    /* *** */

    public List<TJobExecution> getRunningTJobExecsByPage(int page, int pageSize,
            Direction dir, boolean withoutChilds) {
        return getTJobsExecsPageByResults(page, pageSize, dir,
                ResultEnum.getNotFinishedOrExecutedResultList(), withoutChilds);
    }

    public List<TJobExecution> getRunningTJobExecsByPageAndIdLessThan(int page,
            int pageSize, Long id, Direction dir, boolean withoutChilds) {
        return getTJobsExecsPageByResultsAndIdLessThan(page, pageSize, dir,
                ResultEnum.getNotFinishedOrExecutedResultList(), id,
                withoutChilds);
    }

    public List<TJobExecution> getRunningTJobExecsByPageAndIdGreaterThan(
            int page, int pageSize, Long id, Direction dir,
            boolean withoutChilds) {
        return getTJobsExecsPageByResultsAndIdGreaterThan(page, pageSize, dir,
                ResultEnum.getNotFinishedOrExecutedResultList(), id,
                withoutChilds);
    }

    /* *** */

    public List<TJobExecution> getLastNRunningTJobExecs(Long number,
            boolean withoutChilds) {
        return getTJobsExecsPageByResults(0, number.intValue(), Direction.DESC,
                ResultEnum.getNotFinishedOrExecutedResultList(), withoutChilds);
    }

    /* *** Finished *** */

    public List<TJobExecution> getAllFinishedOrNotExecutedTJobExecs(
            boolean withoutChilds) {
        List<ResultEnum> finishedAndNotExecutedResultList = ResultEnum
                .getFinishedAndNotExecutedResultList();
        if (withoutChilds) {
            return this.tJobExecRepositoryImpl.findByResultsWithoutChilds(
                    finishedAndNotExecutedResultList);
        } else {
            return this.tJobExecRepositoryImpl
                    .findByResults(finishedAndNotExecutedResultList);
        }
    }

    public List<TJobExecution> getAllFinishedOrNotExecutedTJobsExecsByIdLessThan(
            Long id, boolean withoutChilds) {
        List<ResultEnum> finishedAndNotExecutedResultList = ResultEnum
                .getFinishedAndNotExecutedResultList();
        if (withoutChilds) {
            return this.tJobExecRepositoryImpl
                    .findByResultsAndIdLessThanWithoutChilds(
                            finishedAndNotExecutedResultList, id);
        } else {
            return this.tJobExecRepositoryImpl.findByResultsAndIdLessThan(
                    finishedAndNotExecutedResultList, id);
        }
    }

    public List<TJobExecution> getAllFinishedOrNotExecutedTJobsExecsByIdGreaterThan(
            Long id, boolean withoutChilds) {
        List<ResultEnum> finishedAndNotExecutedResultList = ResultEnum
                .getFinishedAndNotExecutedResultList();
        if (withoutChilds) {
            return this.tJobExecRepositoryImpl
                    .findByResultsAndIdGreaterThanWithoutChilds(
                            finishedAndNotExecutedResultList, id);
        } else {
            return this.tJobExecRepositoryImpl.findByResultsAndIdGreaterThan(
                    finishedAndNotExecutedResultList, id);
        }
    }

    /* *** */

    public List<TJobExecution> getFinishedOrNotExecutedTJobsExecsByPage(
            int page, int pageSize, Direction dir, boolean withoutChilds) {
        return getTJobsExecsPageByResults(page, pageSize, dir,
                ResultEnum.getFinishedAndNotExecutedResultList(),
                withoutChilds);
    }

    public List<TJobExecution> getFinishedOrNotExecutedTJobsExecsByPageAndIdLessThan(
            int page, int pageSize, Long id, Direction dir,
            boolean withoutChilds) {
        return getTJobsExecsPageByResultsAndIdLessThan(page, pageSize, dir,
                ResultEnum.getFinishedAndNotExecutedResultList(), id,
                withoutChilds);
    }

    public List<TJobExecution> getFinishedOrNotExecutedTJobsExecsByPageAndIdGreaterThan(
            int page, int pageSize, Long id, Direction dir,
            boolean withoutChilds) {
        return getTJobsExecsPageByResultsAndIdGreaterThan(page, pageSize, dir,
                ResultEnum.getFinishedAndNotExecutedResultList(), id,
                withoutChilds);
    }

    /* *** */

    public List<TJobExecution> getLastNFinishedOrNotExecutedTJobsExecs(
            Long number, boolean withoutChilds) {
        return getTJobsExecsPageByResults(0, number.intValue(), Direction.DESC,
                ResultEnum.getFinishedAndNotExecutedResultList(),
                withoutChilds);
    }

    /* *** ************ *** */

    public TJobExecution getTJobExecById(Long id) {
        return tJobExecRepositoryImpl.findById(id).get();
    }

    public List<TJobExecution> getTJobExecutionsByTJobId(Long tJobId,
            boolean withoutChilds) {
        if (withoutChilds) {
            return tJobExecRepositoryImpl.findByTJobIdWithoutChilds(tJobId);
        } else {
            TJob tJob = tJobRepo.findById(tJobId).get();
            return getTJobsExecutionsByTJob(tJob);
        }
    }

    public List<TJobExecution> getTJobsExecutionsByTJob(TJob tJob) {
        return tJobExecRepositoryImpl.findByTJob(tJob);
    }

    public TJobExecution getTJobsExecution(Long tJobId, Long tJobExecId) {
        TJob tJob = tJobRepo.findById(tJobId).get();
        return tJobExecRepositoryImpl.findByIdAndTJob(tJobExecId, tJob);
    }

    public TJob modifyTJob(TJob tJob) throws RuntimeException {
        if (tJobRepo.findById(tJob.getId()) != null) {
            return tJobRepo.save(tJob);
        } else {
            throw new HTTPException(405);
        }
    }

    public List<TJobExecutionFile> getTJobExecutionFilesUrls(Long tJobId,
            Long tJobExecId) throws InterruptedException {
        return esmService.getTJobExecutionFilesUrls(tJobId, tJobExecId);
    }

    public String getFileUrl(String serviceFilePath) throws IOException {
        String urlResponse = contextPath.replaceFirst("/", "")
                + registryContextPath + "/"
                + serviceFilePath.replace("\\\\", "/");
        return urlResponse;
    }

    public void getFiles(Long tJobId, Long tJobExecId) {

    }

    public TJobExecution getChildTJobExecParent(Long tJobExecId) {
        TJobExecution tJobExec = this.tJobExecRepositoryImpl
                .findById(tJobExecId).get();
        TJobExecution parent = null;
        if (tJobExec.isMultiExecutionChild()
                && tJobExec.getExecParent() != null) {
            parent = this.tJobExecRepositoryImpl
                    .findById(tJobExec.getExecParent().getId()).get();
        }
        return parent;
    }

    public List<TJobExecution> getParentTJobExecChilds(Long tJobExecId) {
        TJobExecution tJobExec = this.tJobExecRepositoryImpl
                .findById(tJobExecId).get();
        List<TJobExecution> childs = new ArrayList<>();
        if (tJobExec.isMultiExecutionParent()
                && tJobExec.getExecChilds() != null) {
            childs = tJobExec.getExecChilds();
        }
        return childs;
    }

    public Class<? extends TJobMinimalView> getView(String viewType) {
        Class<? extends TJobMinimalView> view = TJobCompleteView.class;

        if (viewType != null) {
            if ("minimal".equals(viewType)) {
                view = TJobMinimalView.class;
            } else if ("medium".equals(viewType)) {
                view = TJobMediumView.class;
            }
        }
        return view;
    }

    public MappingJacksonValue getMappingJacksonValue(Object obj,
            String viewType) {
        final MappingJacksonValue result = new MappingJacksonValue(obj);
        Class<? extends TJobMinimalView> view = getView(viewType);

        result.setSerializationView(view);
        return result;
    }
}