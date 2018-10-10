package io.elastest.etm.model;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.SutExecution.SutExecView;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.swagger.annotations.ApiModel;

@Embeddable
@ApiModel(description = "Configuration for multi tJob.")
public class MultiConfig {

    public interface BasicAttMultiConfig {
    }

    @JsonView({ BasicAttMultiConfig.class, BasicAttTJob.class,
            BasicAttProject.class, BasicAttTJobExec.class, SutView.class,
            SutExecView.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ BasicAttMultiConfig.class, BasicAttTJob.class,
            BasicAttProject.class, BasicAttTJobExec.class, SutView.class,
            SutExecView.class })
    @Column(name = "configValues", length = 16777215)
    @JsonProperty("configValues")
    private ArrayList<String> configValues = null;

    // Constructors
    public MultiConfig() {
    }

    // Getters and setters
    public MultiConfig(Long id, String name, ArrayList<String> configValues,
            TJob tJob, TJobExecution tJobExec) {
        this.name = name;
        this.configValues = configValues;

    }

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
