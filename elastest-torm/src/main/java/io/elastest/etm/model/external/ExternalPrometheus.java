package io.elastest.etm.model.external;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ExternalPrometheus extends ExternalMonitoringDB {

    public interface ExternalPrometheusView {
    }

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalMonitoringDBView.class, ExternalPrometheusView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @ElementCollection
    @CollectionTable(name = "ExternalPrometheusViewFieldFilters", joinColumns = @JoinColumn(name = "ExternalPrometheusView"))
    @MapKeyColumn(name = "NAME")
    @Column(name = "VALUES", length = 16777215)
    private List<MultiConfig> fieldFilters = new ArrayList<MultiConfig>();

    /* ************************** */
    /* ****** Constructors ****** */
    /* ************************** */

    public ExternalPrometheus() {
    }

    public ExternalPrometheus(Long id, String ip, String port, String path,
            ProtocolEnum protocol, String user, String pass,
            @JsonProperty("streamFields") String streamFields,
            @JsonProperty("contentFieldName") String contentFieldName,
            @JsonProperty("traceNameField") String traceNameField) {
        super(id, ip, port, path, protocol, user, pass, streamFields,
                contentFieldName, traceNameField);
        this.fieldFilters = new ArrayList<>();
    }

    public ExternalPrometheus(ExternalPrometheus externalPrometheus) {
        super(externalPrometheus);

        if (externalPrometheus.getFieldFilters() != null) {
            this.fieldFilters = new ArrayList<>();
            for (MultiConfig fieldFilter : externalPrometheus
                    .getFieldFilters()) {
                this.fieldFilters.add(new MultiConfig(fieldFilter));
            }
        }
    }

    /* *************************** */
    /* *** Getters and setters *** */
    /* *************************** */

    public List<MultiConfig> getFieldFilters() {
        return fieldFilters;
    }

    public void setFieldFilters(List<MultiConfig> fieldFilters) {
        this.fieldFilters = fieldFilters;
    }

    @Override
    public String toString() {
        return "ExternalPrometheus [toString()=" + ", fieldFilters="
                + fieldFilters + super.toString() + "]";
    }

}
