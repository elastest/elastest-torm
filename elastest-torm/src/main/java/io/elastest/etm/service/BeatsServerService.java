package io.elastest.etm.service;

import static java.lang.Thread.sleep;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.beats.Message;
import io.elastest.etm.beats.MessageListener;
import io.elastest.etm.beats.Server;
import io.elastest.etm.utils.UtilsService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

// docker run --rm --name zxc -e LOGSTASHHOST=172.17.0.1 -e LOGSTASHPORT=5044 -v /var/run/docker.sock:/var/run/docker.sock elastest/etm-dockbeat
@Service
public class BeatsServerService {
    public final Logger log = getLogger(lookup().lookupClass());

    @Value("${et.etm.lsbeats.port}")
    public String lsBeatsPort;

    // Dockbeat
    @Value("${et.etm.internal.lsbeats.port}")
    public String lsInternalBeatsPort;

    private static EventLoopGroup group;
    private final String host = "0.0.0.0";
    private final int threadCount = 10;
    private Server server;
    private Server dockbeatServer;
    private int beatsPort;
    private int dockbeatPort;

    private TracesService tracesService;

    public UtilsService utilsService;

    public BeatsServerService(TracesService tracesService,
            UtilsService utilsService) {
        this.tracesService = tracesService;
        this.utilsService = utilsService;
    }

    @PostConstruct
    void init() throws InterruptedException {
        if (utilsService.isElastestMini()) {
            beatsPort = Integer.parseInt(lsBeatsPort);
            dockbeatPort = Integer.parseInt(lsInternalBeatsPort);

            group = new NioEventLoopGroup();
            this.startBeatsServer();
            this.startDockbeatServer();
        }
    }

    @PreDestroy
    void stopServer() throws InterruptedException {
        if (utilsService.isElastestMini()) {
            log.info("Shuting down Beats server");
            group.shutdownGracefully();
            server.stop();
            dockbeatServer.stop();
        }
    }

    public void startBeatsServer() throws InterruptedException {

        server = new Server(host, beatsPort, 30, threadCount);
        SpyListener listener = new SpyListener();
        server.setMessageListener(listener);
        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    server.listen();
                } catch (InterruptedException e) {
                }
            }
        };

        new Thread(serverTask).start();
        log.info("Listen at {} Beats Port", beatsPort);
        sleep(1000); // start server give is some time.

    }

    public void startDockbeatServer() throws InterruptedException {
        dockbeatServer = new Server(host, dockbeatPort, 30, threadCount);
        SpyListener listener = new SpyListener(true);
        dockbeatServer.setMessageListener(listener);
        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    dockbeatServer.listen();
                } catch (InterruptedException e) {
                }
            }
        };

        new Thread(serverTask).start();
        log.info("Listen at {} Dockbeat Beats Port", dockbeatPort);
        sleep(1000); // start server give is some time.
    }

    /**
     * Used to assert the number of messages send to the server
     */
    private class SpyListener extends MessageListener {
        boolean fromDockbeat = false;

        public SpyListener() {
        }

        public SpyListener(boolean fromDockbeat) {
            this.fromDockbeat = fromDockbeat;

        }

        public void onNewMessage(ChannelHandlerContext ctx, Message message) {
//            log.debug("The Beats message data: {}", message.getData());
            tracesService.processBeatTrace(message.getData(),
                    this.fromDockbeat);
        }
    }
}
