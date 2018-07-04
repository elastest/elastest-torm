package io.elastest.etm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ImageInfo;

import io.elastest.etm.dao.LogAnalyzerRepository;
import io.elastest.etm.model.ContextInfo;
import io.elastest.etm.model.HelpInfo;
import io.elastest.etm.model.LogAnalyzerConfig;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.VersionInfo;

@Service
public class EtmContextService {
    public static final String EUS_TSS_ID = "29216b91-497c-43b7-a5c4-6613f13fa0e9";
    private static final Logger logger = LoggerFactory
            .getLogger(EtmContextService.class);
    private final LogAnalyzerRepository logAnalyzerRepository;

    EsmService esmService;
    EtmContextAuxService etmContextAuxService;
    DockerEtmService dockerEtmService;

    @Value("${et.public.host}")
    public String etPublicHost;
    @Value("${et.in.prod}")
    public boolean etInProd;
    @Value("${et.etm.rabbit.path.with-proxy}")
    public String etEtmRabbitPathWithProxy;

    @Value("${exec.mode}")
    String execMode;

    @Value("${et.images}")
    String etImages;
    @Value("${et.core.images}")
    String etCoreImages;

    @Value("${et.esm.ss.desc.files.path}")
    public String etEsmSsDescFilesPath;

    @Value("${server.port}")
    public String serverPort;
    @Value("${elastest.docker.network}")
    private String etDockerNetwork;
    @Value("${et.edm.alluxio.api}")
    public String etEdmAlluxioApi;
    @Value("${et.edm.mysql.host}")
    public String etEdmMysqlHost;
    @Value("${et.edm.mysql.port}")
    public String etEdmMysqlPort;
    @Value("${et.edm.api}")
    public String etEdmApi;
    @Value("${et.epm.api}")
    public String etEpmApi;
    @Value("${et.etm.api}")
    public String etEtmApi;
    @Value("${et.esm.api}")
    public String etEsmApi;
    @Value("${et.eim.api}")
    public String etEimApi;

    @Value("${et.etm.rabbit.host}")
    public String etEtmRabbitHost;
    @Value("${et.etm.rabbit.port}")
    public String etEtmRabbitPort;
    @Value("${et.emp.api}")
    public String etEmpApi;
    @Value("${et.emp.influxdb.api}")
    public String etEmpInfluxdbApi;
    @Value("${et.emp.influxdb.host}")
    public String etEmpInfluxdbHost;
    @Value("${et.emp.influxdb.graphite.port}")
    public String etEmpInfluxdbGraphitePort;

    // Logstash
    @Value("${et.etm.lsbeats.host}")
    public String etEtmLsBeatsHost;
    @Value("${et.etm.lsbeats.port}")
    public String etEtmLsBeatsPort;
    @Value("${et.etm.binded.lstcp.port}")
    public String etEtmBindedLstcpPort;
    @Value("${et.etm.binded.lsbeats.port}")
    public String etEtmBindedLsbeatsPort;
    @Value("${et.etm.binded.internal.lsbeats.port}")
    public String etEtmBindedInternalLsbeatsPort;
    @Value("${et.etm.lshttp.api}")
    public String etEtmLsHttpApi;
    @Value("${et.etm.lshttp.port}")
    public String etEtmLsHttpPort;
    @Value("${et.etm.lstcp.host}")
    public String etEtmLsTcpHost;
    @Value("${et.etm.lstcp.port}")
    public String etEtmLsTcpPort;

    HelpInfo helpInfo;

    public EtmContextService(LogAnalyzerRepository logAnalyzerRepository,
            EsmService esmService, EtmContextAuxService etmContextAuxService,
            DockerEtmService dockerEtmService) {
        this.logAnalyzerRepository = logAnalyzerRepository;
        this.esmService = esmService;
        this.etmContextAuxService = etmContextAuxService;
        this.dockerEtmService = dockerEtmService;
    }

    public ContextInfo getContextInfo() {
        ContextInfo contextInfo = this.etmContextAuxService.getContextInfo();
        contextInfo.setEusSSInstance(getEusApiUrl());

        return contextInfo;
    }

