package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.elastest.epm.client.model.DockerServiceStatus;

public class EtPlugin extends DockerServiceStatus {
    private String name;
    private String url;
    private List<String> imagesList = new ArrayList<>();
    private Map<String, String> parameters;

    public EtPlugin() {
    }

    public EtPlugin(String name) {
        super();
        this.name = name;
        this.parameters = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getImagesList() {
        return imagesList;
    }

    public void setImagesList(List<String> imagesList) {
        this.imagesList = imagesList;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "TestEngine [name=" + name + ", url=" + url
                + ", imagesList=" + imagesList + ", statusMsg=" + getStatusMsg()
                + ", status=" + getStatusMsg() + "]";
    }

    public void initToDefault() {
        this.setImagesList(new ArrayList<>());
        this.setParameters(new HashMap<>());
        this.setUrl("");

        super.initToDefault();
    }

}