package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;

import io.elastest.etm.prometheus.client.PrometheusApiClient;
import io.elastest.etm.prometheus.client.PrometheusApiResponse;
import io.elastest.etm.prometheus.client.PrometheusQueryData;

public class PrometheusService {
    protected final Logger logger = getLogger(lookup().lookupClass());

    private String prometheusApiUrl;

    private String protocol;
    private String host;
    private int port;

    private String user;
    private String pass;
    private String path;

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

    public void getMetric(String metricName) {

        PrometheusApiResponse<PrometheusQueryData> a = this.prometheusClient
                .executeQuery(metricName);

        logger.debug("aaaaaaa {}", a);

    }

}
