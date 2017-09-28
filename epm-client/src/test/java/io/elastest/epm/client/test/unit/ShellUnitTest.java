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
package io.elastest.epm.client.test.unit;

import static java.lang.System.getProperty;
import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.slf4j.Logger;

import io.elastest.epm.client.service.ShellService;
import io.elastest.epm.client.test.util.MockitoExtension;

/**
 * Tests for shell service.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(PER_CLASS)
@Tag("unit")
@DisplayName("Unit tests for shell Service")
public class ShellUnitTest {

    final Logger log = getLogger(lookup().lookupClass());

    @InjectMocks
    ShellService shellService;

    @Test
    @DisplayName("List current folder")
    void testShellWithDir() throws IOException {
        String dirOutput = shellService.runAndWait("dir");
        log.debug("The list of the current folder is: {}", dirOutput);
        assertThat(dirOutput, not(isEmptyString()));
    }

    @Test
    @DisplayName("Is running in container")
    void testIsRunningInContainer() {
        boolean runningInJenkins = isEmpty(getProperty("jenkins"));
        boolean runningInContainer = shellService.isRunningInContainer();
        assertThat(runningInContainer, not(equalTo(runningInJenkins)));
    }

}
