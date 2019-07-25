package io.elastest.epm.client.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerProject {

    String name;
    String yml;
    Map<String, String> env = new HashMap<>();
    List<String> extraHosts = new ArrayList<String>();

    public DockerProject(String name, String yml) {
        this.name = name;
        this.yml = yml;
        this.env = new HashMap<>();
    }

    public DockerProject(String name, String yml, List<String> envList) {
        this(name, yml);

        for (String var : envList) {
            String[] envPair = var.split("=");
            if (envPair != null && envPair.length == 2) {
                this.env.put(envPair[0], envPair[1]);
            }
        }
    }

    public DockerProject(String name, String yml, List<String> envList,
            List<String> extraHosts) {
        this(name, yml, envList);
        this.extraHosts = extraHosts;
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

    public List<String> getExtraHosts() {
        return extraHosts;
    }

    @Override
    public String toString() {
        return "DockerComposeCreateProject [name=" + name + ", yml=" + yml
                + ", env=" + env + ", extraHosts=" + extraHosts + "]";
    }

}
