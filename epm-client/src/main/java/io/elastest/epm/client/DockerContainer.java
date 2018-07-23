package io.elastest.epm.client;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.PortBinding;

/**
 * Docker Container.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.1.2
 */
public class DockerContainer {
    private String imageId;
    private Optional<String> containerName;
    private Optional<Map<String, List<PortBinding>>> portBindings;
    private Optional<List<String>> exposedPorts;
    private Optional<List<String>> binds;
    private Optional<List<String>> envs;
    private Optional<String> network;
    private Optional<List<String>> cmd;
    private Optional<List<String>> entryPoint;
    private boolean privileged;
    private Optional<LogConfig> logConfig;
    private Optional<List<Bind>> volumeBindList;

    private String containerId;
    private String containerUrl;

    private DockerContainer(DockerBuilder builder) {
        this.imageId = builder.imageId;
        this.containerName = builder.containerName != null
                ? of(builder.containerName)
                : empty();
        this.portBindings = builder.portBindings != null
                ? of(builder.portBindings)
                : empty();
        this.exposedPorts = builder.exposedPorts != null
                ? of(builder.exposedPorts)
                : empty();

        this.binds = builder.binds != null ? of(builder.binds) : empty();
        this.envs = builder.envs != null ? of(builder.envs) : empty();
        this.network = builder.network != null ? of(builder.network) : empty();
        this.cmd = builder.cmd != null ? of(builder.cmd) : empty();
        this.entryPoint = builder.entryPoint != null ? of(builder.entryPoint)
                : empty();
        this.privileged = builder.privileged;
        this.logConfig = builder.logConfig != null ? of(builder.logConfig)
                : empty();
        this.volumeBindList = builder.volumeBindList != null
                ? of(builder.volumeBindList)
                : empty();
    }

    public static DockerBuilder dockerBuilder(String imageId) {
        return new DockerBuilder(imageId);
    }

    public String getImageId() {
        return imageId;
    }

    public Optional<String> getContainerName() {
        return containerName;
    }

    public Optional<Map<String, List<PortBinding>>> getPortBindings() {
        return portBindings;
    }

    public Optional<List<String>> getExposedPorts() {
        return exposedPorts;
    }

    public Optional<List<String>> getBinds() {
        return binds;
    }

    public Optional<List<String>> getEnvs() {
        return envs;
    }

    public Optional<String> getNetwork() {
        return network;
    }

    public Optional<List<String>> getCmd() {
        return cmd;
    }

    public Optional<List<String>> getEntryPoint() {
        return entryPoint;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getContainerUrl() {
        return containerUrl;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setContainerUrl(String containerUrl) {
        this.containerUrl = containerUrl;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public Optional<LogConfig> getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(Optional<LogConfig> logConfig) {
        this.logConfig = logConfig;
    }

    public Optional<List<Bind>> getVolumeBindList() {
        return volumeBindList;
    }

    public static class DockerBuilder {
        private String imageId;
        private String containerName;
        private Map<String, List<PortBinding>> portBindings;
        private List<String> exposedPorts;
        private List<String> binds;
        private List<String> envs;
        private List<String> cmd;
        private String network;
        private List<String> entryPoint;
        private boolean privileged = false;
        private LogConfig logConfig;
        private List<Bind> volumeBindList;

        public DockerBuilder(String imageId) {
            this.imageId = imageId;
        }

        public DockerBuilder containerName(String containerName) {
            this.containerName = containerName;
            return this;
        }

        public DockerBuilder portBindings(
                Map<String, List<PortBinding>> portBindings) {
            this.portBindings = portBindings;
            return this;
        }

        public DockerBuilder exposedPorts(List<String> exposedPorts) {
            this.exposedPorts = exposedPorts;
            return this;
        }

        public DockerBuilder binds(List<String> binds) {
            this.binds = binds;
            return this;
        }

        public DockerBuilder envs(List<String> envs) {
            this.envs = envs;
            return this;
        }

        public DockerBuilder network(String network) {
            this.network = network;
            return this;
        }

        public DockerBuilder cmd(List<String> cmd) {
            this.cmd = cmd;
            return this;
        }

        public DockerBuilder entryPoint(List<String> entryPoint) {
            this.entryPoint = entryPoint;
            return this;
        }

        public DockerBuilder privileged() {
            this.privileged = true;
            return this;
        }

        public DockerBuilder logConfig(LogConfig logConfig) {
            this.logConfig = logConfig;
            return this;
        }

        public DockerBuilder volumeBindList(List<Bind> volumeBindList) {
            this.volumeBindList = volumeBindList;
            return this;
        }

        public DockerContainer build() {
            return new DockerContainer(this);
        }
    }

}