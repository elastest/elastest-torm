package io.elastest.epm.client.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerProject {
    
    String name;
    String yml;
    Map<String, String> env = new HashMap<>();

    public DockerProject(String name, String yml,
            List<String> envList) {
        this.name = name;
        this.yml = yml;
        this.env = new HashMap<>();

        for (String var : envList) {
            String[] envPair = var.split("=");
            if (envPair != null && envPair.length == 2) {
                this.env.put(envPair[0], envPair[1]);
            }
        }
    }

    public DockerProject(String name, String yml) {
        this.name = name;
        this.yml = yml;
        this.env = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getYml() {
        return yml;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    @Override
    public String toString() {
        return "DockerComposeCreateProject [name=" + name + ", yml=" + yml
                + ", env=" + env + "]";
    }

}
