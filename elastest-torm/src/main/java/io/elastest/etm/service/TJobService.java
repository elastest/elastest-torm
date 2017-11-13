package io.elastest.etm.service;

import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;

import java.io.File;
import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobExecutionFile;

@Service
public class TJobService {
    private static final Logger logger = LoggerFactory
            .getLogger(TJobService.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${registry.contextPath}")
    private String registryContextPath;

    @Value("${et.shared.folder}")
    private String sharedFolder;

    private final TJobRepository tJobRepo;
    private final TJobExecRepository tJobExecRepositoryImpl;
    private final TJobExecOrchestratorService tJobExecOrchestratorService;

    Map<String, Future<Void>> asyncExecs = new HashMap<String, Future<Void>>();

    public TJobService(TJobRepository tJobRepo,
            TJobExecRepository tJobExecRepositoryImpl,
            TJobExecOrchestratorService epmIntegrationService) {
        super();
        this.tJobRepo = tJobRepo;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.tJobExecOrchestratorService = epmIntegrationService;
    }

    public TJob createTJob(TJob tjob) {
        return tJobRepo.save(tjob);
    }

    public void deleteTJob(Long tJobId) {
        TJob tJob = tJobRepo.findOne(tJobId);
        tJobRepo.delete(tJob);
    }

    public List<TJob> getAllTJobs() {
        return tJobRepo.findAll();
    }

    public String getMapNameByTJobExec(TJobExecution tJobExec) {
        return tJobExec.getTjob().getId() + "_" + tJobExec.getId();
    }

    public TJobExecution executeTJob(Long tJobId, List<Parameter> parameters) {
        TJob tjob = tJobRepo.findOne(tJobId);
        TJobExecution tJobExec = new TJobExecution();
        tJobExec.setStartDate(new Date());
        tJobExec.setTjob(tjob);
        tJobExec.setParameters(parameters);
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        // After first save, get real Id
        tJobExec.generateLogIndex();
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        if (!tjob.isExternal()) {
            // Run async execution
            Future<Void> asyncExec = tJobExecOrchestratorService
                    .executeTJob(tJobExec, tjob.getSelectedServices());
            asyncExecs.put(getMapNameByTJobExec(tJobExec), asyncExec);
        }

        return tJobExec;
    }

    public TJobExecution stopTJobExec(Long tJobExecId) {
        TJobExecution tJobExec = tJobExecRepositoryImpl.findOne(tJobExecId);
        String mapKey = getMapNameByTJobExec(tJobExec);
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
                tJobExec = tJobExecRepositoryImpl.findOne(tJobExecId);
            }
            asyncExecs.remove(mapKey);
        } catch (Exception e) {
            logger.info("Error during forcing stop", e);
        }
        return tJobExec;
    }

    public void finishTJobExecution(Long tJobExecId) {
        TJobExecution tJobExecution = tJobExecRepositoryImpl
                .findOne(tJobExecId);
        tJobExecution.setResult(ResultEnum.SUCCESS);

        tJobExecRepositoryImpl.save(tJobExecution);
    }

    public void deleteTJobExec(Long tJobExecId) {
        TJobExecution tJobExec = tJobExecRepositoryImpl.findOne(tJobExecId);
        tJobExecRepositoryImpl.delete(tJobExec);
    }

    public TJob getTJobById(Long tJobId) {
        return tJobRepo.findOne(tJobId);
    }

    public TJob getTJobByName(String name) {
        return tJobRepo.findByName(name);
    }

    public List<TJobExecution> getAllTJobExecs() {
        return tJobExecRepositoryImpl.findAll();
    }

    public List<TJobExecution> getTJobsExecutionsByTJobId(Long tJobId) {
        TJob tJob = tJobRepo.findOne(tJobId);
        return getTJobsExecutionsByTJob(tJob);
    }

    public List<TJobExecution> getTJobsExecutionsByTJob(TJob tJob) {
        return tJobExecRepositoryImpl.findByTJob(tJob);
    }

    public TJobExecution getTJobsExecution(Long tJobId, Long tJobExecId) {
        TJob tJob = tJobRepo.findOne(tJobId);
        return tJobExecRepositoryImpl.findByIdAndTJob(tJobExecId, tJob);
    }

    public TJob modifyTJob(TJob tJob) throws RuntimeException {
        if (tJobRepo.findOne(tJob.getId()) != null) {
            return tJobRepo.save(tJob);
        } else {
            throw new HTTPException(405);
        }
    }

    public List<TJobExecutionFile> getTJobExecutionFilesUrls(Long tJobId,
            Long tJobExecId) throws InterruptedException {
        logger.info("Retrived the files generated by the TJob execution: {}",
                tJobExecId);
        List<TJobExecutionFile> filesList = new ArrayList<TJobExecutionFile>();

        TJob tJob = getTJobById(tJobId);
        tJob.getSelectedServices();

        String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
        String tJobExecFilePath = "tjobs" + fileSeparator + "tjob_" + tJobId
                + fileSeparator + "exec_" + tJobExecId + fileSeparator;
        String tJobExecFolder = sharedFolder + tJobExecFilePath;

        logger.debug("Shared folder:" + tJobExecFolder);
        try {
            File file = ResourceUtils.getFile(tJobExecFolder);
            // If not in dev mode
            if (file.exists()) {
                List<String> servicesFolders = new ArrayList<>(
                        Arrays.asList(file.list()));
                for (String serviceFolderName : servicesFolders) {
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ie) {
                        logger.error("Thread sleep fail");
                        throw ie;
                    }
                    logger.debug("Files folder:" + serviceFolderName);
                    logger.debug(
                            "Full path:" + tJobExecFolder + serviceFolderName);
                    File serviceFolder = ResourceUtils
                            .getFile(tJobExecFolder + serviceFolderName);
                    List<String> servicesFilesNames = new ArrayList<>(
                            Arrays.asList(serviceFolder.list()));
                    for (String serviceFileName : servicesFilesNames) {
                        filesList.add(new TJobExecutionFile(serviceFileName,
                                getFileUrl(tJobExecFilePath + serviceFolderName
                                        + fileSeparator + serviceFileName),
                                serviceFolderName));
                    }
                }
            }
        } catch (IOException fnfe) {
            logger.warn(
                    "Error building the URLs of the files of the executio {}",
                    tJobExecId);
        }

        return filesList;
    }

    public String getFileUrl(String serviceFilePath) throws IOException {
        String urlResponse = contextPath.replaceFirst("/", "")
                + registryContextPath + "/"
                + serviceFilePath.replace("\\\\", "/");
        return urlResponse;
    }

    public void getFiles(Long tJobId, Long tJobExecId) {

    }

}