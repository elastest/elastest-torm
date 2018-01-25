package io.elastest.etm.model.external;

import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.ws.rs.WebApplicationException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.elastest.etm.model.external.ExternalProject.BasicAttExternalProject;
import io.elastest.etm.model.external.ExternalTestCase.BasicAttExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution.BasicAttExternalTestExecution;

@Embeddable
public class ExternalId implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "externalId")
    @JsonProperty("externalId")
    String externalId;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "externalSystemId")
    @JsonProperty("externalSystemId")
    String externalSystemId;

    public ExternalId() {
    }

    public ExternalId(String externalId, String externalSystemId) {
        this.externalId = externalId == null ? "0" : externalId;
        this.externalSystemId = externalSystemId == null ? "0"
                : externalSystemId;
    }

    public ExternalId(Integer externalId, String externalSystemId) {
        this.externalId = externalId == null ? "0" : externalId.toString();
        this.externalSystemId = externalSystemId == null ? "0"
                : externalSystemId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId == null ? "0" : externalId;
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
                + ((externalId == null) ? 0 : externalId.hashCode());
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
        ExternalId other = (ExternalId) obj;
        if (externalSystemId == null) {
            if (other.externalSystemId != null)
                return false;
        } else if (!externalSystemId.equals(other.externalSystemId))
            return false;
        if (externalId == null) {
            if (other.externalId != null)
                return false;
        } else if (!externalId.equals(other.externalId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExternalId [externalId=" + externalId + ", externalSystemId="
                + externalSystemId + "]";
    }

    /**
     * Deserializes an Object of class ExternalId from its JSON representation
     */
    public static ExternalId fromString(String jsonRepresentation) {
        ObjectMapper mapper = new ObjectMapper(); // Jackson's JSON marshaller
        ExternalId o = null;
        try {
            System.out.println(jsonRepresentation);
            o = mapper.readValue(jsonRepresentation, ExternalId.class);
        } catch (IOException e) {
            throw new WebApplicationException();
        }
        return o;
    }
}
