package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

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

import io.elastest.etm.utils.UtilsService;

// docker run --name sut_3718_fullteaching --rm  -itd --log-driver=syslog --log-opt syslog-address=tcp://localhost:5000 --log-opt tag=sut_3718_fullteaching_exec elastest/test-etm-alpinegitjava sh -c "while true; do echo "aaaaa"; sleep 2; done"
@Service
public class TcpServerService {
    public final Logger log = getLogger(lookup().lookupClass());

    @Value("${et.etm.lstcp.port}")
    public String lsTcpPort;

    private SyslogServerIF server;
    private int tcpPort;
    private TracesService tracesService;
    public UtilsService utilsService;

    public TcpServerService(TracesService tracesService, UtilsService utilsService) {
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
//                    log.debug("The TCP message: {}", event.getMessage());
                    tracesService.processTcpTrace(event.getMessage(),
                            event.getDate());
                }
            };
            serverConfig.addEventHandler(handler);

            server = SyslogServer.createThreadedInstance("tcp_session",
                    serverConfig);
            log.info("Listen at {} TCP Port", tcpPort);
        }
    }

    @PreDestroy
    void stopServer() throws InterruptedException {
        if (utilsService.isElastestMini()) {
            log.info("Starting shuting down TCP server");
            if (server.getThread().isAlive()) {
                try {
                    log.info("Interrupting TCP server thread");
                    server.getThread().interrupt();
                } catch (Exception e) {
                    log.error("Error on Interrupting TCP server thread");
                }
            }
            log.info("Shuting down TCP server");
            server.shutdown();
            SyslogServer.shutdown();
        }
    }

}
