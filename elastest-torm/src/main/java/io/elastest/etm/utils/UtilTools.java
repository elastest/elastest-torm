package io.elastest.etm.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.DockerClientException;

@Component
public class UtilTools {

    private static final Logger logger = LoggerFactory
            .getLogger(UtilTools.class);

    private static String hostIp;

    @Value("${os.name}")
    private static String windowsSO;

    @Value("${et.etm.incontainer}")
    private static String inContainer;

    /*
     * public boolean pingHost(String host, int port, int timeout) { try (Socket
     * socket = new Socket()) { socket.connect(new InetSocketAddress(host,
     * port), timeout); return true; } catch (IOException e) { return false; //
     * Either timeout or unreachable or failed DNS lookup. } }
     */

    public static String getElasTestHostOnWin() {
        return "localhost";
    }

    /**
     * Returns the docker-host's url on Windows
     * 
     * @return
     */
    public static String getDockerHostUrlOnWin() {
        BufferedReader reader = null;
        String dockerHostUrl = "";

        try {
            Process child = Runtime.getRuntime().exec("docker-machine url");
            reader = new BufferedReader(
                    new InputStreamReader(child.getInputStream()));
            dockerHostUrl = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return dockerHostUrl;
    }

    /**
     * Returns the docker-host's ip.
     * 
     * @return
     */
    public static String getDockerHostIpOnWin() {
        BufferedReader reader = null;
        String dockerHostIp = "";
        if (windowsSO != null && windowsSO.toLowerCase().contains("win")) {
            try {
                Process child = Runtime.getRuntime().exec("docker-machine ip");
                reader = new BufferedReader(
                        new InputStreamReader(child.getInputStream()));
                dockerHostIp = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return dockerHostIp;
    }

    public static String getHostIp() {
        if (hostIp == null) {
            if (inContainer != null && inContainer.equals("true")) {
                try {
                    String ipRoute = Shell.runAndWait("sh", "-c",
                            "/sbin/ip route");
                    String[] tokens = ipRoute.split("\\s");
                    hostIp = tokens[2];
                } catch (Exception e) {
                    throw new DockerClientException(
                            "Exception executing /sbin/ip route", e);
                }
            } else {
                hostIp = "127.0.0.1";
            }
        }
        logger.debug("Host IP is {}", hostIp);
        return hostIp;
    }

    public static String getDockerHostIp() {
        if (windowsSO != null && windowsSO.toLowerCase().contains("win")) {
            return getDockerHostIpOnWin();
        } else
            return getHostIp();
    }

    public static String getMyIp() {
        String myIp = "";
        try {
            InetAddress ip = InetAddress.getLocalHost();
            if (hostIp != null) {
                logger.debug("Current IP address : " + hostIp);
            }
            myIp = ip.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return myIp;
    }

    public static String convertJsonString(Object obj,
            Class<?> serializationView) {
        String jsonString = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonString = objectMapper.writerWithView(serializationView)
                    .writeValueAsString(obj);

        } catch (IOException e) {
            logger.error("Error during conversion: " + e.getMessage());
        }
        return jsonString;
    }

    /**
     * Generate an unique id.
     * 
     * @return the new unique id
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static int findRandomOpenPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    public static boolean checkIfUrlIsUp(String urlToCheck) {
        boolean up = false;
        URL url;
        try {
            url = new URL(urlToCheck);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int responseCode = huc.getResponseCode();
            up = (responseCode >= 200 && responseCode <= 299);
            if (!up) {
                return up;
            }
        } catch (IOException e) {
            return false;
        }

        return up;
    }

    public static String doPing(String ip) throws IOException {
        InetAddress ping;
        ping = InetAddress.getByName(ip);
        if (ping.isReachable(5000)) {
            return ping.getHostAddress().toString();
        } else {
            throw new IOException("Ip " + ip + " non reachable");
        }
    }

}
