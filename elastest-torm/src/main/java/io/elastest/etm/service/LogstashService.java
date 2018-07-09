package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import io.elastest.etm.model.ContextInfo;
import io.elastest.etm.utils.UtilsService;

@Service
public class LogstashService {
    final Logger logger = getLogger(lookup().lookupClass());

    ContextInfo contextInfo;

    String lsHttpApi;

    String logDefaultStream = "default_log";
    String defaultTestComponent = "test";

    EtmContextAuxService etmContextAuxService;
    UtilsService utilsService;

    public LogstashService(EtmContextAuxService etmContextAuxService,
            UtilsService utilsService) {
        this.etmContextAuxService = etmContextAuxService;
        this.utilsService = utilsService;
    }

    @PostConstruct
    public void init() {
        this.contextInfo = this.etmContextAuxService.getContextInfo();
        this.lsHttpApi = this.contextInfo.getLogstashHttpUrl();
    }

    public void sendSingleLogTrace(String message, String component,
            String exec, String stream) {
        if (lsHttpApi == null) {
            return;
        }

        try {
            URL url = new URL(lsHttpApi);

            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            String body = "{" + "\"component\":\"" + component + "\""
                    + ",\"exec\":\"" + exec + "\"" + ",\"stream\":\"" + stream
                    + "\"" + ",\"message\":\"" + message + "\"" + "}";
            byte[] out = body.getBytes(UTF_8);
            logger.debug("Sending single log trace to logstash ({}): {}",
                    lsHttpApi, body);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type",
                    "application/json; charset=UTF-8");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
        } catch (

        Exception e) {
            logger.error("Exception in send single log trace", e);
        }
    }

    public void sendStartTestLogtrace(String exec, String testName) {
        String message = utilsService.tcStartMsgPrefix + " " + testName;
        this.sendSingleLogTrace(message, defaultTestComponent, exec,
                logDefaultStream);
    }

    public void sendFinishTestLogtrace(String exec, String testName) {
        String message = utilsService.tcFinishMsgPrefix + " " + testName;
        this.sendSingleLogTrace(message, defaultTestComponent, exec,
                logDefaultStream);
    }
}
