package io.elastest.etm.platform.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ProgressMessage;

import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.json.DockerContainerInfo.DockerContainer;
import io.elastest.epm.client.model.DockerPullImageProgress;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.epm.client.model.DockerServiceStatus.DockerServiceStatusEnum;
import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TJobExecution;

@Service
public class DockerServiceImpl implements PlatformService {
    final Logger logger = getLogger(lookup().lookupClass());

    public DockerComposeService dockerComposeService;
    public DockerEtmService dockerEtmService;

    public DockerServiceImpl(DockerComposeService dockerComposeService,
            DockerEtmService dockerEtmService) {
        super();
        this.dockerComposeService = dockerComposeService;
        this.dockerEtmService = dockerEtmService;
    }

    @Override
    public String undeployTSS() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deploySUT() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String undeploySUT() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TJobExecution deployTJobExecution(TJobExecution tJobExecution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TJobExecution unDeployTJobExecution(TJobExecution tJobExecution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SupportServiceInstance deployTSS(SupportService supportService) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deployTSSs(TJobExecution tJobExecution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean createServiceDeploymentProject(String projectName,
            String serviceDescriptor, String targetPath, boolean override,
            Map<String, String> envs, boolean withBindedExposedPortsToRandom,
            boolean withRemoveVolumes) throws Exception {
        return dockerComposeService.createProjectWithEnv(projectName,
                serviceDescriptor, targetPath, true, envs, false, false);
    }

    @Override
    public List<String> getServiceDeploymentImages(String projectName)
            throws Exception {
        List<String> images = dockerComposeService
                .getProjectImages(projectName);
        return images;
    }

    @Override
    public boolean undeployService(String projectName) throws IOException {
        return dockerComposeService.stopProject(projectName);
    }

    @Override
    public boolean undeployAndCleanDeployment(String projectName) {
        return dockerComposeService.stopAndRemoveProject(projectName);
    }

    @Override
    public boolean deployService(String projectName, boolean withPull)
            throws IOException {
        return dockerComposeService.startProject(projectName, false);
    }

    @Override
    public List<String> getDeploymentImages(String projectName)
            throws Exception {
        return dockerComposeService.getProjectImages(projectName);
    }

    @Override
    public void pullImageWithProgress(String projectName,
            ProgressHandler progressHandler, String image) throws Exception {
        dockerComposeService.pullImageWithProgressHandler(projectName,
                progressHandler, image);

    }

    @Override
    public void pullDeploymentImages(String projectName,
            DockerServiceStatus serviceStatus, List<String> images,
            boolean withProgress) throws Exception {
        for (String image : images) {
            ProgressHandler progressHandler = getEtPluginProgressHandler(
                    projectName, image, serviceStatus);

            pullImageWithProgress(projectName, progressHandler, image);
        }
    }

    @Override
    public DockerContainerInfo getContainers(String projectName) {
        List<Container> containers;
        DockerContainerInfo dockerContainerInfo = new DockerContainerInfo();
        try {
            containers = dockerEtmService.dockerService
                    .getContainersByPrefix(projectName);
            for (Container container : containers) {
                io.elastest.epm.client.json.DockerContainerInfo.DockerContainer dockerContainer = new io.elastest.epm.client.json.DockerContainerInfo.DockerContainer();
                dockerContainer.initFromContainer(container);
                dockerContainerInfo.getContainers().add(dockerContainer);
            }

        } catch (Exception e) {
            logger.error("Error on get containers of project {}", projectName);
        }
        return dockerContainerInfo;
    }

    @Override
    public boolean isContainerIntoNetwork(String networkId, String containerId)
            throws Exception {
        return dockerEtmService.dockerService.isContainerIntoNetwork(networkId,
                containerId);
    }

    @Override
    public String getContainerIpByNetwork(String containerId, String network)
            throws Exception {
        return dockerEtmService.dockerService
                .getContainerIpByNetwork(containerId, network);
    }

    @Override
    public void insertIntoETNetwork(String engineName, String network)
            throws Exception {
        try {
            for (DockerContainer container : getContainers(engineName)
                    .getContainers()) {
                try {
                    dockerEtmService.dockerService.insertIntoNetwork(network,
                            container.getName());
                    try {
                        // Insert into bridge too
                        dockerEtmService.dockerService.insertIntoNetwork(
                                "bridge", container.getName());
                    } catch (Exception e) {
                        logger.error("Error on insert container "
                                + container.getName() + " into bridge network");
                    }
                } catch (InterruptedException | DockerCertificateException e) {
                    throw new Exception(
                            "Error on insert container " + container.getName()
                                    + " into " + network + " network");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getContainerName(String serviceName, String network) {
        String currentContainerName = null;
        try {
            for (Container container : dockerComposeService.dockerService
                    .getAllContainers()) {
                currentContainerName = container.names().get(0); // example:

                if (currentContainerName != null
                        && currentContainerName.endsWith(serviceName + "_1")
                        && isContainerIntoNetwork(network,
                                currentContainerName)) {
                    return currentContainerName;

                }
            }
        } catch (Exception e) {
            logger.error("Error on get {} internal url", serviceName);
        }
        return currentContainerName;
    }

    private ProgressHandler getEtPluginProgressHandler(String projectName,
            String image, DockerServiceStatus serviceToPull) {
        DockerPullImageProgress dockerPullImageProgress = new DockerPullImageProgress();
        dockerPullImageProgress.setImage(image);
        dockerPullImageProgress.setCurrentPercentage(0);

        updateStatus(projectName, DockerServiceStatusEnum.PULLING,
                "Pulling " + image + " image", serviceToPull);
        return new ProgressHandler() {
            @Override
            public void progress(ProgressMessage message)
                    throws DockerException {
                dockerPullImageProgress.processNewMessage(message);
                String msg = "Pulling image " + image + " from " + projectName
                        + " ET Plugin: "
                        + dockerPullImageProgress.getCurrentPercentage() + "%";

                updateStatus(projectName, DockerServiceStatusEnum.PULLING, msg,
                        serviceToPull);
            }

        };

    }

    public void updateStatus(String serviceName, DockerServiceStatusEnum status,
            String statusMsg, DockerServiceStatus serviceToPull)
            throws NotFoundException {
        serviceToPull.setStatus(status);
        serviceToPull.setStatusMsg(statusMsg);
    }

}
