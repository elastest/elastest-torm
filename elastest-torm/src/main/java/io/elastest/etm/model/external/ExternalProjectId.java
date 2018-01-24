package io.elastest.etm.model.external;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.external.ExternalProject.BasicAttExternalProject;

@Embeddable
public class ExternalProjectId implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonView({ BasicAttExternalProject.class })
    @Column(name = "pjExternalId")
    @JsonProperty("pjExternalId")
    String pjExternalId;

    @JsonView({ BasicAttExternalProject.class })
    @Column(name = "externalSystemId")
    @JsonProperty("externalSystemId")
    String externalSystemId;

    public ExternalProjectId() {
    }

    public ExternalProjectId(String pjExternalId, String externalSystemId) {
        this.pjExternalId = pjExternalId == null ? "0" : pjExternalId;
        this.externalSystemId = externalSystemId == null ? "0"
                : externalSystemId;
    }

    public ExternalProjectId(Integer pjExternalId, String externalSystemId) {
        this.pjExternalId = pjExternalId == null ? "0"
                : pjExternalId.toString();
        this.externalSystemId = externalSystemId == null ? "0"
                : externalSystemId;
    }

    public String getPjExternalId() {
        return pjExternalId;
    }

    public void setPjExternalId(String pjExternalId) {
        this.pjExternalId = pjExternalId == null ? "0" : pjExternalId;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId == null ? "0"
                : externalSystemId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((externalSystemId == null) ? 0
                : externalSystemId.hashCode());
        result = prime * result
                + ((pjExternalId == null) ? 0 : pjExternalId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExternalProjectId other = (ExternalProjectId) obj;
        if (externalSystemId == null) {
            if (other.externalSystemId != null)
                return false;
        } else if (!externalSystemId.equals(other.externalSystemId))
            return false;
        if (pjExternalId == null) {
            if (other.pjExternalId != null)
                return false;
        } else if (!pjExternalId.equals(other.pjExternalId))
            return false;
        return true;
    }

}
