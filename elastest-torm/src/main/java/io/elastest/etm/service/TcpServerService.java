package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.SocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.productivity.java.syslog4j.server.SyslogRFC5424ServerSessionEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.productivity.java.syslog4j.server.impl.event.SyslogRFC5424ServerEvent;
import org.productivity.java.syslog4j.server.impl.net.tcp.TCPNetSyslogRFC5424Server;
import org.productivity.java.syslog4j.server.impl.net.tcp.TCPNetSyslogRFC5424ServerConfig;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.utils.UtilsService;

// docker run --name sut_3718_fullteaching --rm  -itd --log-driver=syslog --log-opt syslog-address=tcp://localhost:5000 --log-opt tag=sut_3718_fullteaching_exec elastest/test-etm-alpinegitjava sh -c "while true; do echo "aaaaa"; sleep 2; done"
@Service
public class TcpServerService {
    public final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.etm.lstcp.port}")
    public String lsTcpPort;

    private Map<String, Long> cachedTimeDifference = new HashMap<>();

    private TCPNetSyslogRFC5424Server server;
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
            TCPNetSyslogRFC5424ServerConfig serverConfig = new TCPNetSyslogRFC5424ServerConfig(
                    tcpPort);

            SyslogRFC5424ServerSessionEventHandlerIF handler = new SyslogRFC5424ServerSessionEventHandlerIF() {
                private static final long serialVersionUID = 1L;

                @Override
                public void event(Object session, SyslogServerIF syslogServer,
                        SocketAddress socketAddress,
                        SyslogRFC5424ServerEvent event) {
                    // Process
                    try {
                        String finalLogMsg = event.getMsgid() + " - "
                                + event.getMessage();

                        try {
                            Date timestamp = event.getDate();

                            // Cache time difference by hostname
                            String key = event.getHostName();
                            // If is empty or null, use containerName
                            if (key == null || "".equals(key)) {
                                String containerName = tracesService
                                        .getContainerNameFromMessage(
                                                finalLogMsg);
                                key = containerName;
                            }

                            logger.trace("Received Date {}", timestamp);
                            timestamp = processDateIfIsNecessary(timestamp,
                                    finalLogMsg, key);
                            logger.trace("Modified Date {}", timestamp);

                            // Process trace
                            tracesService.processTcpTrace(finalLogMsg,
                                    timestamp);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            logger.error("Error on parse timestamp {}:",
                                    event.getDate(), e);
                        }
                    } catch (Exception e) {
                        // RAW: empty raw;
                    }

                }

                @Override
                public Object sessionOpened(SyslogServerIF syslogServer,
                        SocketAddress socketAddress) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void exception(Object session,
                        SyslogServerIF syslogServer,
                        SocketAddress socketAddress, Exception exception) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void sessionClosed(Object session,
                        SyslogServerIF syslogServer,
                        SocketAddress socketAddress, boolean timeout) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void initialize(SyslogServerIF syslogServer) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void destroy(SyslogServerIF syslogServer) {
                    // TODO Auto-generated method stub

                }

            };

            serverConfig.addEventHandler(handler);
            server = (TCPNetSyslogRFC5424Server) SyslogServer
                    .createThreadedInstance("tcp_session", serverConfig);
            logger.info("Listen at {} TCP Port", tcpPort);
        }
    }

    private Date processDateIfIsNecessary(Date date, String message,
            String key) {
        if (date != null) {
            // Checks first trace only, if is different timezone, checks all
            // Checks always too if containerName does not exists
            if (!cachedTimeDifference.containsKey(key)
                    || (cachedTimeDifference.containsKey(key)
                            && cachedTimeDifference.get(key) != 0)) {
                Date beforeDate = new Date(date.getTime());

                // Get difference and convert if its necessary

                long difference;
                // If cached, use it
                if (cachedTimeDifference.containsKey(key)
                        && cachedTimeDifference.get(key) != 0) {
                    difference = cachedTimeDifference.get(key);
                } else { // else calculate
                    difference = utilsService
                            .getLocaltimeDifferenceFromLiveDate(date);
                }

                date = utilsService.getLocaltimeDateFromLiveDate(date,
                        difference);

                Date afterDate = new Date(date.getTime());

                if (key != null && beforeDate != null) {
                    cachedTimeDifference.put(key, difference);
                }
            }
            return date;
        } else {
            return new Date();
        }

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
