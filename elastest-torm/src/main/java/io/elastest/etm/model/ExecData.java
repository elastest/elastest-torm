package io.elastest.etm.model;

import java.util.List;

public class ExecData {
    private List<Parameter> tJobParams;
    private List<Parameter> sutParams;
    private List<MultiConfig> multiConfigurations;

    public ExecData() {
    }

    public ExecData(List<Parameter> tJobParams, List<Parameter> sutParams,
            List<MultiConfig> multiConfigs) {
        this.tJobParams = tJobParams;
        this.sutParams = sutParams;
        this.multiConfigurations = multiConfigs;
    }

    public List<Parameter> gettJobParams() {
        return tJobParams;
    }

    public void settJobParams(List<Parameter> tJobParams) {
        this.tJobParams = tJobParams;
    }

    public List<Parameter> getSutParams() {
        return sutParams;
    }

    public void setSutParams(List<Parameter> sutParams) {
        this.sutParams = sutParams;
    }

    public List<MultiConfig> getMultiConfigurations() {
        return multiConfigurations;
    }

    public void setMultiConfigurations(List<MultiConfig> multiConfigurations) {
        this.multiConfigurations = multiConfigurations;
    }

    @Override
    public String toString() {
        return "ExecData [tJobParams=" + tJobParams + ", sutParams=" + sutParams
                + ", multiConfigurations=" + multiConfigurations + "]";
    }

}