    private SupportServiceInstance getEusApiUrl() {
        SupportServiceInstance eusInstance = null;
        if (esmService.getServicesInstances() != null) {
            for (Map.Entry<String, SupportServiceInstance> entry : esmService
                    .getServicesInstances().entrySet()) {
                if (entry.getValue().getService_id().equals(EUS_TSS_ID)) {
                    eusInstance = entry.getValue();
                    break;
                }
            }
        }
        return eusInstance;
    }

    public HelpInfo getHelpInfo() {
        if (helpInfo == null) {
            loadHelpInfoFromImages();
        }
        return helpInfo;
    }

    private void loadHelpInfoFromImages() {
        List<String> imagesNames = Arrays.asList(etImages.split(","));
        helpInfo = new HelpInfo();

        imagesNames.forEach((imageName) -> {
            try {
                VersionInfo imageVersionInfo = getImageVersionInfo(imageName);
                helpInfo.getVersionsInfo().put(imageName, imageVersionInfo);
            } catch (Exception e) {
                logger.error("Unable to retrieve ElasTest Help Information.");
            }
        });
    }

    private VersionInfo getImageVersionInfo(String imageName) throws Exception {
        ImageInfo imageInfo = dockerEtmService.dockerService
                .getImageInfoByName(imageName);
        return new VersionInfo(imageInfo.config().labels().get("git_commit"),
                imageInfo.config().labels().get("commit_date"),
                imageInfo.config().labels().get("version"));
    }

    private VersionInfo getImageVersionInfoByContainer(Container container) {
        return new VersionInfo(container.labels().get("git_commit"),
                container.labels().get("commit_date"),
                container.labels().get("version"));
    }

    /* ********************* */
    /* *** Core Services *** */
    /* ********************* */

    public List<CoreServiceInfo> getCoreServicesInfo() {
        List<CoreServiceInfo> coreServices = new ArrayList<>();
        List<String> imagesNames = Arrays.asList(etCoreImages.split(","));
        imagesNames.forEach((imageName) -> {
            try {
                CoreServiceInfo coreService = new CoreServiceInfo();
                String version = dockerEtmService.dockerService
                        .getTagByCompleteImageName(imageName);
                Container container;
                if (version.equals("unspecified")) {
                    container = dockerEtmService.dockerService
                            .getRunningContainersByImageName(imageName).get(0);
                } else {
                    container = dockerEtmService.dockerService
                            .getRunningContainersByImageNameAndVersion(
                                    imageName, version)
                            .get(0);
                }
                String serviceName = imageName.split("/")[1].split(":")[0];
                coreService.setName(serviceName);

                VersionInfo versionInfo = getImageVersionInfoByContainer(
                        container);
                versionInfo.setTag(version);

                coreService.setVersionInfo(versionInfo);

                coreService.setImageName(dockerEtmService.dockerService
                        .getImageNameByCompleteImageName(imageName));
                coreService.setDataByContainer(container);

                coreServices.add(coreService);
            } catch (Exception e) {
                logger.error(
                        "Unable to retrieve ElasTest Core Service {} Information. Probably not started. Obtaining information from Image",
                        imageName);

                CoreServiceInfo coreService = new CoreServiceInfo();
                String version = dockerEtmService.dockerService
                        .getTagByCompleteImageName(imageName);
                VersionInfo versionInfo;
                try {
                    versionInfo = getImageVersionInfo(imageName);
                    versionInfo.setTag(version);

                    String serviceName = imageName.split("/")[1].split(":")[0];
                    coreService.setName(serviceName);

                    coreService.setVersionInfo(versionInfo);

                    coreService.setImageName(dockerEtmService.dockerService
                            .getImageNameByCompleteImageName(imageName));
                    coreService.setStatus("Not Started");

                    coreServices.add(coreService);
                } catch (Exception e1) {
                    logger.error(
                            "Unable to retrieve ElasTest Core Service {} Information Definitively",
                            imageName);
                }
            }
        });
        return coreServices;
    }

