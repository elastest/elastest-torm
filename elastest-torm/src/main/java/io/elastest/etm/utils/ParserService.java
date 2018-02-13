package io.elastest.etm.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ParserService {
    public static final Logger logger = LoggerFactory
            .getLogger(ParserService.class);
    static ObjectMapper mapper = new ObjectMapper();

    public static ObjectNode fromStringToJson(String jsonString)
            throws IOException {
        try {
            return mapper.readValue(jsonString, ObjectNode.class);
        } catch (IOException ioe) {
            logger.error("Error parsing a json string: {}", jsonString);
            throw ioe;
        }
    }

    public static JsonNode getNodeByElemChain(ObjectNode jsonNode,
            List<String> nodeNames) {
        if (nodeNames.size() > 1) {
            return getNodeByElemChain(
                    (ObjectNode) jsonNode.get(nodeNames.get(0)),
                    nodeNames.subList(1, nodeNames.size()));
        } else if (nodeNames.size() == 1) {
            return jsonNode.get(nodeNames.get(0));
        } else {
            return null;
        }

    }

}
