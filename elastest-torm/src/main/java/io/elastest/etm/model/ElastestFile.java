package io.elastest.etm.model;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Entity that represents an ElasTest file.")
public class ElastestFile {

    @JsonProperty("name")
    private String name;

    @JsonProperty("extension")
    private String extension;

    @JsonProperty("url")
    private String url;

    @JsonProperty("encodedUrl")
    private String encodedUrl;

    @JsonProperty("folderName")
    private String folderName;

    @JsonProperty("relativePath")
    private String relativePath;

    @JsonProperty("resourceType")
    private FileResourceTypeEnum resourceType;

    @JsonProperty("serviceName")
    private String serviceName;

    public ElastestFile(String name, String url, String encodedUrl, String folderName,
            String relativePath, String serviceName) {
        super();
        this.name = name;
        this.setExtensionByName();
        this.url = url;
        this.encodedUrl = encodedUrl;
        this.folderName = folderName;
        this.relativePath = relativePath;
        this.setResourceTypeByRelativePath();
        this.serviceName = serviceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setExtensionByName() {
        if (this.name == null) {
            this.setExtension("unknown");
        } else {
            String[] splittedName = this.name.split("\\.");
            if (splittedName.length < 2) {
                this.setExtension("unknown");
            } else {
                extension = splittedName[1].toLowerCase();
            }
        }
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

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public FileResourceTypeEnum getResourceType() {
        return resourceType;
    }

    public void setResourceType(FileResourceTypeEnum resourceType) {
        this.resourceType = resourceType;
    }

    public void setResourceTypeByRelativePath() {
        List<String> videoExtension = Arrays.asList("mp4", "webm", "avi", "mov", "wmv", "vob",
                "mpg", "mpeg", "mkv", "flv", "3gp", "y4m");

        if (this.relativePath == null) {
            this.setResourceType(FileResourceTypeEnum.DEFAULT);
        } else {
            if (this.relativePath.endsWith("eus") || this.relativePath.endsWith("eus/")) {
                if (extension != null) {
                    if (videoExtension.contains(extension)) {
                        this.setResourceType(FileResourceTypeEnum.EUS_BROWSER_RECORDING);
                    } else if (extension.equals("eus")) {
                        this.setResourceType(FileResourceTypeEnum.EUS_SESSION_INFO);
                    } else {
                        this.setResourceType(FileResourceTypeEnum.EUS);
                    }
                } else {
                    this.setResourceType(FileResourceTypeEnum.EUS);
                }
            } else if (this.relativePath.endsWith("eus/qoe")
                    || this.relativePath.endsWith("eus/qoe/")) {
                this.setResourceType(FileResourceTypeEnum.EUS_QOE_RESULT);
            } else {
                this.setResourceType(FileResourceTypeEnum.DEFAULT);
            }
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "ElastestFile [name=" + name + ", extension=" + extension + ", url=" + url
                + ", encodedUrl=" + encodedUrl + ", folderName=" + folderName + ", relativePath="
                + relativePath + ", resourceType=" + resourceType + ", serviceName=" + serviceName
                + "]";
    }

    /* *********************************************** */
    /* ******************** Enums ******************** */
    /* *********************************************** */
    public enum FileResourceTypeEnum {
        DEFAULT("DEFAULT"), EUS("EUS"), EUS_BROWSER_RECORDING("EUS_BROWSER_RECORDING"),
        EUS_SESSION_INFO("EUS_SESSION_INFO"), EUS_QOE_RESULT("EUS_QOE_RESULT");

        private String value;

        FileResourceTypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static FileResourceTypeEnum fromValue(String text) {
            for (FileResourceTypeEnum b : FileResourceTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }
}
