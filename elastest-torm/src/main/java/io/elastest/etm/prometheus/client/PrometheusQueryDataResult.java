package io.elastest.etm.prometheus.client;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrometheusQueryDataResult {

    PrometheusQueryDataResultMetric metric;

    List<Object> value;

    // For query range
    List<List<Object>> values;

    public PrometheusQueryDataResult() {
    }

    public PrometheusQueryDataResult(PrometheusQueryDataResultMetric metric,
            ArrayList<Object> value) {
        super();
        this.metric = metric;
        this.value = value;
    }

    public PrometheusQueryDataResult(PrometheusQueryDataResultMetric metric,
            List<List<Object>> values) {
        super();
        this.metric = metric;
        this.values = values;
    }

    public PrometheusQueryDataResultMetric getMetric() {
        return metric;
    }

    public void setMetric(PrometheusQueryDataResultMetric metric) {
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

    public class PrometheusQueryDataResultMetric {
        private String instance;
        private String job;

        @JsonProperty("__name__")
        private String name;

        public PrometheusQueryDataResultMetric() {
        }

        public PrometheusQueryDataResultMetric(String instance, String job,
                String name) {
            super();
            this.instance = instance;
            this.job = job;
            this.name = name;
        }

        public String getInstance() {
            return instance;
        }

        public void setInstance(String instance) {
            this.instance = instance;
        }

        public String getJob() {
            return job;
        }

        public void setJob(String job) {
            this.job = job;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "PrometheusQueryDataResultMetric [instance=" + instance
                    + ", job=" + job + ", name=" + name + "]";
        }

    }

}
