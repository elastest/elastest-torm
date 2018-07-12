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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
public class DockerComposeService {

    final Logger logger = getLogger(lookup().lookupClass());
    private static final Map<String, DockerComposeContainer> projects = new HashMap<>();

    private DockerService dockerService;
    private JsonService jsonService;

    public DockerComposeService(DockerService dockerService,
            JsonService jsonService) {
        this.dockerService = dockerService;
        this.jsonService = jsonService;
    }

    @SuppressWarnings("rawtypes")
    @PreDestroy
    public void teardown() throws Exception {
        // todo stop projects
        if (projects != null) {
            for (HashMap.Entry<String, DockerComposeContainer> project : projects
                    .entrySet()) {
                this.stopAndRemoveProject(project.getKey());
            }
        }
    }

    public DockerComposeProject createAndStartDockerComposeWithFile(
            String projectName, String dockerComposeFile) throws IOException {
        String dockerComposeYml = IOUtils.toString(
                this.getClass().getResourceAsStream("/" + dockerComposeFile),
                defaultCharset());

        return createAndStartDockerComposeByContent(projectName,
                dockerComposeYml);
    }

    public DockerComposeProject createAndStartDockerComposeByContent(
            String projectName, String dockerComposeYml) throws IOException {
        DockerComposeProject dockerComposeProject = new DockerComposeProject(
                projectName, dockerComposeYml, this);
        dockerComposeProject.start();
        dockerComposeProject.updateContainerInfo();

        return dockerComposeProject;
    }

    public List<DockerComposeProject> listProjects() throws IOException {// TODO
        logger.debug("List Docker Compose projects");
        List<DockerComposeProject> projects = new ArrayList<>();

        // Response<DockerComposeList> response =
        // dockerComposeApi.listProjects()
        // .execute();
        // logger.debug("List projects response code {}", response.code());
        //
        // if (response.isSuccessful()) {
        // DockerComposeList body = response.body();
        // logger.debug("Success: {}", body);
        // Set<String> keySet = body.getProjects().keySet();
        //
        // for (String key : keySet) {
        // DockerComposeProject project = new DockerComposeProject(key,
        // this);
        // project.updateDockerComposeYml();
        // project.updateContainerInfo();
        // projects.add(project);
        // }
        // }
        return projects;
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
            String targetPath) throws IOException {

        logger.debug("Creating Docker Compose with data: {}", project);
        File ymlFile = createFileFromProject(project, targetPath);

        List<String> images = this.getImagesFromYML(project.getYml());

        DockerComposeContainer compose = new DockerComposeContainer<>(
                project.getName(), false, ymlFile).withImages(images)
                        .setComposeYmlList(Arrays.asList(project.getYml()));
        projects.put(project.getName(), compose);
        logger.debug("Created Docker Compose with data: {}", project);

        return true;

    }

    public boolean createProject(String projectName, String dockerComposeYml,
            String targetPath) throws IOException {
        DockerComposeCreateProject createProject = new DockerComposeCreateProject(
                projectName, dockerComposeYml.replaceAll("'", "\""));

        return createProject(createProject, targetPath);
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
        } catch (InterruptedException e) {
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

    @SuppressWarnings("unchecked")
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
        } catch (IOException e) {
            return false;
        }
        this.removeProjectTmpFiles(projectName);
        return stopped;
    }

    public File createFileFromProject(DockerComposeCreateProject project,
            String targetPath) throws IOException {
        String filePath = targetPath.endsWith("/") ? targetPath
                : targetPath + "/";
        filePath += project.getName() + ".yml";
        File file = new File(filePath);
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

    private List<String> getImagesFromYML(String yml) {
        List<String> images = new ArrayList<>();
        YAMLFactory yf = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yf);
        Object object;
        try {
            object = mapper.readValue(yml, Object.class);

            Map<String, HashMap<String, HashMap>> dockerComposeMap = (HashMap) object;
            Map<String, HashMap> servicesMap = dockerComposeMap.get("services");
            for (HashMap.Entry<String, HashMap> service : servicesMap
                    .entrySet()) {

                HashMap<String, String> serviceContent = service.getValue();

                String imageKey = "image";
                // If service has image, pull
                if (serviceContent.containsKey(imageKey)) {
                    String image = serviceContent.get(imageKey);
                    images.add(image);
                }

            }
        } catch (Exception e) {
            logger.error("Error on process yml to get images");
        }
        return images;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void pullImages(DockerComposeContainer dockerComposeContainer) {
        if (dockerComposeContainer.getImagesList() != null) {
            for (String image : (List<String>) dockerComposeContainer
                    .getImagesList()) {
                try {
                    dockerService.pullImage(image);
                } catch (Exception e) {
                    logger.error("Error on pull {} image", image);
                }
            }
        }

    }

}
