package io.elastest.etm.prometheus.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.elastest.etm.utils.UtilTools;

public class PrometheusQueryData {

    PrometheusQueryDataResultType resultType;

    @JsonProperty("result")
    List<PrometheusQueryDataResult> result;

    public PrometheusQueryData() {
    }

    public PrometheusQueryData(PrometheusQueryDataResultType resultType,
            List<PrometheusQueryDataResult> result) {
        super();
        this.resultType = resultType;
        this.result = result;
    }

    public PrometheusQueryDataResultType getResultType() {
        return resultType;
    }

    public void setResultType(PrometheusQueryDataResultType resultType) {
        this.resultType = resultType;
    }

    public List<PrometheusQueryDataResult> getResult() {
        return result;
    }

    public void setResult(List<PrometheusQueryDataResult> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        String resultString = "";
        if (result != null) {
            for (PrometheusQueryDataResult currentResult : result) {
                if (currentResult != null) {
                    if (!"".equals(resultString)) {
                        resultString += ", ";
                    }
                    resultString += currentResult.toString();
                }
            }
        }

        return "PrometheusQueryData [resultType=" + resultType + ", result=["
                + resultString + "]]";
    }

    public enum PrometheusQueryDataResultType {
        VECTOR("vector"), MATRIX("matrix");

        private String value;

        PrometheusQueryDataResultType(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static PrometheusQueryDataResultType fromValue(String text) {
            for (PrometheusQueryDataResultType b : PrometheusQueryDataResultType
                    .values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

    }

}