    public String getAllCoreServiceLogs(String coreServiceName,
            boolean withFollow) throws Exception {
        CoreServiceInfo coreService = getCoreServiceIfExist(coreServiceName);
        if (coreService != null) {
            String containerName = coreService.getFirstContainerNameCleaned();
            if (containerName != null) {
                return this.dockerEtmService.dockerService
                        .getAllContainerLogs(containerName, withFollow);
            }
        }
        throw new Exception("Error on get " + coreServiceName
                + " logs. Invalid Core Service Name");
    }

    public String getSomeCoreServiceLogs(String coreServiceName, int amount,
            boolean withFollow) throws Exception {
        CoreServiceInfo coreService = getCoreServiceIfExist(coreServiceName);
        if (coreService != null) {
            String containerName = coreService.getFirstContainerNameCleaned();
            if (containerName != null) {
                return this.dockerEtmService.dockerService.getSomeContainerLogs(
                        containerName, amount, withFollow);
            }
        }
        return null;
    }

    public String getCoreServiceLogsSince(String coreServiceName, int since,
            boolean withFollow) throws Exception {
        CoreServiceInfo coreService = getCoreServiceIfExist(coreServiceName);
        if (coreService != null) {
            String containerName = coreService.getFirstContainerNameCleaned();
            if (containerName != null) {
                return this.dockerEtmService.dockerService
                        .getContainerLogsSinceDate(containerName, since,
                                withFollow);
            }
        }
        throw new Exception("Error on get " + coreServiceName
                + " logs. Invalid Core Service Name");
    }

    public CoreServiceInfo getCoreServiceIfExist(String coreServiceName) {
        List<CoreServiceInfo> coreServices = this.getCoreServicesInfo();
        for (CoreServiceInfo currentCoreService : coreServices) {
            if (currentCoreService.getName().equals(coreServiceName)) {
                return currentCoreService;
            }
        }
        return null;
    }

    public boolean isPlatformDevImage(String imageName, String version) {
        return isPlatformImage(imageName) && version.equals("dev");
    }

    public boolean isPlatformImage(String imageName) {
        return imageName.startsWith("elastest/platform")
                && !imageName.startsWith("elastest/platform-services");
    }

    /* ******************** */
    /* *** Log Analyzer *** */
    /* ******************** */

    public Map<String, String> getTJobExecMonitoringEnvVars(
            TJobExecution tJobExec) {
        Map<String, String> monEnvs = new HashMap<String, String>();
        monEnvs.putAll(this.etmContextAuxService.getMonitoringEnvVars());

        if (tJobExec != null) {
            monEnvs.put("ET_MON_LOG_TAG", "sut_" + tJobExec.getId() + "_exec");
            monEnvs.put("ET_SUT_CONTAINER_NAME", "sut_" + tJobExec.getId());
            monEnvs.put("ET_MON_EXEC", tJobExec.getId().toString());
            if (tJobExec.getTjob().isExternal()) {
                monEnvs.put("ET_SUT_LOG_TAG",
                        "sut_" + tJobExec.getId() + "_exec");
                // Override
                monEnvs.put("ET_MON_LSHTTP_API",
                        "http://" + etPublicHost + ":" + etEtmLsHttpPort);
                monEnvs.put("ET_MON_LSBEATS_HOST", etPublicHost);
                monEnvs.put("ET_MON_LSBEATS_PORT", etEtmBindedLsbeatsPort);
                monEnvs.put("ET_MON_INTERNAL_LSBEATS_PORT",
                        etEtmBindedInternalLsbeatsPort);
                monEnvs.put("ET_MON_LSTCP_HOST", etPublicHost);
                monEnvs.put("ET_MON_LSTCP_PORT", etEtmBindedLstcpPort);
            }
        }

        return monEnvs;
    }

    public LogAnalyzerConfig saveLogAnalyzerConfig(
            LogAnalyzerConfig logAnalizerConfig) {
        if (logAnalizerConfig.getId() == 0) {
            logAnalizerConfig.setId(new Long(1));
        }

        return this.logAnalyzerRepository.save(logAnalizerConfig);
    }

    public LogAnalyzerConfig getLogAnalyzerConfig() {
        Optional<LogAnalyzerConfig> config = this.logAnalyzerRepository
                .findById(new Long(1));
        return config.isPresent() ? config.get() : null;
    }

}
