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
package io.elastest.epm.client.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Container.PortMapping;

import io.elastest.epm.client.service.K8sService;
import io.fabric8.kubernetes.api.model.Pod;

/**
 * Utility class for deserialize container info from docker-compose-ui.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */
public class DockerContainerInfo {

    List<DockerContainer> containers;

    public DockerContainerInfo() {
        containers = new ArrayList<>();
    }

    public List<DockerContainer> getContainers() {
        return containers;
    }

    @Override
    public String toString() {
        return "DockerContainerInfo [getContainers()=" + getContainers() + "]";
    }

    public static class DockerContainer {

        // TODO Problem with @JsonProperty and underscore
        boolean is_running;

        // TODO Problem with @JsonProperty and underscore
        String name_without_project;

        String command;
        Labels labels;
        String name;
        Map<String, List<PortInfo>> ports;
        String state;
        List<Map<String, Object>> volumes;

        public String getCommand() {
            return command;
        }

        public boolean isRunning() {
            return is_running;
        }

        public Labels getLabels() {
            return labels;
        }

        public String getName() {
            return name;
        }

        public String getNameWithoutProject() {
            return name_without_project;
        }

        public Map<String, List<PortInfo>> getPorts() {
            return ports;
        }

        public String getState() {
            return state;
        }

        public List<Map<String, Object>> getVolumes() {
            return volumes;
        }

        @Override
        public String toString() {
            return "DockerContainer [getCommand()=" + getCommand()
                    + ", isRunning()=" + isRunning() + ", getLabels()="
                    + getLabels() + ", getName()=" + getName()
                    + ", getNameWithoutProject()=" + getNameWithoutProject()
                    + ", getPorts()=" + getPorts() + ", getState()="
                    + getState() + ", getVolumes()=" + getVolumes() + "]";
        }

        public void initFromContainer(Container container) {
            this.command = container.command();
            this.is_running = container.status().startsWith("Up");
            this.labels = new Labels(container.labels());
            if (container.names().size() > 0) {
                this.name = container.names().get(0).replaceFirst("/", "");
            }
            Map<String, List<PortInfo>> portsMap = new HashMap<>();
            for (PortMapping port : container.ports()) {
                List<PortInfo> ports = new ArrayList<>();
                ports.add(new PortInfo(port));

                portsMap.put(port.privatePort() + "/tcp", ports);
            }
            this.ports = portsMap;

            // this.volumes = container.getVolumes TODO docker java has not
            // volumes
        }
        
        public void initFromPod(Pod pod) {
            this.is_running = pod.getStatus().getPhase()
                    .equals(K8sService.PodsStatusEnum.RUNNING.toString());
            this.labels = new Labels(pod.getMetadata().getLabels());
            this.name = pod.getMetadata().getName();
        }

    }

    public static class Labels {
        @JsonProperty("com.docker.compose.config-hash")
        String configHash;

        @JsonProperty("com.docker.compose.container-number")
        String containerNumber;

        @JsonProperty("com.docker.compose.oneoff")
        String oneoff;

        @JsonProperty("com.docker.compose.project")
        String project;

        @JsonProperty("com.docker.compose.service")
        String service;

        @JsonProperty("com.docker.compose.version")
        String version;

        public Labels() {
        }

        public Labels(Map<String, String> labelsMap) {
            this.initFromMap(labelsMap);
        }

        public String getConfigHash() {
            return configHash;
        }

        public String getContainerNumber() {
            return containerNumber;
        }

        public String getOneoff() {
            return oneoff;
        }

        public String getProject() {
            return project;
        }

        public String getService() {
            return service;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return "Labels [getConfigHash()=" + getConfigHash()
                    + ", getContainerNumber()=" + getContainerNumber()
                    + ", getOneoff()=" + getOneoff() + ", getProject()="
                    + getProject() + ", getService()=" + getService()
                    + ", getVersion()=" + getVersion() + "]";
        }

        public void initFromMap(Map<String, String> labelsMap) {
            if (!labelsMap.isEmpty()) {
                if (labelsMap.containsKey("com.docker.compose.config-hash")) {
                    this.configHash = labelsMap
                            .get("com.docker.compose.config-hash");
                }

                if (labelsMap
                        .containsKey("com.docker.compose.container-number")) {
                    this.containerNumber = labelsMap
                            .get("com.docker.compose.container-number");
                }

                if (labelsMap.containsKey("com.docker.compose.oneoff")) {
                    this.oneoff = labelsMap.get("com.docker.compose.oneoff");
                }

                if (labelsMap.containsKey("com.docker.compose.project")) {
                    this.project = labelsMap.get("com.docker.compose.project");
                }

                if (labelsMap.containsKey("com.docker.compose.service")) {
                    this.service = labelsMap.get("com.docker.compose.service");
                }

                if (labelsMap.containsKey("com.docker.compose.version")) {
                    this.version = labelsMap.get("com.docker.compose.version");
                }
            }
        }

    }

    public static class PortInfo {

        // TODO Problem with @JsonProperty and upper case
        String HostIp;

        // TODO Problem with @JsonProperty and upper case
        String HostPort;

        public PortInfo() {
        }

        public PortInfo(PortMapping port) {
            if (port != null) {
                this.HostIp = port.ip();
                this.HostPort = port.publicPort() != null
                        ? port.publicPort().toString()
                        : null;
            }
        }

        public String getHostIp() {
            return HostIp;
        }

        public String getHostPort() {
            return HostPort;
        }

        @Override
        public String toString() {
            return "PortInfo [getHostIp()=" + getHostIp() + ", getHostPort()="
                    + getHostPort() + "]";
        }
    }

}
