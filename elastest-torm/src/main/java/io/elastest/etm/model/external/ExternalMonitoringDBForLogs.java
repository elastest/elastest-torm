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

import io.elastest.etm.model.Project.ProjectMediumView;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.TJob.TJobCompleteView;
import io.elastest.etm.model.external.ExternalElasticsearch.ExternalElasticsearchView;
import io.elastest.etm.model.external.ExternalMonitoringDB.ExternalMonitoringDBView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalPrometheus.ExternalPrometheusView;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ExternalMonitoringDBForLogs {

    public interface ExternalMonitoringDBForLogsView {
    }

    @Id
    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            ExternalPrometheusView.class, SutView.class,
            ProjectMediumView.class, ExternalProjectView.class,
            ExternalTJobView.class, TJobCompleteView.class })
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            ExternalPrometheusView.class, SutView.class,
            ProjectMediumView.class, ExternalProjectView.class,
            ExternalTJobView.class, TJobCompleteView.class })
    @Column(name = "type", nullable = false)
    @JsonProperty("type")
    ExternalMonitoringDBForLogsType type;

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            ExternalPrometheusView.class, SutView.class,
            ProjectMediumView.class, ExternalProjectView.class,
            ExternalTJobView.class, TJobCompleteView.class })
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "externalMonitoringDB")
    @JsonIgnoreProperties(value = "externalMonitoringDBForLogs", allowSetters = true)
    ExternalMonitoringDB externalMonitoringDB;

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            ExternalPrometheusView.class })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "externalMonitoringDBForLogs")
    @JoinColumn(name = "sutSpecification")
    @JsonIgnoreProperties(value = "externalMonitoringDBForLogs")
    private SutSpecification sutSpecification;

    /* ************************** */
    /* ****** Constructors ****** */
    /* ************************** */

    public ExternalMonitoringDBForLogs() {
    }

    public ExternalMonitoringDBForLogs(Long id,
            ExternalMonitoringDBForLogsType type,
            ExternalMonitoringDB externalMonitoringDB) {
        this.id = id == null ? 0 : id;
        this.type = type;
        this.setExternalMonitoringDB(externalMonitoringDB);
    }

    public ExternalMonitoringDBForLogs(Long id,
            ExternalMonitoringDBForLogsType type,
            ExternalElasticsearch externalES) {
        this.id = id == null ? 0 : id;
        this.type = type;
        this.setExternalElasticsearch(externalES);
        if (this.type == null) {
            this.type = ExternalMonitoringDBForLogsType.ELASTICSEARCH;
        }
    }

    public ExternalMonitoringDBForLogs(
            ExternalMonitoringDBForLogs externalMonitoringDBForLogs) {
        this.setId(null);
        if (externalMonitoringDBForLogs != null) {
            this.type = externalMonitoringDBForLogs.getType();
            this.externalMonitoringDB = externalMonitoringDBForLogs
                    .getExternalMonitoringDB();
        }
        if (this.type == null) {
            this.type = ExternalMonitoringDBForLogsType.NONE;
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

    public ExternalMonitoringDBForLogsType getType() {
        return type;
    }

    public void setType(ExternalMonitoringDBForLogsType type) {
        this.type = type;
    }

    public ExternalMonitoringDB getExternalMonitoringDB() {
        return externalMonitoringDB;
    }

    public void setExternalMonitoringDB(
            ExternalMonitoringDB externalMonitoringDB) {
        if (type == ExternalMonitoringDBForLogsType.ELASTICSEARCH) {
            setExternalElasticsearch(
                    (ExternalElasticsearch) externalMonitoringDB);
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

    public enum ExternalMonitoringDBForLogsType {
        ELASTICSEARCH("ELASTICSEARCH"), NONE("NONE");

        private String value;

        ExternalMonitoringDBForLogsType(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ExternalMonitoringDBForLogsType fromValue(String text) {
            for (ExternalMonitoringDBForLogsType b : ExternalMonitoringDBForLogsType
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
        return "ExternalMonitoringDBForLogs [id=" + id + ", type=" + type
                + ", externalMonitoringDB=" + externalMonitoringDB
                + ", sutSpecification=" + sutSpecification + "]";
    }

    public boolean isUsingExternalElasticsearchForLogs() {
        return this.getType() == ExternalMonitoringDBForLogsType.ELASTICSEARCH;
    }

}
