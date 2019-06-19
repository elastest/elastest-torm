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

import java.io.IOException;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Utility class for serialize JSON messages (create project).
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DockerComposeCreateProject extends DockerProject{

    public DockerComposeCreateProject(String name, String yml) {
        super(name, yml);
        }

    public DockerComposeCreateProject(String name, String yml,
            List<String> envList) {
        super(name, yml, envList);
    }

    public List<String> envsAsList() {
        List<String> envList = new ArrayList<>();
        if (this.env != null && !this.env.isEmpty()) {
            for (HashMap.Entry<String, String> currentEnv : this.env
                    .entrySet()) {
                String envString = currentEnv.getKey() + "="
                        + currentEnv.getValue();
                envList.add(envString);
            }
        }

        return envList;
    }

    public Map<String, String> mapFromStringList(List<String> list,
            String splitCharacter) {
        Map<String, String> map = null;
        if (list != null
                && (splitCharacter != null && !splitCharacter.isEmpty())) {
            map = new HashMap<>();
            for (String entry : list) {
                String[] entryArray = entry.split(splitCharacter);
                map.put(entryArray[0],
                        entryArray.length > 1 ? entryArray[1] : "");
            }
        }
        return map;
    }

    public List<String> envsAsList(Map<String, String> envsMap) {
        List<String> envList = new ArrayList<>();
        if (envsMap != null && !envsMap.isEmpty()) {
            for (HashMap.Entry<String, String> currentEnv : envsMap
                    .entrySet()) {
                String envString = currentEnv.getKey() + "="
                        + currentEnv.getValue();
                envList.add(envString);
            }
        }

        return envList;
    }

    /* *** Methods of YML processing *** */

    private HashMap<String, HashMap> getYmlServices() throws Exception {
        if (this.yml != null && !this.yml.isEmpty()) {
            YAMLFactory yf = new YAMLFactory();
            ObjectMapper mapper = new ObjectMapper(yf);
            Object object;
            object = mapper.readValue(this.yml, Object.class);

            Map<String, HashMap<String, HashMap>> dockerComposeMap = (HashMap) object;
            return dockerComposeMap.get("services");
        } else {
            throw new Exception("Error on get yml services: the yml is empty");
        }
    }

    private void setYmlServices(HashMap<String, HashMap> services)
            throws Exception {
        if (this.yml != null && !this.yml.isEmpty()) {
            YAMLFactory yf = new YAMLFactory();
            ObjectMapper mapper = new ObjectMapper(yf);
            Object object;
            object = mapper.readValue(this.yml, Object.class);

            Map<String, HashMap<String, HashMap>> dockerComposeMap = (HashMap) object;
            dockerComposeMap.remove("services");
            dockerComposeMap.put("services", services);

            StringWriter writer = new StringWriter();

            yf.createGenerator(writer).writeObject(object);
            this.yml = writer.toString();
        } else {
            throw new Exception("Error on get yml services: the yml is empty");
        }
    }

    public List<String> getImagesFromYML() throws Exception {
        List<String> images = new ArrayList<>();
        Map<String, HashMap> servicesMap = getYmlServices();
        for (HashMap.Entry<String, HashMap> service : servicesMap.entrySet()) {
            HashMap<String, String> serviceContent = service.getValue();
            String imageKey = "image";
            // If service has image, pull
            if (serviceContent.containsKey(imageKey)) {
                String image = serviceContent.get(imageKey);
                images.add(image);
            }
        }

        return images;
    }

    /* ** Exposed ports ** */

    public void bindAllExposedPortsToRandom() throws Exception {
        HashMap<String, HashMap> servicesMap = getYmlServices();
        for (HashMap.Entry<String, HashMap> service : servicesMap.entrySet()) {
            service = this.bindExposedServicePortsToRandom(service);
        }
        setYmlServices(servicesMap);
    }

    private HashMap.Entry<String, HashMap> bindExposedServicePortsToRandom(
            HashMap.Entry<String, HashMap> service) throws IOException {

        HashMap serviceContent = service.getValue();
        String exposedKey = "expose";
        String bindKey = "ports";

        if (serviceContent.containsKey(exposedKey)) {

            List<Integer> exposedPorts = (List<Integer>) serviceContent
                    .get(exposedKey);

            for (Integer exposedPort : exposedPorts) {
                int hostPort = this.findRandomOpenPort();

                List<String> bindList = new ArrayList<>();
                if (serviceContent.containsKey(bindKey)) {
                    bindList = (List) serviceContent.get(bindKey);
                }

                bindList.add(hostPort + ":" + exposedPort);

                // Add to service
                serviceContent.put(bindKey, bindList);
            }

            serviceContent.remove(exposedKey);
        }

        return service;
    }

    /* ** Env vars ** */

    public void setEnvVarsToYmlServices() throws Exception {
        HashMap<String, HashMap> servicesMap = getYmlServices();
        for (HashMap.Entry<String, HashMap> service : servicesMap.entrySet()) {
            service = this.setEnvVarsToYmlService(service);
        }
        setYmlServices(servicesMap);
    }

    private HashMap.Entry<String, HashMap> setEnvVarsToYmlService(
            HashMap.Entry<String, HashMap> service) {
        if (this.env.size() > 0) {
            HashMap serviceContent = service.getValue();
            String environmentKey = "environment";
            Map<String, String> serviceEnvironments = new HashMap<>();
            if (serviceContent.containsKey(environmentKey)) {
                List<String> existentEnvs = (List<String>) serviceContent
                        .get(environmentKey);
                serviceEnvironments = mapFromStringList(existentEnvs, "=");
                serviceContent.remove(environmentKey);
            }
            for (Entry<String, String> entry : this.env.entrySet()) {
                serviceEnvironments.put(entry.getKey(), entry.getValue());
            }
            // Add to service
            serviceContent.put(environmentKey, envsAsList(serviceEnvironments));
        }
        return service;
    }

    public int findRandomOpenPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

}
