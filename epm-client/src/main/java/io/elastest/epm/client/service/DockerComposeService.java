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
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.HostConfig.Bind.Builder;
import com.spotify.docker.client.messages.PortBinding;

import io.elastest.epm.client.DockerComposeApi;
import io.elastest.epm.client.DockerComposeProject;
import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.DockerException;
import io.elastest.epm.client.dockercompose.DockerComposeContainer;
import io.elastest.epm.client.json.DockerComposeConfig;
import io.elastest.epm.client.json.DockerComposeCreateProject;
import io.elastest.epm.client.json.DockerComposeList;
import io.elastest.epm.client.json.DockerComposeProjectMessage;
import io.elastest.epm.client.json.DockerContainerInfo;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

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

    @Value("${docker.compose.ui.exposedport}")
    private int dockerComposeUiPort;

    @Value("${docker.compose.ui.image}")
    private String dockerComposeUiImageId;

    @Value("${docker.compose.ui.prefix}")
    private String dockerComposeUiPrefix;

    @Value("${docker.compose.ui.timeout}")
    private int dockerComposeTimeout;

    @Value("${docker.default.socket}")
    private String dockerDefaultSocket;

    private String dockerComposeUiContainerName;
    private DockerComposeApi dockerComposeApi;

    private DockerService dockerService;
    private JsonService jsonService;

    public DockerComposeService(DockerService dockerService,
            JsonService jsonService) {
        this.dockerService = dockerService;
        this.jsonService = jsonService;
    }

    public void setup() throws Exception {
        // 1. Start docker-compose-ui container

        logger.debug("Starting docker-compose-ui container: {}",
                dockerComposeUiContainerName);

        dockerComposeUiContainerName = dockerService
                .generateContainerName(dockerComposeUiPrefix);

        int dockerComposeBindPort = dockerService.findRandomOpenPort();
        String dockerComposeServiceUrl = "http://"
                + dockerService.getDockerServerIp() + ":"
                + dockerComposeBindPort;

        List<String> envVariables = new ArrayList<>();

        DockerBuilder dockerBuilder = new DockerBuilder(dockerComposeUiImageId)
                .containerName(dockerComposeUiContainerName);
        dockerBuilder.envs(envVariables);

        dockerBuilder.exposedPorts(
                Arrays.asList(String.valueOf(dockerComposeUiPort)));

        // portBindings
        Map<String, List<PortBinding>> portBindings = new HashMap<>();
        portBindings.put(String.valueOf(dockerComposeUiPort),
                Arrays.asList(PortBinding.of("0.0.0.0",
                        Integer.toString(dockerComposeBindPort))));
        dockerBuilder.portBindings(portBindings);

        // Volumes (docker.sock)
        List<Bind> volumes = new ArrayList<>();
        Builder dockerSockVolumeBuilder = Bind.builder();
        dockerSockVolumeBuilder.from(dockerDefaultSocket);
        dockerSockVolumeBuilder.to(dockerDefaultSocket);
        volumes.add(dockerSockVolumeBuilder.build());
        dockerBuilder.volumeBindList(volumes);

        try {
            dockerService.pullImage(dockerComposeUiImageId);
        } catch (Exception e) {
            if (!dockerService.existsImage(dockerComposeUiImageId)) {
                throw new Exception(
                        "Error on pulling " + dockerComposeUiImageId + " image",
                        e);
            }
        }
        dockerService.createAndStartContainer(dockerBuilder.build());

        // 2. Create Retrofit object to call docker-compose-ui REST API
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(dockerComposeTimeout, SECONDS)
                .connectTimeout(dockerComposeTimeout, SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(dockerComposeServiceUrl).build();
        dockerComposeApi = retrofit.create(DockerComposeApi.class);

        logger.debug("docker-compose-ui up and running on URL: {}",
                dockerComposeServiceUrl);

        // 3.Delete default example projects
        dockerService.waitForHostIsReachable(dockerComposeServiceUrl);
        String[] defaultProjects = { "env-demo", "hello-node", "node-redis",
                "volumes-demo", "volumes-relative-paths" };
        removeProjects(defaultProjects);
    }

    @PreDestroy
    public void teardown() throws Exception {
        if (dockerComposeUiContainerName != null) {
            logger.debug("Stopping docker-compose-ui container: {}",
                    dockerComposeUiContainerName);
            dockerService.stopAndRemoveContainer(dockerComposeUiContainerName);
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

    public boolean createProject(DockerComposeCreateProject project)
            throws IOException {

        logger.debug("Creating Docker Compose with data: {}", project);

        String json = jsonService.objectToJson(project);
        RequestBody data = create(parse(APPLICATION_JSON), json);
        Response<ResponseBody> response = dockerComposeApi.createProject(data)
                .execute();

        logger.trace("Create project response code {}", response.code());
        if (!response.isSuccessful()) {
            throw new DockerException(response.errorBody().string());
        }

        return true;
    }

    public boolean createProject(String projectName, String dockerComposeYml)
            throws IOException {
        DockerComposeCreateProject createProject = new DockerComposeCreateProject(
                projectName, dockerComposeYml.replaceAll("'", "\""));

        return createProject(createProject);
    }

    public boolean startProject(String projectName) throws IOException {
        DockerComposeProjectMessage projectMessage = new DockerComposeProjectMessage(
                projectName);
        logger.debug("Starting Docker Compose project with data: {}",
                projectMessage);

        RequestBody data = create(parse(APPLICATION_JSON),
                jsonService.objectToJson(projectMessage));
        Response<ResponseBody> response = dockerComposeApi.dockerComposeUp(data)
                .execute();

        logger.trace("Start project response code {}", response.code());
        if (!response.isSuccessful()) {

            logger.error("Start project response code {}", response.code());

            throw new DockerException(response.errorBody().string());
        }
        return true;
    }

    public boolean stopProject(String projectName) throws IOException {
        DockerComposeProjectMessage projectMessage = new DockerComposeProjectMessage(
                projectName);
        logger.debug("Stopping Docker Compose project with data: {}",
                projectMessage);

        RequestBody data = create(parse(APPLICATION_JSON),
                jsonService.objectToJson(projectMessage));
        Response<ResponseBody> response = dockerComposeApi
                .dockerComposeDown(data).execute();

        logger.trace("Stop project response code {}", response.code());
        if (!response.isSuccessful()) {
            throw new DockerException(response.errorBody().string());
        }
        return true;
    }

    public List<DockerComposeProject> listProjects() throws IOException {
        logger.debug("List Docker Compose projects");
        List<DockerComposeProject> projects = new ArrayList<>();

        Response<DockerComposeList> response = dockerComposeApi.listProjects()
                .execute();
        logger.debug("List projects response code {}", response.code());

        if (response.isSuccessful()) {
            DockerComposeList body = response.body();
            logger.debug("Success: {}", body);
            Set<String> keySet = body.getProjects().keySet();

            for (String key : keySet) {
                DockerComposeProject project = new DockerComposeProject(key,
                        this);
                project.updateDockerComposeYml();
                project.updateContainerInfo();
                projects.add(project);
            }
        }
        return projects;
    }

    public DockerContainerInfo getContainers(String projectName)
            throws IOException {
        Response<DockerContainerInfo> response = dockerComposeApi
                .getContainers(projectName).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        throw new DockerException(response.errorBody().string());
    }

    public String getYaml(String projectName) throws IOException {
        logger.debug("Get YAML of project {}", projectName);

        Response<DockerComposeConfig> response = dockerComposeApi
                .getDockerComposeYml(projectName).execute();
        logger.debug("Get YAML response code {}", response.code());

        if (response.isSuccessful()) {
            DockerComposeConfig body = response.body();
            logger.debug("YAML body content: {}", body);
            return body.getYml();
        }
        throw new DockerException(response.errorBody().string());
    }

    public void removeProjects(String... projects) throws IOException {
        for (String project : projects) {
            logger.trace("Deleting docker-compose project {}", project);
            dockerComposeApi.removeProject(project).execute();
        }
    }

    public boolean createProject2(DockerComposeCreateProject project,
            String targetPath) throws IOException {

        logger.debug("Creating Docker Compose with data: {}", project);
        File ymlFile = createFileFromProject(project, targetPath);

        List<String> images = this.getImagesFromYML(project.getYml());

        DockerComposeContainer compose = new DockerComposeContainer<>(
                project.getName(), false, ymlFile).withImages(images);
        projects.put(project.getName(), compose);
        logger.debug("Created Docker Compose with data: {}", project);

        return true;

    }

    public boolean createProject2(String projectName, String dockerComposeYml,
            String targetPath) throws IOException {
        DockerComposeCreateProject createProject = new DockerComposeCreateProject(
                projectName, dockerComposeYml.replaceAll("'", "\""));

        return createProject2(createProject, targetPath);
    }

    public boolean startProject2(String projectName, boolean withPull)
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

    public boolean stopProject2(String projectName) throws IOException {
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

    public DockerContainerInfo getContainers2(String projectName) {
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

    public File createFileFromProject(DockerComposeCreateProject project,
            String targetPath) throws IOException {
        String filePath = targetPath + "/" + project.getName() + ".yml";
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
                dockerComposeService.setup();
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
