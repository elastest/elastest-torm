package io.elastest.epm.client.utils;

import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spotify.docker.client.exceptions.DockerException;

public class UtilTools {

    private static final Logger logger = LoggerFactory
            .getLogger(UtilTools.class);

    // Obj to Json

    public static String convertJsonString(Object obj,
            Class<?> serializationView, JsonInclude.Include inclusion) {
        String jsonString = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(inclusion);

            jsonString = objectMapper.writerWithView(serializationView)
                    .writeValueAsString(obj);

        } catch (IOException e) {
            logger.error("Error during conversion: " + e.getMessage());
        }
        return jsonString;
    }

    public static String convertJsonString(Object obj,
            Class<?> serializationView) {
        return convertJsonString(obj, serializationView, Include.ALWAYS);
    }

    // Map to Obj

    @SuppressWarnings("unchecked")
    public static <T> T convertMapToObj(Map<Object, Object> map,
            Class<?> serializationView, JsonInclude.Include inclusion,
            boolean failOnUnknownProperties) {
        T object = null;
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(inclusion);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);

        object = (T) mapper.convertValue(map, serializationView);
        return object;
    }

    public static <T> T convertMapToObj(Map<Object, Object> map,
            Class<?> serializationView) {
        return convertMapToObj(map, serializationView, Include.ALWAYS, true);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertStringKeyMapToObj(Map<String, Object> map,
            Class<?> serializationView, JsonInclude.Include inclusion,
            boolean failOnUnknownProperties) {
        T object = null;
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(inclusion);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);

        object = (T) mapper.convertValue(map, serializationView);
        return object;
    }

    public static <T> T convertStringKeyMapToObj(Map<String, Object> map,
            Class<?> serializationView) {
        return convertStringKeyMapToObj(map, serializationView, Include.ALWAYS,
                true);
    }

    // Json to Obj
    @SuppressWarnings("unchecked")
    public static <T> T convertJsonStringToObj(String json,
            Class<?> serializationView, JsonInclude.Include inclusion,
            boolean failOnUnknownProperties) {
        T object = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(inclusion);
            objectMapper.configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            object = (T) objectMapper.readValue(json, serializationView);
        } catch (IOException e) {
            logger.error("Error during conversion: " + e.getMessage());
        }
        return object;
    }

    public static <T> T convertJsonStringToObj(String json,
            Class<?> serializationView) {
        return convertJsonStringToObj(json, serializationView, Include.ALWAYS,
                true);
    }

    public static <T> T convertJsonStringToObj(String json,
            Class<?> serializationView, JsonInclude.Include inclusion) {
        return convertJsonStringToObj(json, serializationView, inclusion, true);
    }

    public static <T> T convertJsonStringToObj(String json,
            Class<?> serializationView, boolean failOnUnknownProperties) {
        return convertJsonStringToObj(json, serializationView, Include.ALWAYS,
                failOnUnknownProperties);
    }

    // JSON to Obj by TypeRef

    @SuppressWarnings("unchecked")
    public static <T, K> T convertJsonStringToObjByTypeReference(String json,
            TypeReference<K> serializationView, JsonInclude.Include inclusion,
            boolean failOnUnknownProperties) {
        T object = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(inclusion);
            objectMapper.configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            object = (T) objectMapper.readValue(json, serializationView);
        } catch (IOException e) {
            logger.error("Error during conversion: " + e.getMessage());
        }
        return object;
    }

    public static <T, K> T convertJsonStringToObjByTypeReference(String json,
            TypeReference<K> serializationView) {
        return convertJsonStringToObjByTypeReference(json, serializationView,
                Include.ALWAYS, true);
    }

    // Obj to JsonNode
    public static JsonNode convertObjToJsonNode(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(obj);
    }

    // Obj to ObjectNode

    public static ObjectNode convertObjToObjectNode(Object object) {
        return UtilTools.convertObjToJsonNode(object).deepCopy();
    }

    public static void sleep(Integer timeout) {
        try {
            Thread.sleep(timeout * 1000);
        } catch (InterruptedException e) {
            logger.warn("Thread waiting interrupted: {}", e.getMessage());
        }
    }

    private static void waitUrl(String url, long timeoutMillis,
            long endTimeMillis, String errorMessage, int pollTimeMs)
            throws IOException, InterruptedException, DockerException {
        int responseCode = 0;
        while (true) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url)
                        .openConnection();
                connection.setConnectTimeout((int) timeoutMillis);
                connection.setReadTimeout((int) timeoutMillis);
                connection.setRequestMethod("GET");
                responseCode = connection.getResponseCode();

                if (responseCode == HTTP_OK) {
                    logger.debug("URL {} already reachable", url);
                    break;
                } else {
                    logger.trace(
                            "URL {} not reachable (response {}). Trying again in {} ms",
                            url, responseCode, pollTimeMs);
                }

            } catch (SSLHandshakeException | SocketException e) {
                logger.trace("Error {} waiting URL {}, trying again in {} ms",
                        e.getMessage(), url, pollTimeMs);

            } finally {
                // Polling to wait a consistent state
                try {
                    Thread.sleep(pollTimeMs);
                } catch (InterruptedException e) {
                    logger.warn("Thread waiting interrupted: {}",
                            e.getMessage());
                }
            }

            if (currentTimeMillis() > endTimeMillis) {
                throw new DockerException(errorMessage);
            }
        }
    }

    public static void waitForHostIsReachable(String url, int waitTimeoutSec)
            throws DockerException {
        long timeoutMillis = MILLISECONDS.convert(waitTimeoutSec, SECONDS);
        long endTimeMillis = System.currentTimeMillis() + timeoutMillis;

        logger.debug("Waiting for {} to be reachable (timeout {} seconds)", url,
                waitTimeoutSec);
        String errorMessage = "URL " + url + " not reachable in "
                + waitTimeoutSec + " seconds";
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[] {};
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs,
                            String authType) {
                        // No actions required
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs,
                            String authType) {
                        // No actions required
                    }
                } };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            waitUrl(url, timeoutMillis, endTimeMillis, errorMessage, 200);

        } catch (Exception e) {
            // Not propagating multiple exceptions (NoSuchAlgorithmException,
            // KeyManagementException, IOException, InterruptedException) to
            // improve readability
            throw new DockerException(errorMessage, e);
        }

    }
}
