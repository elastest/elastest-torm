package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Entity that represents an ElasTest file.")
public class ElastestFile {

    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("encodedUrl")
    private String encodedUrl;

    @JsonProperty("folderName")
    private String folderName;

    public ElastestFile(String name, String url, String encodedUrl,
            String folderName) {
        super();
        this.name = name;
        this.url = url;
        this.encodedUrl = encodedUrl;
        this.folderName = folderName;
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

    public String getEncodedUrl() {
        return encodedUrl;
    }

    public void setEncodedUrl(String encodedUrl) {
        this.encodedUrl = encodedUrl;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String serviceName) {
        this.folderName = serviceName;
    }

    @Override
    public String toString() {
        return "TJobExecutionFile [name=" + name + ", url=" + url
                + ", encodedUrl=" + encodedUrl + ", folderName=" + folderName
                + "]";
    }
    
    
}
