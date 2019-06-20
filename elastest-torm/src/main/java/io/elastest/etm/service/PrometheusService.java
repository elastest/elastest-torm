package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import io.elastest.etm.prometheus.client.PrometheusApiClient;
import io.elastest.etm.prometheus.client.PrometheusApiResponse;
import io.elastest.etm.prometheus.client.PrometheusQueryData;
import io.elastest.etm.utils.UtilsService;

public class PrometheusService {
    protected final Logger logger = getLogger(lookup().lookupClass());

    private String prometheusApiUrl;

    private String protocol;
    private String host;
    private int port;

    private String user;
    private String pass;
    private String path;

    @Autowired
    protected UtilsService utilsService;

    PrometheusApiClient prometheusClient;

    public PrometheusService(String prometheusApiUrl) {
        this.prometheusApiUrl = prometheusApiUrl;
        init();
    }

    public PrometheusService(String prometheusApiUrl, String user, String pass,
            String path) {
        this.prometheusApiUrl = prometheusApiUrl;
        this.user = !"".equals(user) ? user : null;
        this.pass = pass;
        this.path = path;
        init();
    }

    public PrometheusService(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public PrometheusService(String protocol, String host, int port,
            String user, String pass, String path) {
        this(protocol, host, port);
        this.user = !"".equals(user) ? user : null;
        this.pass = pass;
        this.path = path;
        init();
    }

    public PrometheusService(String protocol, String host, String port,
            String user, String pass, String path) {
        this(protocol, host, Integer.parseInt(port), user, pass, path);
    }

    private void init() {
        URL url;
        try {
            if (this.prometheusApiUrl == null
                    || this.prometheusApiUrl.equals("")) {
                this.prometheusApiUrl = this.protocol + "://" + this.host + ":"
                        + this.port;
            } else {
                url = new URL(this.prometheusApiUrl);
                this.protocol = url.getProtocol();
                this.host = url.getHost();
                this.port = url.getPort();
            }

            String finalPrometheusApiUrl = this.prometheusApiUrl;

            if (this.path != null && !"".equals(this.path)) {
                finalPrometheusApiUrl = finalPrometheusApiUrl
                        + (this.path.startsWith("/") ? this.path
                                : "/" + this.path);
            }

            if (this.user != null) {
                this.prometheusClient = new PrometheusApiClient(
                        finalPrometheusApiUrl, this.user, this.pass);
            } else {
                this.prometheusClient = new PrometheusApiClient(
                        finalPrometheusApiUrl);
            }

        } catch (MalformedURLException e) {
            logger.error("Cannot get Prometheus url by given: {}",
                    this.prometheusApiUrl);
        }

    }

    /* ************************************* */
    /* ************** Methods ************** */
    /* ************************************* */

    public boolean isHealthy() {

        return this.prometheusClient.isHealthy();
    }

    public boolean isReady() {
        logger.debug("prometheusService.isReady() {}",
                prometheusClient.isReady());
        return this.prometheusClient.isReady();
    }

    public List<String> getAllLabelNames() {
        return prometheusClient.getAllLabelNames();
    }

    public void getMetric(String metricName) {

        PrometheusApiResponse<PrometheusQueryData> a = this.prometheusClient
                .executeQuery(metricName);

        logger.debug("aaaaaaa {}", a);
        // TODO
    }

    public PrometheusApiResponse<PrometheusQueryData> getMetricByRange(
            String metricName, String start, String end) {
        return this.prometheusClient.executeQueryRange(metricName, start, end);
    }

    public List<PrometheusQueryData> searchTraces(Date startDate, Date endDate)
            throws ParseException {
        List<PrometheusQueryData> traces = new ArrayList<>();

        Double startTime = (double) (utilsService
                .getIso8601UTCDateFromDate(startDate).getTime() / 1000);
        Double endTime = (double) utilsService
                .getIso8601UTCDateFromDate(endDate).getTime() / 1000;

        List<String> labels = getAllLabelNames();
        if (labels != null) {
            for (String label : labels) {
                PrometheusApiResponse<PrometheusQueryData> metricData = getMetricByRange(
                        label, startTime.toString(), endTime.toString());
                if (metricData != null) {
                    traces.add(metricData.getData());
                }
            }
        }

        return traces;
    }

}
