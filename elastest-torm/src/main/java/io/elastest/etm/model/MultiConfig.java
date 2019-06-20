package io.elastest.etm.model;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.ProjectMediumView;
import io.elastest.etm.model.SutExecution.SutExecView;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.TJob.TJobMediumView;
import io.elastest.etm.model.TJobExecution.TJobExecCompleteView;
import io.swagger.annotations.ApiModel;

@Embeddable
@ApiModel(description = "Configuration for multi tJob.")
public class MultiConfig {

    public interface MultiConfigView {
    }

    @JsonView({ MultiConfigView.class, TJobMediumView.class,
            ProjectMediumView.class, TJobExecCompleteView.class, SutView.class,
            SutExecView.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ MultiConfigView.class, TJobMediumView.class,
            ProjectMediumView.class, TJobExecCompleteView.class, SutView.class,
            SutExecView.class })
    @Column(name = "configValues", length = 16777215)
    @JsonProperty("configValues")
    private ArrayList<String> configValues = null;

    // Constructors
    public MultiConfig() {
    }

    public MultiConfig(String name, ArrayList<String> configValues) {
        this.name = name;
        this.configValues = configValues;
    }

    public MultiConfig(MultiConfig fieldFilter) {
        if (fieldFilter != null) {
            this.name = fieldFilter.getName();
            if (fieldFilter.getValues() != null) {
                this.configValues = new ArrayList<>(fieldFilter.getValues());
            }
        }
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getValues() {
        return configValues;
    }

    public void setValues(ArrayList<String> configValues) {
        this.configValues = configValues;
    }

    // Others
    @Override
    public String toString() {
        return "MultiConfig [name=" + name + ", values=" + configValues + "]";
    }

}
