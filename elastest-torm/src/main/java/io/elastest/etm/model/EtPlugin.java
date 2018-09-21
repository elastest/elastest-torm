package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.elastest.epm.client.model.DockerServiceStatus;

public class EtPlugin extends DockerServiceStatus {
    private String name;
    private String internalUrl;
    private String bindedUrl;
    private String url;
    private List<String> imagesList = new ArrayList<>();
    private Map<String, String> parameters;
    private String user;
    private String pass;

    public EtPlugin() {
    }

    public EtPlugin(String name) {
        super();
        this.name = name;
        this.parameters = new HashMap<>();
    }

    public EtPlugin(EtPlugin plugin) {
        super();
        this.name = plugin.name;
        this.internalUrl = plugin.internalUrl;
        this.bindedUrl = plugin.bindedUrl;
        this.url = plugin.url;
        this.imagesList = new ArrayList<>(plugin.imagesList);
        this.parameters = new HashMap<>(plugin.parameters);
        this.setStatus(plugin.getStatus());
        this.setStatusMsg(plugin.getStatusMsg());
        this.user = plugin.user;
        this.pass = plugin.pass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    public String getBindedUrl() {
        return bindedUrl;
    }

    public void setBindedUrl(String bindedUrl) {
        this.bindedUrl = bindedUrl;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "EtPlugin [name=" + name + ", internalUrl=" + internalUrl
                + ", bindedUrl=" + bindedUrl + ", url=" + url + ", imagesList="
                + imagesList + ", parameters=" + parameters + ", user=" + user
                + ", pass=" + pass + "]";
    }

    public void initToDefault() {
        this.setImagesList(new ArrayList<>());
        this.setParameters(new HashMap<>());
        this.setUrl("");

        super.initToDefault();
    }

}
