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

import java.util.List;
import java.util.Map;

/**
 * Utility class for deserialize project list from docker-compose-ui.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */
public class DockerComposeList {

    List<Object> active;
    Map<String, String> projects;

    public Map<String, String> getProjects() {
        return projects;
    }

    public List<Object> getActive() {
        return active;
    }

    @Override
    public String toString() {
        return "DockerComposeList [getProjects()=" + getProjects()
                + ", getActive()=" + getActive() + "]";
    }

}
