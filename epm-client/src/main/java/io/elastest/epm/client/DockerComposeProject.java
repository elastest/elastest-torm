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
package io.elastest.epm.client;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.slf4j.Logger;

import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.service.DockerComposeService;

/**
 * Project on docker-compose-ui.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */
public class DockerComposeProject {

    final Logger log = getLogger(lookup().lookupClass());

    private String projectName;
    private String dockerComposeYml;
    private DockerComposeService dockerComposeService;
    private boolean isStarted = false;
    private DockerContainerInfo containersInfo;

    public DockerComposeProject(String projectName, String dockerComposeYml,
            DockerComposeService dockerComposeService) {
        this.projectName = projectName;
        this.dockerComposeYml = dockerComposeYml;
        this.dockerComposeService = dockerComposeService;
    }

    public DockerComposeProject(String projectName,
            DockerComposeService dockerComposeService) {
        this.projectName = projectName;
        this.dockerComposeService = dockerComposeService;
        this.isStarted = true;
    }

    public synchronized void start() throws IOException {
        assertNotProjectStarted();

        log.debug("Starting Docker Compose project {}", projectName);
        dockerComposeService.createProject(projectName, dockerComposeYml);
        dockerComposeService.startProject(projectName);
        isStarted = true;
        log.debug("Docker Compose project {} started correctly", projectName);
    }

    public synchronized void stop() throws IOException {
        assertProjectStarted();

        log.debug("Stopping Docker Compose project {}", projectName);
        dockerComposeService.stopProject(projectName);
        isStarted = false;
        log.debug("Docker Compose project {} stopped correctly", projectName);
    }

    public synchronized void updateContainerInfo() throws IOException {
        log.debug("Updating container info of project {}", projectName);
        containersInfo = dockerComposeService.getContainers(projectName);
    }

    public synchronized void updateDockerComposeYml() throws IOException {
        log.debug("Updating docker compose yaml of project {}", projectName);
        dockerComposeYml = dockerComposeService.getYaml(projectName);
    }

    private void assertProjectStarted() {
        assert isStarted : "Docker Compose project " + projectName
                + " already is NOT started";
    }

    private void assertNotProjectStarted() {
        assert !isStarted : "Docker Compose project " + projectName
                + " already is ALREADY started";
    }

    public boolean isStarted() {
        return isStarted;
    }

    public String getDockerComposeYml() {
        return dockerComposeYml;
    }

    public DockerContainerInfo getContainersInfo() {
        return containersInfo;
    }

}
