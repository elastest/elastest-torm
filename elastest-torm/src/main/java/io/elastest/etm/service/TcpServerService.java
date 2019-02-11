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

import io.elastest.etm.beats.Message;
import io.elastest.etm.utils.UtilsService;
import io.netty.buffer.Unpooled;

// docker run --name sut_3718_fullteaching --rm  -itd --log-driver=syslog --log-opt syslog-address=tcp://localhost:5000 --log-opt tag=sut_3718_fullteaching_exec elastest/test-etm-alpinegitjava sh -c "while true; do echo "aaaaa"; sleep 2; done"
@Service
public class TcpServerService {
    public final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.etm.lstcp.port}")
    public String lsTcpPort;

    // For EMS TCP beat metrics
    @Value("${et.etm.internal.lstcp.port}")
    public String internalLsTcpPort;

    private Map<String, Long> cachedTimeDifference = new HashMap<>();

    private TCPNetSyslogRFC5424Server tcpServer;
    private int tcpPort;

    private TCPNetSyslogRFC5424Server tcpServerForEMSBeats;
    private int tcpPortForEMSBeats;

    private TracesService tracesService;
    public UtilsService utilsService;

    SyslogRFC5424ServerSessionEventHandlerIF tcpLogsHandler = new SyslogRFC5424ServerSessionEventHandlerIF() {
        private static final long serialVersionUID = 1L;

        @Override
        public void event(Object session, SyslogServerIF syslogServer,
                SocketAddress socketAddress, SyslogRFC5424ServerEvent event) {
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
                                .getContainerNameFromMessage(finalLogMsg);
                        key = containerName;
                    }

                    logger.trace("Received Date {}", timestamp);
                    timestamp = processDateIfIsNecessary(timestamp, finalLogMsg,
                            key);
                    logger.trace("Modified Date {}", timestamp);

                    // Process trace
                    tracesService.processTcpTrace(finalLogMsg, timestamp);
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
        public void exception(Object session, SyslogServerIF syslogServer,
                SocketAddress socketAddress, Exception exception) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sessionClosed(Object session, SyslogServerIF syslogServer,
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

    SyslogRFC5424ServerSessionEventHandlerIF tcpEMSBeatsHandler = new SyslogRFC5424ServerSessionEventHandlerIF() {
        private static final long serialVersionUID = 1L;

        @Override
        public void event(Object session, SyslogServerIF syslogServer,
                SocketAddress socketAddress, SyslogRFC5424ServerEvent event) {
            try {
                if (event != null && event.getRaw() != null) {
                    logger.trace("EMS tcp beats trace: {}",
                            event.getRawString());
                    Message msg = new Message(0,
                            Unpooled.wrappedBuffer(event.getRaw()));

                    tracesService.processBeatTrace(msg.getData(), true);
                }
            } catch (Exception e) {

            }
        }

        @Override
        public Object sessionOpened(SyslogServerIF syslogServer,
                SocketAddress socketAddress) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void exception(Object session, SyslogServerIF syslogServer,
                SocketAddress socketAddress, Exception exception) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sessionClosed(Object session, SyslogServerIF syslogServer,
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

    public TcpServerService(TracesService tracesService,
            UtilsService utilsService) {
        this.tracesService = tracesService;
        this.utilsService = utilsService;
    }

    @PostConstruct
    void init() throws InterruptedException {
        if (utilsService.isElastestMini()) {
            /* *** Normal TCP SERVER *** */
            tcpPort = Integer.parseInt(lsTcpPort);
            TCPNetSyslogRFC5424ServerConfig tcpServerConfig = new TCPNetSyslogRFC5424ServerConfig(
                    tcpPort);

            tcpServerConfig.addEventHandler(tcpLogsHandler);
            tcpServer = (TCPNetSyslogRFC5424Server) SyslogServer
                    .createThreadedInstance("tcp_session", tcpServerConfig);
            logger.info("Listen at {} TCP Port", tcpPort);

            /* *** EMS TCP SERVER*** */

            tcpPortForEMSBeats = Integer.parseInt(internalLsTcpPort);
            TCPNetSyslogRFC5424ServerConfig emsTcpServerConfig = new TCPNetSyslogRFC5424ServerConfig(
                    tcpPortForEMSBeats);

            emsTcpServerConfig.addEventHandler(tcpEMSBeatsHandler);
            tcpServerForEMSBeats = (TCPNetSyslogRFC5424Server) SyslogServer
                    .createThreadedInstance("ems_tcp_session",
                            emsTcpServerConfig);
            logger.info("Listen at {} TCP Port for EMS Beats",
                    tcpPortForEMSBeats);

        }
    }

    @PreDestroy
    void stopServer() throws InterruptedException {
        if (utilsService.isElastestMini()) {
            /* *** Normal TCP SERVER *** */
            logger.info("Starting shuting down TCP server");
            if (tcpServer.getThread().isAlive()) {
                try {
                    logger.info("Interrupting TCP server thread");
                    tcpServer.getThread().interrupt();
                } catch (Exception e) {
                    logger.error("Error on Interrupting TCP server thread");
                }
            }
            logger.info("Shuting down TCP server");
            tcpServer.shutdown();
            SyslogServer.shutdown();

            /* *** EMS TCP SERVER*** */
            logger.info("Starting shuting down EMS Beats TCP server");
            if (tcpServerForEMSBeats.getThread().isAlive()) {
                try {
                    logger.info("Interrupting EMS Beats TCP server thread");
                    tcpServerForEMSBeats.getThread().interrupt();
                } catch (Exception e) {
                    logger.error(
                            "Error on Interrupting EMS Beats TCP server thread");
                }
            }
            logger.info("Shuting down EMS Beats TCP server");
            tcpServerForEMSBeats.shutdown();
            SyslogServer.shutdown();
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

                if (key != null && beforeDate != null) {
                    cachedTimeDifference.put(key, difference);
                }
            }
            return date;
        } else {
            return new Date();
        }

    }
}
