package io.elastest.etm.model.external;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.model.MultiConfig;
import io.elastest.etm.model.Project.ProjectMediumView;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.external.ExternalMonitoringDBForLogs.ExternalMonitoringDBForLogsView;
import io.elastest.etm.model.external.ExternalMonitoringDBForMetrics.ExternalMonitoringDBForMetricsView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonTypeName("ELASTICSEARCH")
public class ExternalElasticsearch extends ExternalMonitoringDB {

    public interface ExternalElasticsearchView {
    }

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "indices")
    @JsonProperty("indices")
    private String indices;

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "streamFields")
    @JsonProperty("streamFields")
    private String streamFields;

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @ElementCollection
    @CollectionTable(name = "ExternalElasticsearchFieldFilters", joinColumns = @JoinColumn(name = "ExternalElasticsearch"))
    @MapKeyColumn(name = "NAME")
    @Column(name = "VALUES", length = 16777215)
    private List<MultiConfig> fieldFilters = new ArrayList<MultiConfig>();

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "useESIndicesByExecution")
    @JsonProperty("useESIndicesByExecution")
    private Boolean useESIndicesByExecution;

    /* ************************** */
    /* ****** Constructors ****** */
    /* ************************** */

    @JsonCreator
    public ExternalElasticsearch() {
    }

    @JsonCreator
    public ExternalElasticsearch(@JsonProperty("id") Long id,
            @JsonProperty("ip") String ip, @JsonProperty("port") String port,
            @JsonProperty("path") String path,
            @JsonProperty("user") String user,
            @JsonProperty("pass") String pass,
            @JsonProperty("indices") String indices,
            @JsonProperty("protocol") ProtocolEnum protocol,
            @JsonProperty("streamFields") String streamFields,
            @JsonProperty("useESIndicesByExecution") Boolean useESIndicesByExecution) {
        super(id, ip, port, path, protocol, user, pass);
        this.indices = indices;
        this.streamFields = streamFields;
        this.fieldFilters = new ArrayList<>();
        this.useESIndicesByExecution = useESIndicesByExecution != null
                ? useESIndicesByExecution
                : false;
    }

    public ExternalElasticsearch(ExternalElasticsearch externalElasticsearch) {
        super(externalElasticsearch);
        this.indices = externalElasticsearch.getIndices();
        this.streamFields = externalElasticsearch.getStreamFields();
        this.fieldFilters = externalElasticsearch.getFieldFilters();
        this.useESIndicesByExecution = externalElasticsearch
                .getUseESIndicesByExecution() != null
                        ? externalElasticsearch.getUseESIndicesByExecution()
                        : false;
    }

    /* *************************** */
    /* *** Getters and setters *** */
    /* *************************** */

    public String getIndices() {
        return indices;
    }

    public void setIndices(String indices) {
        this.indices = indices;
    }

    public String getStreamFields() {
        return streamFields;
    }

    public List<String> getStreamFieldsAsList() {
        List<String> streamFieldsList = new ArrayList<>();

        if (streamFields != null && !streamFields.isEmpty()) {
            streamFieldsList = Arrays.asList(streamFields.split(","));
        }

        return streamFieldsList;
    }

    public void setStreamFields(String streamFields) {
        this.streamFields = streamFields;
    }

    public List<MultiConfig> getFieldFilters() {
        return fieldFilters;
    }

    public void setFieldFilters(List<MultiConfig> fieldFilters) {
        this.fieldFilters = fieldFilters;
    }

    public Boolean getUseESIndicesByExecution() {
        return useESIndicesByExecution != null ? useESIndicesByExecution
                : false;
    }

    public void setUseESIndicesByExecution(Boolean useESIndicesByExecution) {
        this.useESIndicesByExecution = useESIndicesByExecution != null
                ? useESIndicesByExecution
                : false;
    }

    @Override
    public String toString() {
        return "ExternalElasticsearch [indices=" + indices + ", streamFields="
                + streamFields + ", fieldFilters=" + fieldFilters
                + ", useESIndicesByExecution=" + useESIndicesByExecution
                + ", toString()=" + super.toString() + "]";
    }
}
