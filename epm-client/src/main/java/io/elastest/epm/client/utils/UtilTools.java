package io.elastest.epm.client.utils;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

}
