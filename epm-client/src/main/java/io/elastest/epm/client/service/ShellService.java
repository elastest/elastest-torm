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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.google.common.io.CharStreams;

/**
 * Utilities to execute commands on the shell.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.0.1
 */
@Service
public class ShellService {

    final Logger log = getLogger(lookup().lookupClass());

    public String runAndWait(String... command) throws IOException {
        return runAndWaitArray(command);
    }

    public String runAndWaitArray(String[] command) throws IOException {
        assert (command.length > 0);

        String commandStr = Arrays.toString(command);
        log.trace("Running command on the shell: {}", commandStr);
        String result = runAndWaitNoLog(command);
        log.trace("Result: {}", result);
        return result;
    }

    public String runAndWaitNoLog(String... command) throws IOException {
        assert (command.length > 0);

        Process process = new ProcessBuilder(command).redirectErrorStream(true)
                .start();
        String output = CharStreams.toString(
                new InputStreamReader(process.getInputStream(), UTF_8));
        process.destroy();
        return output;
    }

    public boolean isRunningInContainer() {
        boolean isRunningInContainer = false;
        try (BufferedReader br = Files
                .newBufferedReader(Paths.get("/proc/1/cgroup"), UTF_8)) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("/docker")) {
                    return true;
                }
                isRunningInContainer = false;
            }

        } catch (IOException e) {
            log.trace("Not running inside a Docker container");
        }
        return isRunningInContainer;
    }

}
