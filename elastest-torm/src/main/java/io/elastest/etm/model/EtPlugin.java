package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.elastest.epm.client.model.DockerServiceStatus;

public class EtPlugin extends DockerServiceStatus {
    protected String name;
    protected String internalUrl;
    protected String bindedUrl;
    protected String url;
    protected List<String> imagesList = new ArrayList<>();
    protected Map<String, String> parameters;
    protected String user;
    protected String pass;
    protected String displayName;
    protected String fileName;
    private Map<String, String> additionalLabels = new HashMap<>();

    protected List<String> extraHosts = new ArrayList<>();

    public EtPlugin() {
    }

    public EtPlugin(String name, String displayName) {
        super();
        this.name = name;
        this.parameters = new HashMap<>();
        this.displayName = displayName;
        this.fileName = name;
    }

    public EtPlugin(String name, String displayName, String fileName) {
        super();
        this.name = name;
        this.parameters = new HashMap<>();
        this.displayName = displayName;
        this.fileName = fileName;
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
        this.displayName = plugin.displayName;
        this.fileName = plugin.fileName;
        this.extraHosts = plugin.extraHosts;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getExtraHosts() {
        return extraHosts;
    }

    public void setExtraHosts(List<String> extraHosts) {
        this.extraHosts = extraHosts;
    }

    public Map<String, String> getAdditionalLabels() {
        return additionalLabels;
    }

    public void setAdditionalLabels(Map<String, String> additionalLabels) {
        this.additionalLabels = additionalLabels;
    }

    @Override
    public String toString() {
        return "EtPlugin [name=" + name + ", internalUrl=" + internalUrl
                + ", bindedUrl=" + bindedUrl + ", url=" + url + ", imagesList="
                + imagesList + ", parameters=" + parameters + ", user=" + user
                + ", pass=" + pass + ", displayName=" + displayName
                + ", fileName=" + fileName + ", extraHosts=" + extraHosts
                + ", toString()=" + super.toString() + "]";
    }

    public void initToDefault() {
        this.setImagesList(new ArrayList<>());
        this.setParameters(new HashMap<>());
        this.setUrl("");

        super.initToDefault();
    }

}
