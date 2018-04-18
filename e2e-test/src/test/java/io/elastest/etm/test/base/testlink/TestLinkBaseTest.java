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
package io.elastest.etm.test.base.testlink;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import io.elastest.etm.test.base.EtmBaseTest;

public class TestLinkBaseTest extends EtmBaseTest {

    final Logger log = getLogger(lookup().lookupClass());

    protected String tlApiPath = "/testlink";

    protected void navigateToTestlinkPage(WebDriver driver) {
        this.navigateToTorm(driver);
        log.info("Navigate to TestLink Section");

        driver.findElement(By.xpath("//a[@id='nav_testlink']")).click();
    }

    /* *************** */
    /* *** Project *** */
    /* *************** */

    protected boolean tlTestProjectExists(WebDriver driver,
            String projectName) {

        ResponseEntity<String> response = this.restClient
                .get(tlApiPath + "/project/name/" + projectName);

        return response.getBody() != null;
    }

    protected boolean createTlTestProject(WebDriver driver, TestProject project)
            throws JsonProcessingException {
        if (this.tlTestProjectExists(driver, project.getName())) {
            return true;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            String jsonPj = mapper.writeValueAsString(project);

            ResponseEntity<String> response = this.restClient
                    .post(tlApiPath + "/project", jsonPj);

            return response.getBody() != null;
        }
    }

    /* ************* */
    /* *** Suite *** */
    /* ************* */

    protected boolean tlTestSuiteExists(WebDriver driver, String suiteName) {

        ResponseEntity<String> response = this.restClient
                .get(tlApiPath + "/testlink/project/suite/name/" + suiteName);

        return response.getBody() != null;
    }

    /* **************** */
    /* *** TestCase *** */
    /* **************** */

    protected boolean tlTestCaseExists(WebDriver driver, String caseName) {
        ResponseEntity<String> response = this.restClient.get(
                tlApiPath + "/testlink/project/suite/case/name/" + caseName);

        return response.getBody() != null;
    }

    /* **************** */
    /* *** TestPlan *** */
    /* **************** */

    protected boolean tlTestPlanExists(WebDriver driver, String planName) {

        ResponseEntity<String> response = this.restClient
                .get(tlApiPath + "/project/plan/name/" + planName);

        return response.getBody() != null;
    }

    /* ************* */
    /* *** Build *** */
    /* ************* */

    protected boolean tlBuildExists(WebDriver driver, String buildName) {

        ResponseEntity<String> response = this.restClient.get(
                tlApiPath + "/testlink/project/plan/build/name/" + buildName);

        return response.getBody() != null;
    }

}
