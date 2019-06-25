package io.elastest.etm.prometheus.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrometheusQueryDataResult {

    Map<String, Object> metric;

    List<Object> value;

    // For query range
    List<List<Object>> values;

    public PrometheusQueryDataResult() {
    }

    public PrometheusQueryDataResult(Map<String, Object> metric,
            ArrayList<Object> value) {
        super();
        this.metric = metric;
        this.value = value;
    }

    public PrometheusQueryDataResult(Map<String, Object> metric,
            List<List<Object>> values) {
        super();
        this.metric = metric;
        this.values = values;
    }

    public Map<String, Object> getMetric() {
        return metric;
    }

    public void setMetric(Map<String, Object> metric) {
        this.metric = metric;
    }

    public List<Object> getValue() {
        return value;
    }

    public void setValue(List<Object> value) {
        this.value = value;
    }

    public List<List<Object>> getValues() {
        return values;
    }

    public void setValues(List<List<Object>> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "PrometheusQueryDataResult [metric=" + metric + ", value="
                + value + ", values=" + values + "]";
    }

}
