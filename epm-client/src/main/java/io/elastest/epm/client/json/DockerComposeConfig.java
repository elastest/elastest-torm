/*
dlis     * (C) Copyright 2017-2019 ElasTest (http://elastest.io/)
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

import java.util.List;

/**
 * Utility class for deserialize project configuration from docker-compose-ui.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */
public class DockerComposeConfig {

    List<Object> config;
    Object env;
    String yml;
    String path;

    public List<Object> getConfig() {
        return config;
    }

    public Object getEnv() {
        return env;
    }

    public String getYml() {
        return yml;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "DockerComposeConfig [getConfig()=" + getConfig() + ", getEnv()="
                + getEnv() + ", getYml()=" + getYml() + ", getPath()="
                + getPath() + "]";
    }

}
