package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ImageInfo;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Object that stores the relevant information about the version of an ElasTest module.")
public class VersionInfo {

    @JsonProperty("commitId")
    private String commitId;

    @JsonProperty("commitDate")
    private String commitDate;

    @JsonProperty("tag")
    private String tag;

    public VersionInfo(String commitId, String date, String tag) {
        super();
        this.commitId = commitId;
        this.commitDate = date;
        this.tag = tag;
    }

    public VersionInfo(ImageInfo imageInfo) {
        String commit = null;
        String commitDate = null;
        String tag = null;

        if (imageInfo.config() != null && imageInfo.config().labels() != null) {
            commit = imageInfo.config().labels().get("git_commit");
            commitDate = imageInfo.config().labels().get("commit_date");
            tag = imageInfo.config().labels().get("version");
        }

        this.commitId = commit;
        this.commitDate = commitDate;
        this.tag = tag;
    }

    public VersionInfo(Container container) {
        String commit = null;
        String commitDate = null;
        String tag = null;

        if (container.labels() != null) {
            commit = container.labels().get("git_commit");
            commitDate = container.labels().get("commit_date");
            tag = container.labels().get("version");
        }

        this.commitId = commit;
        this.commitDate = commitDate;
        this.tag = tag;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "VersionInfo [commitId=" + commitId + ", commitDate="
                + commitDate + ", tag=" + tag + "]";
    }

}
