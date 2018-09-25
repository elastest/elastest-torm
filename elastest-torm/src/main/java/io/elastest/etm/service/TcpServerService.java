package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
import org.productivity.java.syslog4j.server.SyslogServerEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.productivity.java.syslog4j.server.impl.net.tcp.TCPNetSyslogServerConfig;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Charsets;

import io.elastest.etm.utils.UtilsService;

// docker run --name sut_3718_fullteaching --rm  -itd --log-driver=syslog --log-opt syslog-address=tcp://localhost:5000 --log-opt tag=sut_3718_fullteaching_exec elastest/test-etm-alpinegitjava sh -c "while true; do echo "aaaaa"; sleep 2; done"
@Service
public class TcpServerService {
    public final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.etm.lstcp.port}")
    public String lsTcpPort;

    private Map<String, Boolean> executionWithDifferentTimezone = new HashMap<>();

    private SyslogServerIF server;
    private int tcpPort;
    private TracesService tracesService;
    public UtilsService utilsService;

    public TcpServerService(TracesService tracesService,
            UtilsService utilsService) {
        this.tracesService = tracesService;
        this.utilsService = utilsService;
    }

    @PostConstruct
    void init() throws InterruptedException {
        if (utilsService.isElastestMini()) {
            tcpPort = Integer.parseInt(lsTcpPort);
            SyslogServerConfigIF serverConfig = new TCPNetSyslogServerConfig(
                    tcpPort);

            SyslogServerEventHandlerIF handler = new SyslogServerEventHandlerIF() {
                private static final long serialVersionUID = 1L;

                @Override
                public void event(SyslogServerIF syslogServer,
                        SyslogServerEventIF event) {
                    event = processDateIfIsNecessary(event);
                    tracesService.processTcpTrace(event.getMessage(),
                            event.getDate());
                }
            };
            serverConfig.addEventHandler(handler);
            server = SyslogServer.createThreadedInstance("tcp_session",
                    serverConfig);
            logger.info("Listen at {} TCP Port", tcpPort);
        }
    }

    private SyslogServerEventIF processDateIfIsNecessary(
            SyslogServerEventIF event) {
        if (event.getDate() != null) {
            String containerName = tracesService
                    .getContainerNameFromMessage(event.getMessage());

            // Checks first trace only, if is different timezone, checks all
            // Checks always too if containerName does not exists
            if (!executionWithDifferentTimezone.containsKey(containerName)
                    || (executionWithDifferentTimezone
                            .containsKey(containerName)
                            && executionWithDifferentTimezone
                                    .get(containerName))) {
                Date beforeDate = event.getDate();
                Date afterDate = utilsService
                        .getLocaltimeDateFromLiveDate(event.getDate());
                event.setDate(afterDate);

                Boolean differentTimezone = false;
                if (beforeDate != null) {
                    int comparission = afterDate.compareTo(beforeDate);
                    differentTimezone = comparission != 0;
                }

                if (containerName != null && beforeDate != null) {
                    executionWithDifferentTimezone.put(containerName,
                            differentTimezone);
                }
            }
        } else {
            event.setDate(new Date());
        }
        return event;

    }

    @PreDestroy
    void stopServer() throws InterruptedException {
        if (utilsService.isElastestMini()) {
            logger.info("Starting shuting down TCP server");
            if (server.getThread().isAlive()) {
                try {
                    logger.info("Interrupting TCP server thread");
                    server.getThread().interrupt();
                } catch (Exception e) {
                    logger.error("Error on Interrupting TCP server thread");
                }
            }
            logger.info("Shuting down TCP server");
            server.shutdown();
            SyslogServer.shutdown();
        }
    }

}
