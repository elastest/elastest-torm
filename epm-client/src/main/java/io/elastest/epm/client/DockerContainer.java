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

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.List;
import java.util.Optional;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Volume;

/**
 * Docker container builder.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.0.1
 */
public class DockerContainer {
    private final String imageId;
    private final String containerName;
    private final Optional<List<PortBinding>> portBindings;
    private final Optional<List<Volume>> volumes;
    private final Optional<List<Bind>> binds;
    private final Optional<List<String>> envs;
    private final Optional<String> network;

    private DockerContainer(DockerBuilder builder) {
        this.imageId = builder.imageId;
        this.containerName = builder.containerName;
        this.portBindings = builder.portBindings != null
                ? of(builder.portBindings)
                : empty();
        this.volumes = builder.volumes != null ? of(builder.volumes) : empty();
        this.binds = builder.binds != null ? of(builder.binds) : empty();
        this.envs = builder.envs != null ? of(builder.envs) : empty();
        this.network = builder.network != null ? of(builder.network) : empty();
    }

    public static DockerBuilder dockerBuilder(String imageId, String containerName) {
        return new DockerBuilder(imageId, containerName);
    }

    public String getImageId() {
        return imageId;
    }

    public String getContainerName() {
        return containerName;
    }

    public Optional<List<PortBinding>> getPortBindings() {
        return portBindings;
    }

    public Optional<List<Volume>> getVolumes() {
        return volumes;
    }

    public Optional<List<Bind>> getBinds() {
        return binds;
    }

    public Optional<List<String>> getEnvs() {
        return envs;
    }

    public Optional<String> getNetwork() {
        return network;
    }

    public static class DockerBuilder {
        private String imageId;
        private String containerName;
        private List<PortBinding> portBindings;
        private List<Volume> volumes;
        private List<Bind> binds;
        private List<String> envs;
        private String network;

        public DockerBuilder(String imageId, String containerName) {
            this.imageId = imageId;
            this.containerName = containerName;
        }

        public DockerBuilder portBindings(List<PortBinding> portBindings) {
            this.portBindings = portBindings;
            return this;
        }

        public DockerBuilder volumes(List<Volume> volumes) {
            this.volumes = volumes;
            return this;
        }

        public DockerBuilder binds(List<Bind> binds) {
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

        public DockerContainer build() {
            return new DockerContainer(this);
        }
    }

}