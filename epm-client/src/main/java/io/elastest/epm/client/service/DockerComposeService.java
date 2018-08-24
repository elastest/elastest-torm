/*
 * (C) Copyright 2017-2019 ElasTest (http://elastest.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.elastest.epm.client.service;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.Charset.defaultCharset;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.Container;

import io.elastest.epm.client.DockerComposeProject;
import io.elastest.epm.client.DockerException;
import io.elastest.epm.client.dockercompose.DockerComposeContainer;
import io.elastest.epm.client.json.DockerComposeCreateProject;
import io.elastest.epm.client.json.DockerComposeProjectMessage;
import io.elastest.epm.client.json.DockerContainerInfo;

/**
 * Service implementation for Docker Compose.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.0.1
 */
@Service
@PropertySources({ @PropertySource(value = "classpath:epm-client.properties") })
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DockerComposeService {

    @Autowired
    EpmService epmService;

    @Autowired
    FilesService filesService;

    final Logger logger = getLogger(lookup().lookupClass());
    private static final Map<String, DockerComposeContainer> projects = new HashMap<>();

    public DockerService dockerService;

    public DockerComposeService(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @PreDestroy
    public void teardown() throws Exception {
        if (projects != null) {
            Map<String, DockerComposeContainer> projectsMapCopy = new HashMap<>();
            projectsMapCopy.putAll(projects);
            for (HashMap.Entry<String, DockerComposeContainer> project : projectsMapCopy
                    .entrySet()) {
                this.stopAndRemoveProject(project.getKey());
            }
        }
    }

    public DockerComposeProject createAndStartDockerComposeWithFile(
            String projectName, String dockerComposeFile) throws Exception {
        String dockerComposeYml = IOUtils.toString(
                this.getClass().getResourceAsStream("/" + dockerComposeFile),
                defaultCharset());

        return createAndStartDockerComposeByContent(projectName,
                dockerComposeYml);
    }

    public DockerComposeProject createAndStartDockerComposeByContent(
            String projectName, String dockerComposeYml) throws Exception {
        DockerComposeProject dockerComposeProject = new DockerComposeProject(
                projectName, dockerComposeYml, this);
        dockerComposeProject.start();
        dockerComposeProject.updateContainerInfo();

        return dockerComposeProject;
    }

    public List<DockerComposeContainer> listProjects() throws IOException {
        logger.debug("List Docker Compose projects");
        return new ArrayList(projects.values());
    }

    public String getYaml(String projectName) throws IOException {
        logger.debug("Get YAML of project {}", projectName);

        if (projects.containsKey(projectName)
                && projects.get(projectName).getComposeYmlList() != null
                && projects.get(projectName).getComposeYmlList().size() > 0) {

            return (String) projects.get(projectName).getComposeYmlList()
                    .get(0);
        }
        throw new DockerException(
                "Error on get yaml of project " + projectName);
    }

    // Create project from yml string
    public boolean createProject(DockerComposeCreateProject project,
            String targetPath, boolean override,
            boolean withBindedExposedPortsToRandom, boolean withRemoveVolumes)
            throws Exception {

        if (withBindedExposedPortsToRandom) {
            project.bindAllExposedPortsToRandom();
        }

        logger.debug("Creating Docker Compose with data: {}", project);
        File ymlFile = createFileFromProject(project, targetPath, override);
        List<String> images = new ArrayList<>();
        try {
            images = project.getImagesFromYML();
        } catch (Exception e) {
            logger.error("Error on process yml to get images");
        }

        DockerComposeContainer compose = new DockerComposeContainer<>(
                project.getName(), false, epmService, filesService, targetPath,
                ymlFile).setComposeYmlList(Arrays.asList(project.getYml()));

        if (project.getEnv() != null && !project.getEnv().isEmpty()) {
            compose.withEnv(project.getEnv());
        }

        compose.withRemoveVolumes(withRemoveVolumes);

        if (images != null && images.size() > 0) {
            compose.withImages(images);
        }

        projects.put(project.getName(), compose);
        logger.debug("Created Docker Compose with data: {}", project);

        return true;

    }

    public boolean createProjectWithEnv(String projectName,
            String dockerComposeYml, String targetPath, boolean override,
            Map<String, String> envs, boolean withBindedExposedPortsToRandom,
            boolean withRemoveVolumes) throws Exception {
        DockerComposeCreateProject createProject = new DockerComposeCreateProject(
                projectName, dockerComposeYml.replaceAll("'", "\""));
        if (envs != null) {
            createProject.getEnv().putAll(envs);
            createProject.setEnvVarsToYmlServices();
        }

        return createProject(createProject, targetPath, override,
                withBindedExposedPortsToRandom, withRemoveVolumes);
    }

    public boolean createProject(String projectName, String dockerComposeYml,
            String targetPath, boolean override,
            boolean withBindedExposedPortsToRandom, boolean withRemoveVolumes)
            throws Exception {

        return createProjectWithEnv(projectName, dockerComposeYml, targetPath,
                override, null, withBindedExposedPortsToRandom,
                withRemoveVolumes);
    }

    public boolean createProject(String projectName, String dockerComposeYml,
            String targetPath) throws Exception {
        return createProject(projectName, dockerComposeYml, targetPath, false,
                false, false);
    }

    public boolean startProject(String projectName, boolean withPull)
            throws IOException {
        DockerComposeProjectMessage projectMessage = new DockerComposeProjectMessage(
                projectName);
        logger.debug("Starting Docker Compose project with data: {}",
                projectMessage);

        if (!projects.containsKey(projectName)) {
            return false;
        }
        try {
            if (withPull) {
                this.pullImages(projects.get(projectName));
            }
            projects.get(projectName).start();
        } catch (InterruptedException | ServiceException e) {
            throw new DockerException(
                    "Error on starting project " + projectName, e);
        }

        return true;
    }

    public boolean stopProject(String projectName) throws IOException {
        DockerComposeProjectMessage projectMessage = new DockerComposeProjectMessage(
                projectName);
        logger.debug("Stopping Docker Compose project with data: {}",
                projectMessage);

        if (!projects.containsKey(projectName)) {
            return false;
        }
        projects.get(projectName).stop();

        return true;
    }

    public DockerContainerInfo getContainers(String projectName) {
        List<Container> containers;
        DockerContainerInfo dockerContainerInfo = new DockerContainerInfo();
        try {
            containers = dockerService.getContainersByPrefix(projectName);
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

    public void removeProjects(String... projects) throws IOException {// TODO
        for (String project : projects) {
            logger.trace("Deleting docker-compose project {}", project);
            this.stopProject(project);
            this.removeProjectTmpFiles(project);
        }
    }

    public void removeProjectTmpFiles(String projectName) {
        if (projects.containsKey(projectName)) {
            for (File currentFile : (List<File>) projects.get(projectName)
                    .getComposeFiles()) {
                currentFile.delete();
            }

            projects.remove(projectName);
        }
    }

    public boolean stopAndRemoveProject(String projectName) {
        boolean stopped;
        try {
            stopped = this.stopProject(projectName);
            this.removeProjectTmpFiles(projectName);
        } catch (IOException e) {
            logger.error("error: {}", e);
            return false;
        }
        return stopped;
    }

    public File createFileFromProject(DockerComposeCreateProject project,
            String targetPath, boolean override) throws IOException {
        String filePath = targetPath.endsWith("/") ? targetPath
                : targetPath + "/";
        filePath += project.getName() + ".yml";
        File file = new File(filePath);
        if (override) {
            file.delete();
        }

        file.getParentFile().mkdirs();
        FileUtils.writeStringToFile(file, project.getYml(),
                StandardCharsets.UTF_8);
        return ResourceUtils.getFile(filePath);
    }

    @Aspect
    @Component
    class DockerComposeAspect {
        final Logger log = getLogger(lookup().lookupClass());

        boolean isStarted = false;

        @Autowired
        DockerComposeService dockerComposeService;

        @Before("execution(* io.elastest.epm.client.service.DockerComposeService.*(..))")
        void before() throws Exception {
            if (!isStarted) {
                isStarted = true;
            }
        }

    }

    public void pullImages(DockerComposeContainer dockerComposeContainer) {
        this.pullImagesWithProgressHandler(dockerComposeContainer,
                dockerService.getEmptyProgressHandler());
    }

    public void pullImagesWithProgressHandler(
            DockerComposeContainer dockerComposeContainer,
            ProgressHandler progressHandler) {
        if (dockerComposeContainer.getImagesList() != null) {
            for (String image : (List<String>) dockerComposeContainer
                    .getImagesList()) {
                try {
                    dockerService.pullImageWithProgressHandler(image,
                            progressHandler);
                } catch (Exception e) {
                    logger.error("Error on pull {} image", image);
                }
            }
        }

    }

    public void pullImagesWithProgressHandler(String projectName,
            ProgressHandler progressHandler) throws Exception {
        if (!projects.containsKey(projectName)) {
            throw new Exception("Error on Pull images of project " + projectName
                    + ": Project does not exists");
        }

        this.pullImagesWithProgressHandler(projects.get(projectName),
                progressHandler);
    }

    public void pullImages(String projectName) throws Exception {
        this.pullImagesWithProgressHandler(projectName,
                dockerService.getEmptyProgressHandler());
    }

    public List<String> getProjectImages(String projectName) throws Exception {
        if (!projects.containsKey(projectName)) {
            throw new Exception("Error on get images of project " + projectName
                    + ": Project does not exists");
        }

        return projects.get(projectName).getImagesList();
    }
}
