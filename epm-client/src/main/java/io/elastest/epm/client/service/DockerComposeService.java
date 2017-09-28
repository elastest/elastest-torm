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

import static com.github.dockerjava.api.model.ExposedPort.tcp;
import static com.github.dockerjava.api.model.Ports.Binding.bindPort;
import static io.elastest.epm.client.DockerContainer.dockerBuilder;
import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.api.model.Volume;

import io.elastest.epm.client.DockerComposeApi;
import io.elastest.epm.client.DockerComposeProject;
import io.elastest.epm.client.DockerException;
import io.elastest.epm.client.DockerContainer.DockerBuilder;
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
public class DockerComposeService {

    final Logger log = getLogger(lookup().lookupClass());

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

    public void setup() throws IOException, InterruptedException {
        // 1. Start docker-compose-ui container
        dockerComposeUiContainerName = dockerService
                .generateContainerName(dockerComposeUiPrefix);

        int dockerComposeBindPort = dockerService.findRandomOpenPort();
        Binding bindNoVncPort = bindPort(dockerComposeBindPort);
        ExposedPort exposedNoVncPort = tcp(dockerComposeUiPort);

        String dockerComposeServiceUrl = "http://"
                + dockerService.getDockerServerIp() + ":"
                + dockerComposeBindPort;

        log.debug("Starting docker-compose-ui container: {}",
                dockerComposeUiContainerName);

        List<PortBinding> portBindings = asList(
                new PortBinding(bindNoVncPort, exposedNoVncPort));
        Volume volume = new Volume(dockerDefaultSocket);
        List<Volume> volumes = asList(volume);
        List<Bind> binds = asList(new Bind(dockerDefaultSocket, volume));

        DockerBuilder dockerBuilder = dockerBuilder(dockerComposeUiImageId,
                dockerComposeUiContainerName).portBindings(portBindings)
                        .volumes(volumes).binds(binds);
        dockerService.startAndWaitContainer(dockerBuilder.build());

        // 2. Create Retrofit object to call docker-compose-ui REST API
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(dockerComposeTimeout, SECONDS)
                .connectTimeout(dockerComposeTimeout, SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(dockerComposeServiceUrl).build();
        dockerComposeApi = retrofit.create(DockerComposeApi.class);

        log.debug("docker-compose-ui up and running on URL: {}",
                dockerComposeServiceUrl);

        // 3.Delete default example projects
        dockerService.waitForHostIsReachable(dockerComposeServiceUrl);
        String[] defaultProjects = { "env-demo", "hello-node", "node-redis",
                "volumes-demo", "volumes-relative-paths" };
        removeProjects(defaultProjects);
    }

    @PreDestroy
    public void teardown() {
        if (dockerComposeUiContainerName != null) {
            log.debug("Stopping docker-compose-ui container: {}",
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

    public boolean createProject(String projectName, String dockerComposeYml)
            throws IOException {
        DockerComposeCreateProject createProject = new DockerComposeCreateProject(
                projectName, dockerComposeYml.replaceAll("'", "\""));
        log.debug("Creating Docker Compose with data: {}", createProject);

        String json = jsonService.objectToJson(createProject);
        RequestBody data = create(parse(APPLICATION_JSON), json);
        Response<ResponseBody> response = dockerComposeApi.createProject(data)
                .execute();

        log.trace("Create project response code {}", response.code());
        if (!response.isSuccessful()) {
            throw new DockerException(response.errorBody().string());
        }

        return true;
    }

    public boolean startProject(String projectName) throws IOException {
        DockerComposeProjectMessage projectMessage = new DockerComposeProjectMessage(
                projectName);
        log.debug("Starting Docker Compose project with data: {}",
                projectMessage);

        RequestBody data = create(parse(APPLICATION_JSON),
                jsonService.objectToJson(projectMessage));
        Response<ResponseBody> response = dockerComposeApi.dockerComposeUp(data)
                .execute();

        log.trace("Start project response code {}", response.code());
        if (!response.isSuccessful()) {
            throw new DockerException(response.errorBody().string());
        }
        return true;
    }

    public boolean stopProject(String projectName) throws IOException {
        DockerComposeProjectMessage projectMessage = new DockerComposeProjectMessage(
                projectName);
        log.debug("Stopping Docker Compose project with data: {}",
                projectMessage);

        RequestBody data = create(parse(APPLICATION_JSON),
                jsonService.objectToJson(projectMessage));
        Response<ResponseBody> response = dockerComposeApi
                .dockerComposeDown(data).execute();

        log.trace("Stop project response code {}", response.code());
        if (!response.isSuccessful()) {
            throw new DockerException(response.errorBody().string());
        }
        return true;
    }

    public List<DockerComposeProject> listProjects() throws IOException {
        log.debug("List Docker Compose projects");
        List<DockerComposeProject> projects = new ArrayList<>();

        Response<DockerComposeList> response = dockerComposeApi.listProjects()
                .execute();
        log.debug("List projects response code {}", response.code());

        if (response.isSuccessful()) {
            DockerComposeList body = response.body();
            log.debug("Success: {}", body);
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
        log.debug("Get YAML of project {}", projectName);

        Response<DockerComposeConfig> response = dockerComposeApi
                .getDockerComposeYml(projectName).execute();
        log.debug("Get YAML response code {}", response.code());

        if (response.isSuccessful()) {
            DockerComposeConfig body = response.body();
            log.debug("YAML body content: {}", body);
            return body.getYml();
        }
        throw new DockerException(response.errorBody().string());
    }

    public void removeProjects(String... projects) throws IOException {
        for (String project : projects) {
            log.trace("Deleting docker-compose project {}", project);
            dockerComposeApi.removeProject(project).execute();
        }
    }

    @Aspect
    @Component
    class DockerComposeAspect {
        final Logger log = getLogger(lookup().lookupClass());

        boolean isStarted = false;

        @Autowired
        DockerComposeService dockerComposeService;

        @Before("execution(* io.elastest.epm.client.service.DockerComposeService.*(..))")
        void before() throws IOException, InterruptedException {
            if (!isStarted) {
                isStarted = true;
                dockerComposeService.setup();
            }
        }

    }

}
