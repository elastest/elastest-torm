package io.elastest.etm.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UtilsService {
    @Value("${exec.mode}")
    String execMode;

    @Value("${enable.et.mini}")
    public boolean enableETMini;

    @Value("${et.etm.in.dev}")
    public boolean etmInDev;

    public boolean isElastestMini() {
        return enableETMini && execMode.equals(ElastestConstants.MODE_NORMAL);
    }

    public boolean isEtmInDevelopment() {
        return etmInDev;
    }
}
