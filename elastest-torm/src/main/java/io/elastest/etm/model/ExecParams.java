package io.elastest.etm.model;

import java.util.List;

public class ExecParams {
    private List<Parameter> tJobParams;
    private List<Parameter> sutParams;

    public ExecParams() {
    }

    public ExecParams(List<Parameter> tJobParams, List<Parameter> sutParams) {
        this.tJobParams = tJobParams;
        this.sutParams = sutParams;
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

}
