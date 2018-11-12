package io.elastest.etm.config;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestCaseStep;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.model.MultiConfig;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.service.ExternalService;

@Service
public class EtSampleDataLoader {
    final Logger logger = getLogger(lookup().lookupClass());

    private EtDataLoader etDataLoader;
    private ExternalService externalService;

    private static final String EXEC_DASHBOARD_CONFIG = "{\"showAllInOne\":false,\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":false}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_WITH_SUT = "{\"showAllInOne\":false,\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":true}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_FULLTEACHING = "{\"showAllInOne\":false,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"etType\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"etType\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"etType\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"etType\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"etType\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"etType\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":false},{\"component\":\"sut_full_teaching_mysql\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_full_teaching_mysql_default_log_log\",\"activated\":true},{\"component\":\"sut_full_teaching\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_full_teaching_default_log_log\",\"activated\":true},{\"component\":\"sut_full_teaching_openvidu_server_kms\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_full_teaching_openvidu_server_kms_default_log_log\",\"activated\":true}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_FOR_TESTLINK = "{\"showAllInOne\":false,\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":false},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":true}]}}";

    private static final String webAppSutName = "Webapp";
    private static final String webAppSutDesc = "SpringBoot app with a form";
    private static final String webAppSutImage = "elastest/demo-web-java-test-sut";
    private static final ProtocolEnum webAppSutProtocol = ProtocolEnum.HTTP;
    private static final String webAppSutPort = "8080";

    private static String javaMvnImage = "elastest/test-etm-alpinegitjava";
    private static String pythonImage = "elastest/test-etm-alpinegitpython";
    private static String nodeImage = "elastest/test-etm-alpinegitnode";
    private static String gaugeImage = "elastest/test-etm-alpinegitjavagauge";

    @Value("${et.config.folder}")
    private String configFolder;

    public EtSampleDataLoader(EtDataLoader etDataLoader,
            ExternalService externalService) {
        this.etDataLoader = etDataLoader;
        this.externalService = externalService;
    }

    @PostConstruct
    public void createData() {
        String sampleDataCreated = (configFolder.endsWith("/") ? configFolder
                : configFolder + "/") + "sampleDataCreated";
        File sampleDataCreatedFile = new File(sampleDataCreated);

        if (!sampleDataCreatedFile.exists()) {
            this.createUnitTests();
            this.createRestApi();
            this.createWebapp();
            this.createOpenVidu();
            this.createFullteaching();
            if (etDataLoader.isStartedTestLink()) {
                this.createTestLink();
            }

            try {
                logger.info("Sample Data has been created!");
                sampleDataCreatedFile.createNewFile();
            } catch (IOException e) {
                logger.error("File {} has not been created", sampleDataCreated);
            }
        }
    }

    public void printLog(String pjName) {
        logger.debug("Creating '{}' Sample Data...", pjName);
    }

    public void createUnitTests() {
        String pjName = "Unit Tests";
        if (!etDataLoader.projectExists(pjName)) {
            // Create Unit Tests Project
            this.printLog(pjName);
            Project project = etDataLoader.createProject(pjName);

            /* *** TJob 1 *** */
            String tJob1Name = "JUnit5 Unit Test";
            String resultsPath1 = "/demo-projects/unit-java-test/target/surefire-reports/";
            String commands1 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/unit-java-test;\nmvn -B test\n";

            etDataLoader.createTJob(project, tJob1Name, resultsPath1,
                    javaMvnImage, false, commands1, EXEC_DASHBOARD_CONFIG, null,
                    null, null, null);

            /* *** TJob 2 *** */
            String tJob2Name = "JUnit4 Unit Test";
            String resultsPath2 = "/demo-projects/unit-java-test-junit4/target/surefire-reports/";
            String commands2 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/unit-java-test-junit4;\nmvn -B test\n";

            etDataLoader.createTJob(project, tJob2Name, resultsPath2,
                    javaMvnImage, false, commands2, EXEC_DASHBOARD_CONFIG, null,
                    null, null, null);

            /* *** TJob 3 *** */
            String tJob3Name = "Python Unit Test";
            String resultsPath3 = "/demo-projects/python-unit-test/testresults";
            String commands3 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/python-unit-test;\npython UnitTest.py;";

            etDataLoader.createTJob(project, tJob3Name, resultsPath3,
                    pythonImage, false, commands3, EXEC_DASHBOARD_CONFIG, null,
                    null, null, null);

            /* *** TJob 4 *** */
            String tJob4Name = "Jasmine Unit Test";
            String resultsPath4 = "/demo-projects/jasmine-unit-test/testresults";
            String commands4 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/jasmine-unit-test;\njasmine;";

            etDataLoader.createTJob(project, tJob4Name, resultsPath4, nodeImage,
                    false, commands4, EXEC_DASHBOARD_CONFIG, null, null, null,
                    null);
        }
    }

    public void createRestApi() {
        String pjName = "REST API";
        if (!etDataLoader.projectExists(pjName)) {
            // Create Project
            this.printLog(pjName);
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            String sutName = "REST App";
            String sutDesc = "Simple SpringBoot app";
            String sutImage = "elastest/demo-rest-java-test-sut";
            ProtocolEnum sutProtocol = ProtocolEnum.HTTP;
            String sutPort = "8080";

            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithDockerImage(project, null,
                            sutName, sutDesc, sutImage, sutProtocol, sutPort,
                            null);

            /* *** TJob 1 *** */
            String tJob1Name = "JUnit5 Rest Test";
            String resultsPath1 = "/demo-projects/rest-java-test/target/surefire-reports/";
            String commands1 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/rest-java-test;\nmvn -B test;";

            etDataLoader.createTJob(project, tJob1Name, resultsPath1,
                    javaMvnImage, false, commands1,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null, sut, null);

            /* *** TJob 2 *** */
            String tJob2Name = "JUnit4 Rest Test";
            String resultsPath2 = "/demo-projects/rest-java-test-junit4/target/surefire-reports/";
            String commands2 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/rest-java-test-junit4;\nmvn -B test;";

            etDataLoader.createTJob(project, tJob2Name, resultsPath2,
                    javaMvnImage, false, commands2,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null, sut, null);

            /* *** TJob 3 *** */
            String tJob3Name = "Karate Rest Test";
            String resultsPath3 = "/demo-projects/rest-karate-test/target/surefire-reports/";
            String commands3 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/rest-karate-test;\nmvn -B test;";

            etDataLoader.createTJob(project, tJob3Name, resultsPath3,
                    javaMvnImage, false, commands3,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null, sut, null);

            /* *** TJob 4 *** */
            String tJob4Name = "Jasmine Rest Test";
            String resultsPath4 = "/demo-projects/jasmine-rest-test/testresults";
            String commands4 = "npm install --save request;\ngit clone https://github.com/elastest/demo-projects;\ncd demo-projects/jasmine-rest-test;\njasmine;";

            etDataLoader.createTJob(project, tJob4Name, resultsPath4, nodeImage,
                    false, commands4, EXEC_DASHBOARD_CONFIG_WITH_SUT, null,
                    null, sut, null);

            /* *** TJob 5 *** */
            String tJob5Name = "Python Rest Test";
            String resultsPath5 = "/demo-projects/python-rest-test/testresults";
            String commands5 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/python-rest-test;\npython RestTest.py;";

            etDataLoader.createTJob(project, tJob5Name, resultsPath5,
                    pythonImage, false, commands5,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null, sut, null);
        }
    }

    public void createWebapp() {
        String pjName = "Webapp";
        if (!etDataLoader.projectExists(pjName)) {
            // Create Project
            this.printLog(pjName);
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithDockerImage(project, null,
                            webAppSutName, webAppSutDesc, webAppSutImage,
                            webAppSutProtocol, webAppSutPort, null);

            List<String> tss = Arrays.asList("EUS");

            String junit5ResultsPath = "/demo-projects/web-java-test/target/surefire-reports/";
            String junit4ResultsPath = "/demo-projects/web-java-test-junit4/target/surefire-reports/";
            String cucumberResultsPath = "/demo-projects/cucumber-webapp/target/surefire-reports/";
            String gaugeResultsPath = "/demo-projects/gauge-webapp/target/surefire-reports/";
            String protractorResultsPath = "/demo-projects/protractor-webapp/testresults";
            String pythonResultsPath = "/demo-projects/python-webapp/testresults";

            /* ************************************** */
            /* *************** Junit5 *************** */
            /* ************************************** */

            /* *** TJob 1 *** */
            String tJob1Name = "Chrome Test";
            String commands1 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/web-java-test;\nmvn -Dtest=MultipleWebAppTests -B -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob1Name, junit5ResultsPath,
                    javaMvnImage, false, commands1,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* *** TJob 2 *** */

            String tJob2Name = "Firefox Test";
            String commands2 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/web-java-test;\nmvn -Dtest=MultipleWebAppTests -B -Dbrowser=firefox test;";
            etDataLoader.createTJob(project, tJob2Name, junit5ResultsPath,
                    javaMvnImage, false, commands2,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* *** TJob 3 *** */
            String tJob3Name = "Chrome Test with a browser for all tests";
            String commands3 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/web-java-test;\nmvn -Dtest=WebAppTest -B -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob3Name, junit5ResultsPath,
                    javaMvnImage, false, commands3,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* *** TJob 4 *** */

            String tJob4Name = "Multi WebApp";
            String commands4 = "git clone https://github.com/elastest/demo-projects; cd demo-projects/web-java-test; mvn -Dtest=MultipleWebAppTests -B -Dbrowser=$BROWSER test;";
            List<MultiConfig> multiConfigs = new ArrayList<>();
            multiConfigs.add(new MultiConfig("BROWSER",
                    new ArrayList<String>(Arrays.asList("chrome", "firefox"))));

            etDataLoader.createTJob(project, tJob4Name, junit5ResultsPath,
                    javaMvnImage, false, commands4,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    multiConfigs);

            /* ************************************** */
            /* *************** Junit4 *************** */
            /* ************************************** */

            /* *** TJob 5 *** */
            String tJob5Name = "Junit4 Chrome Test";
            String commands5 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/web-java-test-junit4;\nmvn -Dtest=MultipleWebAppTests -B -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob5Name, junit4ResultsPath,
                    javaMvnImage, false, commands5,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* *** TJob 6 *** */
            String tJob6Name = "Junit4 Chrome Test with a browser for all tests";
            String commands6 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/web-java-test-junit4;\nmvn -Dtest=WebAppTest -B -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob6Name, junit4ResultsPath,
                    javaMvnImage, false, commands6,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* ************************************** */
            /* ************** Cucumber ************** */
            /* ************************************** */

            /* *** TJob 7 *** */
            String tJob7Name = "Cucumber Test";
            String commands7 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/cucumber-webapp;\nmvn -B -Dbrowser=chrome -Dtest=MultipleWebAppTestsRunner test;";
            etDataLoader.createTJob(project, tJob7Name, cucumberResultsPath,
                    javaMvnImage, false, commands7,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* *** TJob 8 *** */
            String tJob8Name = "Cucumber Test with a browser for all tests";
            String commands8 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/cucumber-webapp;\nmvn -B -Dbrowser=chrome -Dtest=WebAppTestRunner test;";
            etDataLoader.createTJob(project, tJob8Name, cucumberResultsPath,
                    javaMvnImage, false, commands8,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* *************************************** */
            /* **************** Gauge **************** */
            /* *************************************** */

            /* *** TJob 9 *** */
            String tJob9Name = "Gauge Test";
            String commands9 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/gauge-webapp;\nmvn -B gauge:execute -DspecsDir=specs/multiple-webapp-tests.spec;";
            etDataLoader.createTJob(project, tJob9Name, gaugeResultsPath,
                    gaugeImage, false, commands9,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* *** TJob 10 *** */
            String tJob10Name = "Gauge Test  with a browser for all tests";
            String commands10 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/gauge-webapp;\nmvn -B gauge:execute -DspecsDir=specs/webapp-test.spec;";
            etDataLoader.createTJob(project, tJob10Name, gaugeResultsPath,
                    gaugeImage, false, commands10,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* **************************************** */
            /* ************** Protractor ************** */
            /* **************************************** */

            /* *** TJob 11 *** */
            String tJob11Name = "Protractor Test";
            String commands11 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/protractor-webapp;\nprotractor conf.js;";
            etDataLoader.createTJob(project, tJob11Name, protractorResultsPath,
                    nodeImage, false, commands11,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* **************************************** */
            /* **************** Python **************** */
            /* **************************************** */

            /* *** TJob 12 *** */
            String tJob12Name = "Python Test";
            String commands12 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/python-webapp;\npython WebappTest.py;";
            etDataLoader.createTJob(project, tJob12Name, pythonResultsPath,
                    pythonImage, false, commands12,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);

            /* *** TJob 13 *** */
            String tJob13Name = "Python Test with a browser for all tests";
            String commands13 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/python-webapp;\npython WebappTestBrowserForAll.py;";
            etDataLoader.createTJob(project, tJob13Name, pythonResultsPath,
                    pythonImage, false, commands13,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);
        }
    }

    public void createOpenVidu() {
        String pjName = "OpenVidu WebRTC";
        if (!etDataLoader.projectExists(pjName)) {
            String sutName = "OpenVidu Test App";
            String sutDesc = "OpenVidu Test App";
            String sutImage = "openvidu/testapp:elastest";
            ProtocolEnum sutProtocol = ProtocolEnum.HTTP;
            String sutPort = "4443";

            String tJobName = "Videocall Test";
            String resultsPath = "/demo-projects/openvidu-test/target/surefire-reports/";
            String commands = "echo \"Cloning project\";\ngit clone https://github.com/elastest/demo-projects;\ncd demo-projects/openvidu-test;\necho \"Compiling project\";\nmvn -DskipTests=true -B package;\necho \"Executing test\";\nmvn -B test;";
            List<String> tss = Arrays.asList("EUS");

            this.printLog(pjName);
            // Create Project
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithDockerImage(project, null,
                            sutName, sutDesc, sutImage, sutProtocol, sutPort,
                            null);

            // Create TJob
            etDataLoader.createTJob(project, tJobName, resultsPath,
                    javaMvnImage, false, commands,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut, null);
        }
    }

    public void createFullteaching() {
        String pjName = "FullTeaching";
        if (!etDataLoader.projectExists(pjName)) {
            String sut1Name = "OpenVidu Test App";
            String sut1Desc = "FullTeaching Software under Test";

            String sut1Compose = "version: '3'\r\n" + "services:\r\n"
                    + " full-teaching-mysql:\r\n" + "   image: mysql:5.7.21\r\n"
                    + "   environment:\r\n"
                    + "     - MYSQL_ROOT_PASSWORD=pass\r\n"
                    + "     - MYSQL_DATABASE=full_teaching\r\n"
                    + "     - MYSQL_USER=ft-root\r\n"
                    + "     - MYSQL_PASSWORD=pass\r\n"
                    + " full-teaching-openvidu-server-kms:\r\n"
                    + "   image: openvidu/openvidu-server-kms:1.7.0\r\n"
                    + "   expose:\r\n" + "     - 8443\r\n"
                    + "   environment:\r\n"
                    + "     - KMS_STUN_IP=stun.l.google.com\r\n"
                    + "     - KMS_STUN_PORT=19302\r\n"
                    + "     - openvidu.secret=MY_SECRET\r\n"
                    + "     - openvidu.publicurl=docker\r\n"
                    + " full-teaching:\r\n"
                    + "   image: codeurjc/full-teaching:demo\r\n"
                    + "   depends_on:\r\n" + "     - full-teaching-mysql\r\n"
                    + "     - full-teaching-openvidu-server-kms\r\n"
                    + "   expose:\r\n" + "     - 5000\r\n"
                    + "   environment:\r\n"
                    + "     - WAIT_HOSTS=full-teaching-mysql:3306\r\n"
                    + "     - WAIT_HOSTS_TIMEOUT=120\r\n"
                    + "     - MYSQL_PORT_3306_TCP_ADDR=full-teaching-mysql\r\n"
                    + "     - MYSQL_PORT_3306_TCP_PORT=3306\r\n"
                    + "     - MYSQL_ENV_MYSQL_DATABASE=full_teaching\r\n"
                    + "     - MYSQL_ENV_MYSQL_USER=ft-root\r\n"
                    + "     - MYSQL_ENV_MYSQL_PASSWORD=pass\r\n"
                    + "     - server.port=5000\r\n"
                    + "     - openvidu.url=https://full-teaching-openvidu-server-kms:8443\r\n"
                    + "     - openvidu.secret=MY_SECRET\r\n";

            ProtocolEnum sut1Protocol = ProtocolEnum.HTTP;
            String sut1Port = "5000";

            String tJobName = "E2E Teacher + Student VIDEO-SESSION";
            String resultsPath = "/full-teaching-experiment/target/surefire-reports/";
            String commands = "git clone https://github.com/elastest/full-teaching-experiment;\ncd full-teaching-experiment;\nmvn -Dtest=FullTeachingTestE2EVideoSession -B test;";
            List<String> tss = Arrays.asList("EUS");

            this.printLog(pjName);
            // Create Project
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithCompose(project, null,
                            sut1Name, sut1Desc, sut1Compose, "full-teaching",
                            sut1Protocol, sut1Port, null);

            // Create TJob
            etDataLoader.createTJob(project, tJobName, resultsPath,
                    javaMvnImage, false, commands,
                    EXEC_DASHBOARD_CONFIG_FULLTEACHING, null, tss, sut, null);
        }
    }

    /* *** TestLink *** */

    @Async
    public void createTestLinkAsync() {
        while (!etDataLoader.isReadyTestLink()) {
            // Wait
            try { // TODO timeout
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
        }

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        this.createTestLink();
    }

    public void createTestLink() {
        String pjName = "Webapp";
        if (!etDataLoader.tlProjectExists(pjName)) {
            this.printLog("TestLink " + pjName);

            TestProject project = etDataLoader.createTlTestProject(pjName, "WA",
                    "WebApp Project");
            TestSuite suite = etDataLoader.createTlTestSuite(project.getId(),
                    "Webapp Suite", "Suite of Webapp");
            TestPlan plan = etDataLoader.createTlTestPlan("WebApp Plan", pjName,
                    "Plan of Webapp");

            etDataLoader.createTlBuild(plan.getId(), "Webapp Build",
                    "The Build of Webapp");

            // case 1

            TestCase case1 = new TestCase();
            case1.setName("checkTitleAndBodyNoEmpty");
            case1.setTestSuiteId(suite.getId());
            case1.setTestProjectId(project.getId());

            case1.setSummary(
                    "Add empty data and check if new entry is not empty. This test must always fail");
            case1.setPreconditions(
                    "The Webapp application must be available and must be clean of data");

            case1 = etDataLoader.createTlTestCase(case1);

            TestCaseStep case1Step1 = etDataLoader.getNewCaseStep(
                    "Press 'New' button",
                    "New entry with empty Title and Description added", 1);

            List<TestCaseStep> case1StepsList = Arrays.asList(case1Step1);
            etDataLoader.createTestCaseSteps(case1StepsList, case1);
            case1.setSteps(case1StepsList);

            case1.setTestProjectId(project.getId());
            etDataLoader.addTlTestCaseToPlan(case1, plan);

            // case 2
            TestCase case2 = new TestCase();
            case2.setName("addMsgAndClear");
            case2.setTestSuiteId(suite.getId());
            case2.setTestProjectId(project.getId());
            case2.setSummary(
                    "Add data, press clear button and check if the data has been removed.");
            case2.setPreconditions(
                    "The Webapp application must be available and must be clean of data");

            case2 = etDataLoader.createTlTestCase(case2);

            TestCaseStep case2Step1 = etDataLoader.getNewCaseStep(
                    "Type 'MessageTitle' into Title field",
                    "'MessageTitle' will be shown into Title field", 1);

            TestCaseStep case2Step2 = etDataLoader.getNewCaseStep(
                    "Type 'MessageBody' into Body field",
                    "'MessageBody' will be shown into Body field", 2);

            TestCaseStep case2Step3 = etDataLoader.getNewCaseStep(
                    "Press 'New' button",
                    "New entry with Title='MessageTitle' and Description='MessageBody' added",
                    3);

            TestCaseStep case2Step4 = etDataLoader.getNewCaseStep(
                    "Press 'Clear' button",
                    "The entry created should have been removed", 4);

            List<TestCaseStep> case2StepsList = Arrays.asList(case2Step1,
                    case2Step2, case2Step3, case2Step4);
            etDataLoader.createTestCaseSteps(case2StepsList, case2);
            case2.setSteps(case2StepsList);

            case2.setTestProjectId(project.getId());
            etDataLoader.addTlTestCaseToPlan(case2, plan);

            // case 3
            TestCase case3 = new TestCase();
            case3.setName("findTitleAndBody");
            case3.setTestSuiteId(suite.getId());
            case3.setTestProjectId(project.getId());
            case3.setSummary("Add data and check if the data has been showed.");
            case3.setPreconditions(
                    "The Webapp application must be available and must be clean of data");
            case3 = etDataLoader.createTlTestCase(case3);

            TestCaseStep case3Step1 = etDataLoader.getNewCaseStep(
                    "Type 'MessageTitle' into Title field",
                    "'MessageTitle' will be shown into Title field", 1);

            TestCaseStep case3Step2 = etDataLoader.getNewCaseStep(
                    "Type 'MessageBody' into Body field",
                    "'MessageBody' will be shown into Body field", 2);

            TestCaseStep case3Step3 = etDataLoader.getNewCaseStep(
                    "Press 'New' button",
                    "New entry with Title='MessageTitle' and Description='MessageBody' added",
                    3);

            List<TestCaseStep> case3StepsList = Arrays.asList(case3Step1,
                    case3Step2, case3Step3);
            etDataLoader.createTestCaseSteps(case3StepsList, case3);

            case3.setSteps(case3StepsList);

            case3.setTestProjectId(project.getId());
            etDataLoader.addTlTestCaseToPlan(case3, plan);

            // Sync with ElasTest
            etDataLoader.syncTestLink();

            // Create Sut
            ExternalProject exProject = etDataLoader
                    .getExternalProjectByTestProjectId(project.getId());
            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithDockerImage(null, exProject,
                            webAppSutName, webAppSutDesc, webAppSutImage,
                            webAppSutProtocol, webAppSutPort, null);

            ExternalTJob exTJob = etDataLoader
                    .getExternalTJobByPlanId(plan.getId());
            exTJob.setSut(sut);
            exTJob.setExecDashboardConfig(EXEC_DASHBOARD_CONFIG_FOR_TESTLINK);

            this.externalService.modifyExternalTJob(exTJob);
        }
    }
}
