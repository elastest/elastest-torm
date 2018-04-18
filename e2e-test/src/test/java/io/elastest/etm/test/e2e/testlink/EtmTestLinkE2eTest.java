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
package io.elastest.etm.test.e2e.testlink;

import static io.github.bonigarcia.BrowserType.CHROME;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import io.elastest.etm.test.base.testlink.EtmTestLinkBaseTest;
import io.elastest.etm.test.base.testlink.TestLinkBaseTest;
import io.github.bonigarcia.DockerBrowser;
import io.github.bonigarcia.SeleniumExtension;

/**
 * E2E ETM TestLink test.
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("ETM E2E test of TestLink")
@ExtendWith(SeleniumExtension.class)
public class EtmTestLinkE2eTest extends EtmTestLinkBaseTest {

    final Logger log = getLogger(lookup().lookupClass());

    protected TestLinkBaseTest tlBaseTest;

    @Test
    @DisplayName("Get TestLink Url")
    void getTLUrlTest(@DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException {
        this.driver = driver;
        String url = this.getTestlinkPageUrl(driver);
        log.info("The obtained TestLink url is {}", url);
    }

    @Test
    @DisplayName("Create TestLink Project")
    void createTLProjectTest(
            @DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException, IOException {
        this.driver = driver;
        TestProject project = new TestProject(0, "Test Sample Project", "TSP",
                "This is a note", false, false, false, false, true, true);
        project = this.createTlTestProject(driver, project);
        if (project != null) {
            log.info("Project {} has been created with id {}!",
                    project.getName(), project.getId());
        } else {
            log.error("Project hasn't been created");
        }
    }

}
