package io.elastest.etm.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;

import io.elastest.etm.model.ContextInfo;
import io.elastest.etm.model.HelpInfo;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.VersionInfo;

@Service
public class EtmContextService {
    public static final String EUS_TSS_ID = "29216b91-497c-43b7-a5c4-6613f13fa0e9";
    private static final Logger logger = LoggerFactory
            .getLogger(EtmContextService.class);

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

    HelpInfo helpInfo;

    public ContextInfo getContextInfo() {
        ContextInfo contextInfo = new ContextInfo();
        contextInfo.setElasticSearchUrl(
                etInProd ? "http://" + publicHost + ":37000/elasticsearch"
                        : elasticsearchApi);
        contextInfo.setRabbitPath(etInProd ? etEtmRabbitPathWithProxy : "");
        contextInfo.setElasTestExecMode(execMode);
        contextInfo.setEusSSInstance(getEusApiUrl());
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
        List<String> imagesNames = Arrays
                .asList(etImages.split(","));
        helpInfo = new HelpInfo();

        imagesNames.forEach((imageName) -> {
            try{
                logger.info("Image name {}.", imageName);
                InspectImageResponse imageInfo = dockerService
                        .getImageInfoByName(imageName);
                logger.info("Image commit {}.", imageInfo.getConfig().getLabels().get("git_commit"));
                logger.info("Commit date {}.", imageInfo.getConfig().getLabels().get("commit_date"));
                logger.info("Version name {}.", imageInfo.getConfig().getLabels().get("version"));
                VersionInfo imageVersionInfo = new VersionInfo(
                        imageInfo.getConfig().getLabels().get("git_commit"),
                        imageInfo.getConfig().getLabels().get("commit_date"),
                        imageInfo.getConfig().getLabels().get("version"));
                helpInfo.getVersionsInfo().put(imageName, imageVersionInfo);
            }catch (Exception e){
                logger.error("Unable to retrieve ElasTest Help Information.");
            }
        });
    }

}
