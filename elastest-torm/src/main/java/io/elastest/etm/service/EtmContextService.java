package io.elastest.etm.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.command.InspectImageResponse;

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

    @Autowired
    EsmService esmService;
    @Autowired
    DockerService2 dockerService;

    @Value("${et.edm.elasticsearch.api}")
    public String elasticsearchApi;
    @Value("${et.public.host}")
    public String publicHost;
    @Value("${et.in.prod}")
    public boolean etInProd;
    @Value("${et.etm.rabbit.path.with-proxy}")
    public String etEtmRabbitPathWithProxy;
    @Value("${exec.mode}")
    String execMode;
    @Value("${et.images}")
    String etImages;

    @Value("${et.esm.ss.desc.files.path}")
    public String etEsmSsDescFilesPath;
    @Value("${et.shared.folder}")
    public String etSharedFolder;
    @Value("${et.public.host}")
    public String etPublicHost;
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
    @Value("${et.edm.elasticsearch.api}")
    public String etEdmElasticsearchApi;
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
    @Value("${et.etm.lsbeats.host}")
    public String etEtmLsbeatsHost;
    @Value("${et.etm.lsbeats.port}")
    public String etEtmLsbeatsPort;
    @Value("${et.etm.internal.lsbeats.port}")
    public String etEtmInternalLsbeatsPort;
    @Value("${et.etm.lshttp.api}")
    public String etEtmLshttpApi;
    @Value("${et.etm.lshttp.port}")
    public String etEtmLshttpPort;
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
    @Value("${et.etm.lstcp.host}")
    public String etEtmLstcpHost;
    @Value("${et.etm.lstcp.port}")
    public String etEtmLstcpPort;
    
    @Value("${et.etm.testlink.host}")
    public String etEtmTestLinkHost;

    HelpInfo helpInfo;

    public EtmContextService(LogAnalyzerRepository logAnalyzerRepository) {
        this.logAnalyzerRepository = logAnalyzerRepository;
    }

    public ContextInfo getContextInfo() {
        ContextInfo contextInfo = new ContextInfo();
        contextInfo.setElasticSearchUrl(
                etInProd ? "http://" + publicHost + ":37000/elasticsearch"
                        : elasticsearchApi);
        contextInfo.setRabbitPath(etInProd ? etEtmRabbitPathWithProxy : "");
        contextInfo.setElasTestExecMode(execMode);
        contextInfo.setEusSSInstance(getEusApiUrl());
        contextInfo.setTestLinkStarted(!etEtmTestLinkHost.equals("none") ? true : false);
        return contextInfo;
    }

    private SupportServiceInstance getEusApiUrl() {
        SupportServiceInstance eusInstance = null;
        for (Map.Entry<String, SupportServiceInstance> entry : esmService
                .getServicesInstances().entrySet()) {
            if (entry.getValue().getService_id().equals(EUS_TSS_ID)) {
                eusInstance = entry.getValue();
                break;
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
                InspectImageResponse imageInfo = dockerService
                        .getImageInfoByName(imageName);
                VersionInfo imageVersionInfo = new VersionInfo(
                        imageInfo.getConfig().getLabels().get("git_commit"),
                        imageInfo.getConfig().getLabels().get("commit_date"),
                        imageInfo.getConfig().getLabels().get("version"));
                helpInfo.getVersionsInfo().put(imageName, imageVersionInfo);
            } catch (Exception e) {
                logger.error("Unable to retrieve ElasTest Help Information.");
            }
        });
    }

    public Map<String, String> getMonitoringEnvVars(TJobExecution tJobExec) {
        Map<String, String> monEnvs = new HashMap<String, String>();

        monEnvs.put("ET_MON_LSHTTP_API", etEtmLshttpApi);
        monEnvs.put("ET_MON_LSBEATS_HOST", etEtmLsbeatsHost);
        monEnvs.put("ET_MON_LSBEATS_PORT", etEtmLsbeatsPort);
        monEnvs.put("ET_MON_LSTCP_HOST", etEtmLstcpHost);
        monEnvs.put("ET_MON_LSTCP_PORT", etEtmLstcpPort);
        monEnvs.put("ET_MON_LOG_TAG",
                "sut_" + tJobExec.getId() + "_exec");

        if (tJobExec != null) {
            monEnvs.put("ET_SUT_CONTAINER_NAME", "sut_" + tJobExec.getId());
            monEnvs.put("ET_MON_EXEC", tJobExec.getId().toString());
            if (tJobExec.getTjob().isExternal()) {
                monEnvs.put("ET_SUT_LOG_TAG",
                        "sut_" + tJobExec.getId() + "_exec");
                // Override
                monEnvs.put("ET_SUT_MON_LSHTTP_API",
                        "http://" + etPublicHost + ":" + etEtmLshttpPort);
                monEnvs.put("ET_SUT_MON_LSBEATS_HOST", etPublicHost);
                monEnvs.put("ET_SUT_MON_LSBEATS_PORT", etEtmInternalLsbeatsPort);
                monEnvs.put("ET_SUT_MON_LSTCP_HOST", etPublicHost);
                monEnvs.put("ET_SUT_MON_LSTCP_PORT", etEtmLstcpPort);
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
        return this.logAnalyzerRepository.findOne(new Long(1));
    }

}
