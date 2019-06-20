package io.elastest.etm.model.external;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.Project.ProjectMediumView;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.TJob.TJobCompleteView;
import io.elastest.etm.model.external.ExternalElasticsearch.ExternalElasticsearchView;
import io.elastest.etm.model.external.ExternalMonitoringDB.ExternalMonitoringDBView;
import io.elastest.etm.model.external.ExternalMonitoringDBForLogs.ExternalMonitoringDBForLogsView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalPrometheus.ExternalPrometheusView;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ExternalMonitoringDBForMetrics {
    public interface ExternalMonitoringDBForMetricsView {
    }

    @Id
    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class,
            SutView.class, ProjectMediumView.class, ExternalProjectView.class,
            ExternalTJobView.class, TJobCompleteView.class })
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class,
            SutView.class, ProjectMediumView.class, ExternalProjectView.class,
            ExternalTJobView.class, TJobCompleteView.class })
    @Column(name = "type", nullable = false)
    @JsonProperty("type")
    ExternalMonitoringDBForMetricsType type;

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            ExternalPrometheusView.class, SutView.class,
            ProjectMediumView.class, ExternalProjectView.class,
            ExternalTJobView.class, TJobCompleteView.class })
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "externalMonitoringDB")
    @JsonIgnoreProperties(value = "externalMonitoringDBForMetrics", allowSetters = true)
    ExternalMonitoringDB externalMonitoringDB;

    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "externalMonitoringDBForMetrics")
    @JoinColumn(name = "sutSpecification")
    @JsonIgnoreProperties(value = "externalMonitoringDBForMetrics")
    private SutSpecification sutSpecification;

    /* ************************** */
    /* ****** Constructors ****** */
    /* ************************** */

    public ExternalMonitoringDBForMetrics() {
    }

    public ExternalMonitoringDBForMetrics(Long id,
            ExternalMonitoringDBForMetricsType type,
            ExternalMonitoringDB externalMonitoringDB) {
        this.id = id == null ? 0 : id;
        this.type = type;
        this.externalMonitoringDB = externalMonitoringDB;
    }

    public ExternalMonitoringDBForMetrics(Long id,
            ExternalMonitoringDBForMetricsType type,
            ExternalElasticsearch externalES) {
        this.id = id == null ? 0 : id;
        this.type = type;
        this.externalMonitoringDB = externalES;
        if (this.type == null) {
            this.type = ExternalMonitoringDBForMetricsType.ELASTICSEARCH;
        }
    }

    public ExternalMonitoringDBForMetrics(Long id,
            ExternalMonitoringDBForMetricsType type,
            ExternalPrometheus externalPrometheus) {
        this.id = id == null ? 0 : id;
        this.type = type;
        this.externalMonitoringDB = externalPrometheus;
        if (this.type == null) {
            this.type = ExternalMonitoringDBForMetricsType.PROMETHEUS;
        }
    }

    public ExternalMonitoringDBForMetrics(
            ExternalMonitoringDBForMetrics externalMonitoringDBForMetrics) {
        this.setId(null);
        if (externalMonitoringDBForMetrics != null) {
            this.type = externalMonitoringDBForMetrics.getType();
            this.externalMonitoringDB = externalMonitoringDBForMetrics
                    .getExternalMonitoringDB();
        }
    }

    /* *************************** */
    /* *** Getters and setters *** */
    /* *************************** */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id == null ? 0 : id;
    }

    public ExternalMonitoringDBForMetricsType getType() {
        return type;
    }

    public void setType(ExternalMonitoringDBForMetricsType type) {
        this.type = type;
    }

    public ExternalMonitoringDB getExternalMonitoringDB() {
        return externalMonitoringDB;
    }

    public void setExternalMonitoringDB(
            ExternalMonitoringDB externalMonitoringDB) {
        if (type == ExternalMonitoringDBForMetricsType.ELASTICSEARCH) {
            setExternalElasticsearch(
                    (ExternalElasticsearch) externalMonitoringDB);
        } else if (type == ExternalMonitoringDBForMetricsType.PROMETHEUS) {
            setExternalPrometheus((ExternalPrometheus) externalMonitoringDB);
        } else {
            this.externalMonitoringDB = externalMonitoringDB;
        }
    }

    public void setExternalElasticsearch(ExternalElasticsearch externalES) {
        this.externalMonitoringDB = externalES;
    }

    public void setExternalPrometheus(ExternalPrometheus externalPrometheus) {
        this.externalMonitoringDB = externalPrometheus;
    }

    public SutSpecification getSutSpecification() {
        return sutSpecification;
    }

    public void setSutSpecification(SutSpecification sutSpecification) {
        this.sutSpecification = sutSpecification;
    }

    public enum ExternalMonitoringDBForMetricsType {
        ELASTICSEARCH("ELASTICSEARCH"), PROMETHEUS("PROMETHEUS"), NONE("NONE");

        private String value;

        ExternalMonitoringDBForMetricsType(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ExternalMonitoringDBForMetricsType fromValue(
                String text) {
            for (ExternalMonitoringDBForMetricsType b : ExternalMonitoringDBForMetricsType
                    .values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    /* *************************** */
    /* ****** Other methods ****** */
    /* *************************** */
    @Override
    public String toString() {
        return "ExternalMonitoringDBForMetrics [id=" + id + ", type=" + type
                + ", externalMonitoringDB=" + externalMonitoringDB
                + ", sutSpecification=" + sutSpecification + "]";
    }

    public boolean isUsingExternalElasticsearchForMetrics() {
        return this
                .getType() == ExternalMonitoringDBForMetricsType.ELASTICSEARCH;
    }

    public boolean isUsingExternalPrometheusForMetrics() {
        return this.getType() == ExternalMonitoringDBForMetricsType.PROMETHEUS;
    }
}
