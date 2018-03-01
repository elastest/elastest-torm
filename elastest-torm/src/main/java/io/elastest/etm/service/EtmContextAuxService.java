package io.elastest.etm.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.model.ContextInfo;

@Service
public class EtmContextAuxService {
    @Value("${et.public.host}")
    public String etPublicHost;
    @Value("${et.in.prod}")
    public boolean etInProd;

    @Value("${et.etm.testlink.host}")
    public String etEtmTestLinkHost;
    @Value("${et.edm.elasticsearch.api}")
    public String etEdmElasticsearchApi;
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

    public EtmContextAuxService() {
    }

    public ContextInfo getContextInfo() {
        ContextInfo contextInfo = new ContextInfo();
        contextInfo.setElasticSearchUrl(
                etInProd ? "http://" + etPublicHost + ":37000/elasticsearch"
                        : etEdmElasticsearchApi);
        // Logstash
        contextInfo.setLogstashHttpUrl(
                etInProd ? "http://" + etPublicHost + ":37000/logstash"
                        : etEtmLsHttpApi);
        contextInfo.setLogstashTcpHost(etEtmLsTcpHost);
        contextInfo.setLogstashTcpPort(etEtmLsTcpPort);
        contextInfo.setLogstashBeatsHost(etEtmLsBeatsHost);
        contextInfo.setLogstashBeatsPort(etEtmLsBeatsPort);
        contextInfo.setLogstashHttpPort(etEtmLsHttpPort);
        contextInfo.setLogstashIp(etInProd ? etPublicHost : etPublicHost);

        contextInfo.setRabbitPath(etInProd ? etEtmRabbitPathWithProxy : "");
        contextInfo.setElasTestExecMode(execMode);
        contextInfo.setTestLinkStarted(
                !etEtmTestLinkHost.equals("none") ? true : false);
        return contextInfo;
    }

    public Map<String, String> getMonitoringEnvVars() {
        Map<String, String> monEnvs = new HashMap<String, String>();

        ContextInfo context = this.getContextInfo();

        monEnvs.put("ET_MON_LSHTTP_API", context.getLogstashHttpUrl());
        monEnvs.put("ET_MON_LSBEATS_HOST", context.getLogstashBeatsHost());
        monEnvs.put("ET_MON_LSBEATS_PORT", context.getLogstashBeatsPort());
        monEnvs.put("ET_MON_LSTCP_HOST", context.getLogstashTcpHost());
        monEnvs.put("ET_MON_LSTCP_PORT", context.getLogstashTcpPort());

        return monEnvs;
    }

}
