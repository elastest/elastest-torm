package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.List;

import io.elastest.epm.client.model.DockerServiceStatus;

public class TestEngine extends DockerServiceStatus {
    private String engineName;
    private String url;
    private List<String> imagesList = new ArrayList<>();

    public TestEngine() {
    }

    public TestEngine(String engineName) {
        super();
        this.engineName = engineName;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
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

    @Override
    public String toString() {
        return "TestEngine [engineName=" + engineName + ", url=" + url
                + ", imagesList=" + imagesList + ", statusMsg=" + getStatusMsg()
                + ", status=" + getStatusMsg() + "]";
    }

    public void initToDefault() {
        this.setImagesList(new ArrayList<>());
        this.setUrl("");

        super.initToDefault();
    }

}
