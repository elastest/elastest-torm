package io.elastest.etm.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.model.ContextInfo;
import io.elastest.etm.model.SupportServiceInstance;

@Service
public class EtmContextService {
    public static final String EUS_TSS_ID = "29216b91-497c-43b7-a5c4-6613f13fa0e9";

    @Autowired
    EsmService esmService;
    @Value("${et.edm.elasticsearch.api}")
    public String elasticsearchApi;
    @Value("${et.public.host}")
    public String publicHost;
    @Value("${et.in.prod}")
    public boolean etInProd;
    @Value("${et.etm.rabbit.path.with-proxy}")
    public String etEtmRabbitPathWithProxy;
    @Value("${exec.mode}")
    String execMode;

    public ContextInfo getContextInfo() {
        ContextInfo contextInfo = new ContextInfo();
        contextInfo.setElasticSearchUrl(
                etInProd ? "http://" + publicHost + ":37000/elasticsearch"
                        : elasticsearchApi);
        contextInfo.setRabbitPath(etInProd ? etEtmRabbitPathWithProxy : "");
        contextInfo.setElasTestExecMode(execMode);
        contextInfo.setEusSSInstance(getEusApiUrl());
        return contextInfo;
    }

    private SupportServiceInstance getEusApiUrl() {
        SupportServiceInstance eusInstance = null;
        for (Map.Entry<String, SupportServiceInstance> entry : esmService
                .getServicesInstances().entrySet()) {
            if (entry.getValue().getService_id().equals(EUS_TSS_ID)) {
                eusInstance = entry.getValue();
                break;
            }
        }

        return eusInstance;
    }

}
