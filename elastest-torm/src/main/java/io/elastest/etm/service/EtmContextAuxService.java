package io.elastest.etm.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.model.ContextInfo;
import io.elastest.etm.utils.UtilTools;

@Service
public class EtmContextAuxService {
    @Value("${et.public.host}")
    public String etPublicHost;
    @Value("${et.in.prod}")
    public boolean etInProd;

    @Value("${et.proxy.port}")
    public String etProxyPort;
    @Value("${et.proxy.ssl.port}")
    public String etProxySSLPort;
    @Value("${et.proxy.internal.port}")
    public String etProxyInternalPort;
    @Value("${et.proxy.internal.ssl.port}")
    public String etProxyInternalSSLPort;
    @Value("${et.proxy.host}")
    public String etProxyHost;

    @Value("${elastest.docker.network}")
    private String elastestNetwork;

    @Value("${et.etm.testlink.host}")
    public String etEtmTestLinkHost;
    @Value("${et.edm.elasticsearch.api}")
    public String etEdmElasticsearchApi;
    @Value("${et.edm.elasticsearch.path.with-proxy}")
    public String etEtmElasticsearchPathWithProxy;

    @Value("${et.etm.rabbit.path.with-proxy}")
    public String etEtmRabbitPathWithProxy;

    @Value("${exec.mode}")
    String execMode;

    // Logstash
    @Value("${et.etm.lsbeats.host}")
    public String etEtmLsBeatsHost;
    @Value("${et.etm.lsbeats.port}")
    public String etEtmLsBeatsPort;
    @Value("${et.etm.internal.lsbeats.port}")
    public String etEtmInternalLsbeatsPort;
    @Value("${et.etm.lshttp.api}")
    public String etEtmLsHttpApi;
    @Value("${et.etm.lshttp.port}")
    public String etEtmLsHttpPort;
    @Value("${et.etm.lstcp.host}")
    public String etEtmLsTcpHost;
    @Value("${et.etm.lstcp.port}")
    public String etEtmLsTcpPort;
    @Value("${et.etm.logstash.path.with-proxy}")
    public String etEtmLogstashPathWithProxy;

    @Value("${et.etm.binded.lsbeats.host}")
    public String etEtmBindedLsBeatsHost;
    @Value("${et.etm.binded.lsbeats.port}")
    public String etEtmBindedLsBeatsPort;
    @Value("${et.etm.binded.internal.lsbeats.port}")
    public String etEtmBindedInternalLsbeatsPort;

    @Value("${et.etm.binded.lstcp.host}")
    public String etEtmBindedLsTcpHost;
    @Value("${et.etm.binded.lstcp.port}")
    public String etEtmBindedLsTcpPort;

    @Value("${et.etm.logstash.container.name}")
    private String etEtmLogstashContainerName;

    @Value("${et.emp.grafana.context-path}")
    private String etEmpGrafanContextPath;
    @Value("${et.emp.grafana.dashboard}")
    private String etEmpGrafanaDashboard;
    @Value("${et.edm.command.context-path}")
    private String etEdmCommandContextPath;

    private DockerService2 dockerService;

    public EtmContextAuxService(DockerService2 dockerService) {
        this.dockerService = dockerService;
    }

    public ContextInfo getContextInfo() {
        ContextInfo contextInfo = new ContextInfo();

        // Logstash
        contextInfo.setLogstashPath(etEtmLogstashPathWithProxy);

        contextInfo.setLogstashTcpHost(etEtmLsTcpHost);
        contextInfo.setLogstashTcpPort(etEtmLsTcpPort);
        contextInfo.setLogstashBeatsHost(etEtmLsBeatsHost);
        contextInfo.setLogstashBeatsPort(etEtmLsBeatsPort);
        contextInfo.setInternalLogstashBeatsPort(etEtmInternalLsbeatsPort);
        contextInfo.setLogstashHttpPort(etEtmLsHttpPort);

        contextInfo.setLogstashBindedTcpHost(etEtmBindedLsTcpHost);
        contextInfo.setLogstashBindedTcpPort(etEtmBindedLsTcpPort);
        contextInfo.setLogstashBindedBeatsHost(etEtmBindedLsBeatsHost);
        contextInfo.setLogstashBindedBeatsPort(etEtmBindedLsBeatsPort);
        contextInfo.setLogstashBindedInternalBeatsPort(
                etEtmBindedInternalLsbeatsPort);

        String logstashHost = dockerService.getContainerIpByNetwork(
                etEtmLogstashContainerName, elastestNetwork);
        String proxyIp = etPublicHost;
        String proxyPort = etProxyPort;
        String proxySslPort = etProxySSLPort;
        if (etInProd) {
            if ("localhost".equals(etPublicHost)) {
                try {
                    proxyIp = UtilTools.doPing(etProxyHost);
                    proxyPort = etProxyInternalPort;
                    proxySslPort = etProxyInternalSSLPort;
                } catch (Exception e) {
                }
            }
        } else {
            proxyIp = dockerService.getHostIpByNetwork(elastestNetwork);
        }

        contextInfo.setLogstashHttpUrl("http://" + proxyIp + ":" + proxyPort
                + etEtmLogstashPathWithProxy);
        contextInfo.setLogstashSSLHttpUrl("https://" + proxyIp + ":"
                + proxySslPort + etEtmLogstashPathWithProxy);

        contextInfo.setLogstashIp(logstashHost);

        contextInfo.setElasticsearchPath(etEtmElasticsearchPathWithProxy);
        contextInfo.setElasticSearchUrl("http://" + proxyIp + ":" + proxyPort
                + etEtmElasticsearchPathWithProxy);

        contextInfo.setRabbitPath(etInProd ? etEtmRabbitPathWithProxy : "");
        contextInfo.setElasTestExecMode(execMode);
        contextInfo.setTestLinkStarted(
                !etEtmTestLinkHost.equals("none") ? true : false);

        contextInfo.setEdmCommandUrl("http://" + proxyIp + ":" + proxyPort + "/"
                + etEdmCommandContextPath);
        contextInfo.setEmpGrafanaUrl("http://" + proxyIp + ":" + proxyPort + "/"
                + etEmpGrafanContextPath + etEmpGrafanaDashboard);
        return contextInfo;
    }

    public Map<String, String> getMonitoringEnvVars() {
        return this.getMonitoringEnvVars(false);
    }

    public Map<String, String> getMonitoringEnvVars(boolean isTss) {
        Map<String, String> monEnvs = new HashMap<String, String>();

        ContextInfo context = this.getContextInfo();
        monEnvs.put("ET_MON_LSHTTP_API", context.getLogstashHttpUrl());
        monEnvs.put("ET_MON_LSHTTPS_API", context.getLogstashSSLHttpUrl());
        monEnvs.put("ET_MON_LSBEATS_PORT", context.getLogstashBeatsPort());
        monEnvs.put("ET_MON_INTERNAL_LSBEATS_PORT",
                context.getInternalLogstashBeatsPort());
        monEnvs.put("ET_MON_LSTCP_PORT", context.getLogstashTcpPort());

        if (!isTss) {
            monEnvs.put("ET_MON_LSBEATS_HOST", context.getLogstashBeatsHost());
            monEnvs.put("ET_MON_LSTCP_HOST", context.getLogstashTcpHost());
        } else {
            monEnvs.put("ET_MON_LSBEATS_HOST", context.getLogstashIp());
            monEnvs.put("ET_MON_LSTCP_HOST", context.getLogstashIp());
        }

        return monEnvs;
    }

}
