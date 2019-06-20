package io.elastest.etm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.elastest.etm.dao.external.ExternalElasticsearchRepository;
import io.elastest.etm.dao.external.ExternalMonitoringDBRepository;
import io.elastest.etm.dao.external.ExternalPrometheusRepository;
import io.elastest.etm.model.external.ExternalElasticsearch;
import io.elastest.etm.model.external.ExternalPrometheus;
import io.elastest.etm.utils.UtilsService;

@Service
public class ExternalMonitoringDBService {

    private static final Logger logger = LoggerFactory
            .getLogger(ExternalMonitoringDBService.class);

    private final UtilsService utilsService;
    private final ExternalMonitoringDBRepository externalMonitoringDBRepository;
    private final ExternalElasticsearchRepository externalElasticsearchRepository;
    private final ExternalPrometheusRepository externalPrometheusRepository;

    public ExternalMonitoringDBService(UtilsService utilsService,
            ExternalMonitoringDBRepository externalMonitoringDBRepository,
            ExternalElasticsearchRepository externalElasticsearchRepository,
            ExternalPrometheusRepository externalPrometheusRepository) {
        this.utilsService = utilsService;
        this.externalMonitoringDBRepository = externalMonitoringDBRepository;
        this.externalElasticsearchRepository = externalElasticsearchRepository;
        this.externalPrometheusRepository = externalPrometheusRepository;
    }

    public boolean checkExtElasticsearchConnection(
            ExternalElasticsearch extES) {
        boolean connected = false;
        if (extES != null) {
            try {
                String esApiUrl = extES.getProtocol() + "://" + extES.getIp()
                        + ":" + extES.getPort();
                ElasticsearchService esService = new ElasticsearchService(
                        esApiUrl, extES.getUser(), extES.getPass(),
                        extES.getPath(), utilsService);
                esService.getInfo();
                connected = true;
            } catch (Exception e) {
            }
        }

        return connected;
    }

    public boolean checkExternalPrometheusConnection(
            ExternalPrometheus prometheus) {
        boolean connected = false;
        if (prometheus != null) {
            try {
                PrometheusService prometheusService = new PrometheusService(
                        prometheus.getProtocol().toString(), prometheus.getIp(),
                        prometheus.getPort(), prometheus.getUser(),
                        prometheus.getPass(), prometheus.getPath());

                connected = prometheusService.isReady()
                        && prometheusService.isHealthy();
            } catch (Exception e) {
                logger.error("Error on check External Prometheus Connection",
                        e);
            }
        }

        return connected;
    }

    public ExternalElasticsearch getExternalElasticsearchById(
            Long externalESId) {
        return externalElasticsearchRepository.findById(externalESId).get();
    }

    public ExternalPrometheus getExternalPrometheusById(
            Long externalPrometheusId) {
        return externalPrometheusRepository.findById(externalPrometheusId)
                .get();
    }

}
