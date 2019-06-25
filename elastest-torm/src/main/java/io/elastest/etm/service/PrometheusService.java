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

    protected UtilsService utilsService;

    PrometheusApiClient prometheusClient;

    public PrometheusService(String prometheusApiUrl,
            UtilsService utilsService) {
        this.prometheusApiUrl = prometheusApiUrl;
        this.utilsService = utilsService;
        init();
    }

    public PrometheusService(String prometheusApiUrl, String user, String pass,
            String path, UtilsService utilsService) {
        this.prometheusApiUrl = prometheusApiUrl;
        this.user = !"".equals(user) ? user : null;
        this.pass = pass;
        this.path = path;
        this.utilsService = utilsService;
        init();
    }

    public PrometheusService(String protocol, String host, int port,
            UtilsService utilsService) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.utilsService = utilsService;
    }

    public PrometheusService(String protocol, String host, int port,
            String user, String pass, String path, UtilsService utilsService) {
        this(protocol, host, port, utilsService);
        this.user = !"".equals(user) ? user : null;
        this.pass = pass;
        this.path = path;
        init();
    }

    public PrometheusService(String protocol, String host, String port,
            String user, String pass, String path, UtilsService utilsService) {
        this(protocol, host, Integer.parseInt(port), user, pass, path,
                utilsService);
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

        String startTime = utilsService.getIso8601UTCStrFromDate(startDate);
        String endTime = utilsService.getIso8601UTCStrFromDate(endDate);

        List<String> labels = getAllLabelNames();
        if (labels != null) {
            for (String label : labels) {
                PrometheusApiResponse<PrometheusQueryData> metricData = getMetricByRange(
                        label, startTime, endTime);
                if (metricData != null) {
                    traces.add(metricData.getData());
                }
            }
        }

        return traces;
    }

}
