package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class LogAnalyzerConfig {

    public interface LogAnalyzerConfigView {
    }

    @JsonView({ LogAnalyzerConfigView.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ LogAnalyzerConfigView.class })
    @Column(name = "columnsConfig", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("columnsConfig")
    private String columnsConfig = null;

    public LogAnalyzerConfig() {
    }

    public LogAnalyzerConfig(Long id, String columnsConfig) {
        super();
        this.id = id;
        this.columnsConfig = columnsConfig;
    }

    /**
     * Get id
     * 
     * @return id
     **/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get columnsConfig
     * 
     * @return columnsConfig
     **/

    public String getColumnsConfig() {
        return columnsConfig;
    }

    public void setColumnsConfig(String columnsConfig) {
        this.columnsConfig = columnsConfig;
    }

    /* ****** */
    /* Others */
    /* ****** */
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogAnalyzerConfig logAnalyzer = (LogAnalyzerConfig) o;
        return Objects.equals(this.id, logAnalyzer.id)

                && Objects.equals(this.columnsConfig,
                        logAnalyzer.columnsConfig);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, columnsConfig);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TJob {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    columnsConfig: ").append(toIndentedString(columnsConfig))
                .append("\n");
        return sb.toString();
    }

}
