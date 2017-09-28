/*
 * (C) Copyright 2017-2019 ElasTest (http://elastest.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.elastest.epm.client.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service implementation for JSON utilities.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */
@Service
public class JsonService {

    final Logger log = getLogger(lookup().lookupClass());

    public String objectToJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    public <T> T jsonToObject(String json, Class<T> valueType)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, valueType);
    }

    public String sanitizeMessage(String message) {
        return message != null
                ? message.trim().replaceAll(" +", " ").replaceAll("\\r", "")
                        .replaceAll("\\n", "").replaceAll("\\t", "")
                : message;
    }

    public boolean isJsonValid(String json) {
        try {
            jsonToObject(json, Object.class);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
