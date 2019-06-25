package io.elastest.etm.model.external;

import java.util.ArrayList;
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
            @JsonProperty("contentFieldName") String contentFieldName,
            @JsonProperty("traceNameField") String traceNameField,
            @JsonProperty("useESIndicesByExecution") Boolean useESIndicesByExecution) {
        super(id, ip, port, path, protocol, user, pass, streamFields,
                contentFieldName,traceNameField);
        this.indices = indices;
        this.fieldFilters = new ArrayList<>();
        this.useESIndicesByExecution = useESIndicesByExecution != null
                ? useESIndicesByExecution
                : false;
    }

    public ExternalElasticsearch(ExternalElasticsearch externalElasticsearch) {
        super(externalElasticsearch);
        this.indices = externalElasticsearch.getIndices();

        if (externalElasticsearch.getFieldFilters() != null) {
            this.fieldFilters = new ArrayList<>();
            for (MultiConfig fieldFilter : externalElasticsearch
                    .getFieldFilters()) {
                this.fieldFilters.add(new MultiConfig(fieldFilter));
            }
        }

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
        return "ExternalElasticsearch [indices=" + indices + ", fieldFilters="
                + fieldFilters + ", useESIndicesByExecution="
                + useESIndicesByExecution + ", toString()=" + super.toString()
                + "]";
    }
}
