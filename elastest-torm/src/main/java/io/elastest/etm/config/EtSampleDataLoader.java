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
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.MultiConfig;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.service.ExternalService;
import io.elastest.etm.utils.UtilsService;

@Service
public class EtSampleDataLoader {
    final Logger logger = getLogger(lookup().lookupClass());

    private EtDataLoader etDataLoader;
    private ExternalService externalService;
    private UtilsService utilsService;

    private static final String EXEC_DASHBOARD_CONFIG = "{\"showAllInOne\":false,\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":false}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_WITH_SUT = "{\"showAllInOne\":false,\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":true}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_OPENVIDU = "{\"showAllInOne\":false,\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":true},{\"activated\":true,\"component\":\"sut_aux\",\"name\":\"sut_aux_default_log_log\",\"stream\":\"default_log\",\"streamType\":\"log\"}]}}";

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

    private static String javaRelativeResultsPath = "/target/surefire-reports";
    private static String pythonRelativeResultsPath = "/testresults";
    private static String jasmineAndProtractorRelativeResultsPath = "/testresults";

    @Value("${et.config.folder}")
    private String configFolder;

    public EtSampleDataLoader(EtDataLoader etDataLoader,
            ExternalService externalService, UtilsService utilsService) {
        this.etDataLoader = etDataLoader;
        this.externalService = externalService;
        this.utilsService = utilsService;
    }

    @PostConstruct
    public void createData() {
        this.createData(false);
    }

    public boolean createData(boolean withForce) {
        try {
            String sampleDataCreated = (configFolder.endsWith("/")
                    ? configFolder
                    : configFolder + "/") + "sampleDataCreated";
            File sampleDataCreatedFile = new File(sampleDataCreated);
            boolean alreadyExists = sampleDataCreatedFile.exists();
            if (withForce || (!withForce && !alreadyExists)) {
                this.createUnitTests();
                this.createRestApi();
                this.createWebapp();
                if (!utilsService.isKubernetes()) {
                    this.createOpenVidu();
                    this.createFullteaching();
                    this.createEMS();
                    this.createEDS();
                }

                if (etDataLoader.isStartedTestLink()) {
                    this.createTestLink();
                }

                if (!alreadyExists) {
                    try {
                        logger.info("Sample Data has been created!");
                        sampleDataCreatedFile.createNewFile();
                    } catch (IOException e) {
                        logger.error("File {} has not been created",
                                sampleDataCreated);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            logger.error("Error on create Sample Data", e);
        }
        return false;
    }

    public void printLog(String pjName) {
        logger.debug("Creating '{}' Sample Data...", pjName);
    }

    public void createUnitTests() {
        String unitTestFolder = "/demo-projects/unit";

        String pjName = "Unit Tests";
        if (!etDataLoader.projectExists(pjName)) {
            // Create Unit Tests Project
            this.printLog(pjName);
            Project project = etDataLoader.createProject(pjName);

            String junit5UnitProjectPath = unitTestFolder + "/junit5-unit-test";
            String junit5UnitResultsPath = junit5UnitProjectPath
                    + javaRelativeResultsPath;

            String junit4UnitProjectPath = unitTestFolder + "/junit4-unit-test";
            String junit4UnitResultsPath = junit4UnitProjectPath
                    + javaRelativeResultsPath;

            /* *** TJob 1 *** */
            String tJob1Name = "JUnit5 Unit Test";
            String commands1 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + junit5UnitProjectPath + ";\nmvn -B test\n";

            etDataLoader.createTJob(project, tJob1Name, junit5UnitResultsPath,
                    javaMvnImage, false, commands1, EXEC_DASHBOARD_CONFIG, null,
                    null, null, null);

            /* *** TJob 2 *** */
            String tJob2Name = "JUnit4 Unit Test";
            String commands2 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + junit4UnitProjectPath + ";\nmvn -B test\n";

            etDataLoader.createTJob(project, tJob2Name, junit4UnitResultsPath,
                    javaMvnImage, false, commands2, EXEC_DASHBOARD_CONFIG, null,
                    null, null, null);

            /* *** TJob 3 *** */
            String pythonUnitProjectPath = unitTestFolder + "/python-unit-test";
            String pythonUnitResultsPath = pythonUnitProjectPath
                    + pythonRelativeResultsPath;

            String tJob3Name = "Python Unit Test";
            String commands3 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + pythonUnitProjectPath + ";\npython UnitTest.py;";

            etDataLoader.createTJob(project, tJob3Name, pythonUnitResultsPath,
                    pythonImage, false, commands3, EXEC_DASHBOARD_CONFIG, null,
                    null, null, null);

            /* *** TJob 4 *** */
            String jasmineUnitProjectPath = unitTestFolder
                    + "/jasmine-unit-test";
            String jasmineUnitResultsPath = jasmineUnitProjectPath
                    + jasmineAndProtractorRelativeResultsPath;

            String tJob4Name = "Jasmine Unit Test";

            String commands4 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + jasmineUnitProjectPath + ";\njasmine;";

            etDataLoader.createTJob(project, tJob4Name, jasmineUnitResultsPath,
                    nodeImage, false, commands4, EXEC_DASHBOARD_CONFIG, null,
                    null, null, null);
        }
    }

    public void createRestApi() {
        String restTestFolder = "/demo-projects/rest";

        String pjName = "Rest Api";
        if (!etDataLoader.projectExists(pjName)) {
            // Create Project
            this.printLog(pjName);
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            String sutName = "Rest App";
            String sutDesc = "Simple SpringBoot app";
            String sutImage = "elastest/demo-rest-java-test-sut";
            ProtocolEnum sutProtocol = ProtocolEnum.HTTP;
            String sutPort = "8080";

            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithDockerImage(project, null,
                            sutName, sutDesc, sutImage, sutProtocol, sutPort,
                            null);

            String junit5RestProjectPath = restTestFolder + "/junit5-rest-test";
            String junit5RestResultsPath = junit5RestProjectPath
                    + javaRelativeResultsPath;

            String junit4RestProjectPath = restTestFolder + "/junit4-rest-test";
            String junit4RestResultsPath = junit4RestProjectPath
                    + javaRelativeResultsPath;

            /* *** TJob 1 *** */
            String tJob1Name = "JUnit5 Rest Test";
            String commands1 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + junit5RestProjectPath + ";\nmvn -B test;";

            etDataLoader.createTJob(project, tJob1Name, junit5RestResultsPath,
                    javaMvnImage, false, commands1,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null, sut, null);

            /* *** TJob 2 *** */
            String tJob2Name = "JUnit4 Rest Test";
            String commands2 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + junit4RestProjectPath + ";\nmvn -B test;";

            etDataLoader.createTJob(project, tJob2Name, junit4RestResultsPath,
                    javaMvnImage, false, commands2,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null, sut, null);

            /* *** TJob 3 *** */
            String karateRestProjectPath = restTestFolder + "/karate-rest-test";
            String karateRestResultsPath = karateRestProjectPath
                    + javaRelativeResultsPath;

            String tJob3Name = "Karate Rest Test";
            String commands3 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + karateRestProjectPath + ";\nmvn -B test;";

            etDataLoader.createTJob(project, tJob3Name, karateRestResultsPath,
                    javaMvnImage, false, commands3,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null, sut, null);

            /* *** TJob 4 *** */
            String jasmineRestProjectPath = restTestFolder
                    + "/jasmine-rest-test";
            String jasmineRestResultsPath = jasmineRestProjectPath
                    + jasmineAndProtractorRelativeResultsPath;

            String tJob4Name = "Jasmine Rest Test";
            String commands4 = "npm install --save request;\ngit clone https://github.com/elastest/demo-projects;\ncd "
                    + jasmineRestProjectPath + ";\njasmine;";

            etDataLoader.createTJob(project, tJob4Name, jasmineRestResultsPath,
                    nodeImage, false, commands4, EXEC_DASHBOARD_CONFIG_WITH_SUT,
                    null, null, sut, null);

            /* *** TJob 5 *** */
            String pythonRestProjectPath = restTestFolder + "/python-rest-test";
            String pythonRestResultsPath = pythonRestProjectPath
                    + pythonRelativeResultsPath;

            String tJob5Name = "Python Rest Test";
            String commands5 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + pythonRestProjectPath + ";\npython RestTest.py;";

            etDataLoader.createTJob(project, tJob5Name, pythonRestResultsPath,
                    pythonImage, false, commands5,
                    EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null, sut, null);
        }
    }

    public void createWebapp() {
        String webAppFolder = "/demo-projects/webapp";

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

            /* ************************************** */
            /* *************** Junit5 *************** */
            /* ************************************** */

            String junit5MultipleBrowsersProjectPath = webAppFolder
                    + "/junit5-web-multiple-browsers-test";
            String junit5MultipleBrowsersResultsPath = junit5MultipleBrowsersProjectPath
                    + javaRelativeResultsPath;

            String junit5SingleBrowserProjectPath = webAppFolder
                    + "/junit5-web-single-browser-test";
            String junit5SingleBrowserResultsPath = junit5SingleBrowserProjectPath
                    + javaRelativeResultsPath;

            /* *** TJob 1 *** */
            String tJob1Name = "JUnit5 Multi Browser Test";
            String commands1 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + junit5MultipleBrowsersProjectPath
                    + ";\nmvn -B -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob1Name,
                    junit5MultipleBrowsersResultsPath, javaMvnImage, false,
                    commands1, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* *** TJob 2 *** */

            String tJob2Name = "JUnit5 Multi Browser Test (Firefox)";
            String commands2 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + junit5MultipleBrowsersProjectPath
                    + ";\nmvn -B -Dbrowser=firefox test;";
            etDataLoader.createTJob(project, tJob2Name,
                    junit5MultipleBrowsersResultsPath, javaMvnImage, false,
                    commands2, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* *** TJob 3 *** */
            String tJob3Name = "JUnit5 Single Browser Test";
            String commands3 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + junit5SingleBrowserProjectPath
                    + ";\nmvn -B -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob3Name,
                    junit5SingleBrowserResultsPath, javaMvnImage, false,
                    commands3, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* *** TJob 4 *** */

            String tJob4Name = "Multi JUnit5 Multi Browser Test";
            String commands4 = "git clone https://github.com/elastest/demo-projects; cd "
                    + junit5MultipleBrowsersProjectPath
                    + "; mvn -B -Dbrowser=$BROWSER test;";
            List<MultiConfig> multiConfigs = new ArrayList<>();
            multiConfigs.add(new MultiConfig("BROWSER",
                    new ArrayList<String>(Arrays.asList("chrome", "firefox"))));

            etDataLoader.createTJob(project, tJob4Name,
                    junit5MultipleBrowsersResultsPath, javaMvnImage, false,
                    commands4, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    multiConfigs);

            /* ************************************** */
            /* *************** Junit4 *************** */
            /* ************************************** */

            String junit4MultipleBrowsersProjectPath = webAppFolder
                    + "/junit4-web-multiple-browsers-test";
            String junit4MultipleBrowsersResultsPath = junit4MultipleBrowsersProjectPath
                    + javaRelativeResultsPath;

            String junit4SingleBrowserProjectPath = webAppFolder
                    + "/junit4-web-single-browser-test";
            String junit4SingleBrowserResultsPath = junit4SingleBrowserProjectPath
                    + javaRelativeResultsPath;

            /* *** TJob 5 *** */
            String tJob5Name = "Junit4 Multi Browser Test";
            String commands5 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + junit4MultipleBrowsersProjectPath
                    + ";\nmvn -B -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob5Name,
                    junit4MultipleBrowsersResultsPath, javaMvnImage, false,
                    commands5, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* *** TJob 6 *** */
            String tJob6Name = "Junit4 Single Browser Test";
            String commands6 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + junit4SingleBrowserProjectPath
                    + ";\nmvn -B -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob6Name,
                    junit4SingleBrowserResultsPath, javaMvnImage, false,
                    commands6, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* ************************************** */
            /* ************** Cucumber ************** */
            /* ************************************** */

            String cucumberMultipleBrowsersProjectPath = webAppFolder
                    + "/cucumber-web-multiple-browsers-test";
            String cucumberMultipleBrowsersResultsPath = cucumberMultipleBrowsersProjectPath
                    + javaRelativeResultsPath;

            String cucumberSingleBrowserProjectPath = webAppFolder
                    + "/cucumber-web-single-browser-test";
            String cucumberSingleBrowserResultsPath = cucumberSingleBrowserProjectPath
                    + javaRelativeResultsPath;

            /* *** TJob 7 *** */
            String tJob7Name = "Cucumber Multi Browser Test";
            String commands7 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + cucumberMultipleBrowsersProjectPath
                    + ";\nmvn -B -Dtest=WebAppTestRunner -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob7Name,
                    cucumberMultipleBrowsersResultsPath, javaMvnImage, false,
                    commands7, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* *** TJob 8 *** */
            String tJob8Name = "Cucumber Single Browser Test";
            String commands8 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + cucumberSingleBrowserProjectPath
                    + ";\nmvn -B -Dtest=WebAppTestRunner -Dbrowser=chrome test;";
            etDataLoader.createTJob(project, tJob8Name,
                    cucumberSingleBrowserResultsPath, javaMvnImage, false,
                    commands8, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* *************************************** */
            /* **************** Gauge **************** */
            /* *************************************** */
            String gaugeMultipleBrowsersProjectPath = webAppFolder
                    + "/gauge-web-multiple-browsers-test";
            String gaugeMultipleBrowsersResultsPath = gaugeMultipleBrowsersProjectPath
                    + javaRelativeResultsPath;

            String gaugeSingleBrowserProjectPath = webAppFolder
                    + "/gauge-web-single-browser-test";
            String gaugeSingleBrowserResultsPath = gaugeSingleBrowserProjectPath
                    + javaRelativeResultsPath;

            /* *** TJob 9 *** */
            String tJob9Name = "Gauge Multi Browser Test";
            String commands9 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + gaugeMultipleBrowsersProjectPath + ";\nmvn clean test ;";
            etDataLoader.createTJob(project, tJob9Name,
                    gaugeMultipleBrowsersResultsPath, gaugeImage, false,
                    commands9, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* *** TJob 10 *** */
            String tJob10Name = "Gauge Single Browser Test";
            String commands10 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + gaugeSingleBrowserProjectPath + ";\nmvn clean test ;";
            etDataLoader.createTJob(project, tJob10Name,
                    gaugeSingleBrowserResultsPath, gaugeImage, false,
                    commands10, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* **************************************** */
            /* ************** Protractor ************** */
            /* **************************************** */
            String protractorMultiBrowsersProjectPath = webAppFolder
                    + "/protractor-web-multiple-browsers-test";
            String protractorMultiBrowsersResultsPath = protractorMultiBrowsersProjectPath
                    + jasmineAndProtractorRelativeResultsPath;

            String protractorSingleBrowserProjectPath = webAppFolder
                    + "/protractor-web-single-browser-test";
            String protractorSingleBrowserResultsPath = protractorSingleBrowserProjectPath
                    + jasmineAndProtractorRelativeResultsPath;

            /* *** TJob 11 *** */
            String tJob11Name = "Protractor Multi Browser Test";
            String commands11 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + protractorMultiBrowsersProjectPath
                    + ";\nprotractor conf.js;";
            etDataLoader.createTJob(project, tJob11Name,
                    protractorMultiBrowsersResultsPath, nodeImage, false,
                    commands11, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* *** TJob 12 *** */
            String tJob12Name = "Protractor Single Browser Test";
            String commands12 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + protractorSingleBrowserProjectPath
                    + ";\nprotractor conf.js;";
            etDataLoader.createTJob(project, tJob12Name,
                    protractorSingleBrowserResultsPath, nodeImage, false,
                    commands12, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* **************************************** */
            /* **************** Python **************** */
            /* **************************************** */

            String pythonMultipleBrowsersProjectPath = webAppFolder
                    + "/python-web-multiple-browsers-test";
            String pythonMultipleBrowsersResultsPath = pythonMultipleBrowsersProjectPath
                    + pythonRelativeResultsPath;

            String pythonSingleBrowserProjectPath = webAppFolder
                    + "/python-web-single-browser-test";
            String pythonSingleBrowserResultsPath = pythonSingleBrowserProjectPath
                    + pythonRelativeResultsPath;

            /* *** TJob 13 *** */
            String tJob13Name = "Python Multi Browser Test";
            String commands13 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + pythonMultipleBrowsersProjectPath
                    + ";\npython WebappTest.py;";
            etDataLoader.createTJob(project, tJob13Name,
                    pythonMultipleBrowsersResultsPath, pythonImage, false,
                    commands13, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);

            /* *** TJob 14 *** */
            String tJob14Name = "Python Single Browser Test";
            String commands14 = "git clone https://github.com/elastest/demo-projects;\ncd "
                    + pythonSingleBrowserProjectPath
                    + ";\npython WebappTest.py;";
            etDataLoader.createTJob(project, tJob14Name,
                    pythonSingleBrowserResultsPath, pythonImage, false,
                    commands14, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut,
                    null);
        }
    }

    public void createOpenVidu() {
        String pjName = "OpenVidu WebRTC";
        if (!etDataLoader.projectExists(pjName)) {
            String sutName = "OpenVidu Test App";
            String sutDesc = "OpenVidu Test App";
            String sutImage = "elastest/test-etm-alpinedockernode";
            ProtocolEnum sutProtocol = ProtocolEnum.HTTPS;
            String sutPort = "5000";
            String sutCommands = "echo \"### Create Dockerfile ###\"\n"
                    + "mkdir dockerimage;\n" + "cd dockerimage;\n"
                    + "echo \"FROM openvidu/openvidu-server-kms:2.6.0\" >> Dockerfile\n"
                    + "echo \"RUN apt-get update\" >> Dockerfile\n"
                    + "echo \"RUN apt-get install -y git\" >> Dockerfile\n"
                    + "echo \"RUN apt-get install -y nodejs npm\" >> Dockerfile\n"
                    + "echo \"RUN apt-get install -y curl\" >> Dockerfile\n"
                    + "echo \"RUN curl -sL https://deb.nodesource.com/setup_8.x | bash - \\\\\" >> Dockerfile\n"
                    + "echo \"    && apt-get install -y nodejs\" >> Dockerfile\n"
                    + "echo \"RUN npm install -g @angular/cli@7.1.3\" >> Dockerfile\n"
                    + "echo \"RUN npm install -g http-server\" >> Dockerfile\n"
                    + "echo \"EXPOSE 4443\" >> Dockerfile\n"
                    + "echo \"EXPOSE 5000\" >> Dockerfile\n"
                    + "echo -n \"CMD \" >> Dockerfile\n"
                    + "echo -n \"echo 'run supervisord';\" >> Dockerfile\n"
                    + "echo -n \"/usr/bin/supervisord & \" >> Dockerfile\n"
                    + "echo -n \"echo '##### BUILD OPENVIDU #####';\" >> Dockerfile\n"
                    + "echo -n \"git clone https://github.com/OpenVidu/openvidu.git; \" >> Dockerfile\n"
                    + "echo -n \"cd openvidu;\" >> Dockerfile\n"
                    + "echo -n \"git checkout tags/v2.8.0; \" >> Dockerfile\n"
                    + "echo -n \"cd openvidu-browser;\" >> Dockerfile\n"
                    + "echo -n \"npm install; \" >> Dockerfile\n"
                    + "echo -n \"npm run build; \" >> Dockerfile\n"
                    + "echo -n \"npm link; \" >> Dockerfile\n"
                    + "echo -n \"cd ..; \" >> Dockerfile\n"
                    + "echo -n \"cd openvidu-testapp; \" >> Dockerfile\n"
                    + "echo -n \"echo 'run npm install';\" >> Dockerfile\n"
                    + "echo -n \"npm install; \" >> Dockerfile\n"
                    + "echo -n \"npm link openvidu-browser; \" >> Dockerfile\n"
                    + "echo -n \"ng build --output-path ./dist;\" >> Dockerfile;\n"
                    + "echo -n \"cd dist;\" >> Dockerfile;\n"
                    + "echo -n \"openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -subj '/CN=www.mydom.com/O=My Company LTD./C=US' -keyout key.pem -out cert.pem;\" >> Dockerfile;\n"
                    + "echo -n \" echo '##### RUN OPENVIDU #####';\" >> Dockerfile\n"
                    + "echo -n \"http-server -S -p 5000;\" >> Dockerfile;\n"
                    + "cat Dockerfile;\n" + "echo \"\";\n"
                    + "echo “### BUILD AND RUN ###”\n"
                    + "docker build -t openvidu/elastest .\n" + "echo \"\"\n"
                    + "echo \"Running image\"\n"
                    + "docker run --name $ET_SUT_CONTAINER_NAME --network $ET_NETWORK -e \"OPENVIDU_PUBLICURL=docker\" openvidu/elastest\n";

            String tJobName = "Videocall Test";
            String resultsPath = "/demo-projects/openvidu-test/target/surefire-reports/";
            String tJobCommands = "echo \"Cloning project\";\ngit clone https://github.com/elastest/demo-projects;\ncd demo-projects/openvidu-test;\necho \"Compiling project\";\nmvn -DskipTests=true -B package;\necho \"Executing test\";\nmvn -B test -DbrowserVersion=74;";
            List<String> tss = Arrays.asList("EUS");

            this.printLog(pjName);
            // Create Project
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithCommands(project, null,
                            sutName, sutDesc, sutImage, sutCommands,
                            CommandsOptionEnum.IN_NEW_CONTAINER, sutProtocol,
                            sutPort, null);

            // Create TJob
            etDataLoader.createTJob(project, tJobName, resultsPath,
                    javaMvnImage, false, tJobCommands,
                    EXEC_DASHBOARD_CONFIG_OPENVIDU, null, tss, sut, null);
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

            ProtocolEnum sut1Protocol = ProtocolEnum.HTTPS;
            String sut1Port = "5000";

            String tJobName = "E2E Teacher + Student VIDEO-SESSION";
            String resultsPath = "/full-teaching-experiment/target/surefire-reports/";
            String commands = "git clone https://github.com/elastest/full-teaching-experiment;\ncd full-teaching-experiment;\nmvn -Dtest=FullTeachingTestE2EVideoSession -B test -DbrowserVersion=74;";
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

    private void createEMS() {
        String pjName = "EMS Example";
        if (!etDataLoader.projectExists(pjName)) {
            String sutName = "nginx";
            String sutDesc = "nginx";

            String sutCompose = "version: '3'\r\n" + "services:\r\n"
                    + " nginx-service:\r\n" + "   image: nginx\r\n"
                    + "   entrypoint:\r\n" + "     - /bin/bash\r\n"
                    + "     - \"-c\"\r\n"
                    + "     - \"dd if=/dev/random of=/usr/share/nginx/html/sparse bs=1024 count=1 seek=5242880000;nginx;sleep infinity\"\r\n"
                    + "   expose:\r\n" + "     - \"80\"";

            String mainService = "nginx-service";
            ProtocolEnum sutProtocol = ProtocolEnum.HTTP;
            String sutPort = "80";

            String tJobName = "Double bandwidth";
            String resultsPath = "";
            String tJobImage = "imdeasoftware/e2etjob";
            String commands = "cd /go;./tjob";
            List<String> tss = Arrays.asList("EMS");

            this.printLog(pjName);
            // Create Project
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithCompose(project, null,
                            sutName, sutDesc, sutCompose, mainService,
                            sutProtocol, sutPort, null);

            // Create TJob
            etDataLoader.createTJob(project, tJobName, resultsPath, tJobImage,
                    false, commands, EXEC_DASHBOARD_CONFIG, null, tss, sut,
                    null);
        }

    }

    private void createEDS() {
        String pjName = "EDS Example";
        if (!etDataLoader.projectExists(pjName)) {
            String sutName = "EDS SuT";
            String sutDesc = "Sensor Actuator Logic SuT";
            String sutImage = "elastest/eds-base";
            String sutCommands = "git clone https://github.com/elastest/elastest-device-emulator-service.git /tmp/eds\n"
                    + "\n" + "# create app template\n"
                    + "./create-app-structure -d TestApplication\n"
                    + "cp /tmp/eds/demo/eds_sut/TestApplication/* apps/TestApplication/src/testapplication/\n"
                    + "\n" + "sh ./apps/test-application -v &\n" + "\n"
                    + "python -m SimpleHTTPServer 9000";

            ProtocolEnum sutProtocol = ProtocolEnum.HTTP;
            String sutPort = "9000";

            String tJobName = "EDS TJob";
            String resultsPath = "/tmp/test-reports";
            String tJobImage = "elastest/eds-base";
            String commands = "# Give enough time for full initialization of SuT\n"
                    + "sleep 10\n" + "\n"
                    + "git clone https://github.com/elastest/elastest-device-emulator-service.git /tmp/eds\n"
                    + "\n" + "# create TJob app template\n"
                    + "./create-app-structure -d TestJob\n"
                    + "cp /tmp/eds/demo/eds_tjob/tjob1/* apps/TestJob/src/testjob/\n"
                    + "\n" + "sh ./apps/test-job -v";
            List<String> tss = Arrays.asList("EDS");

            this.printLog(pjName);
            // Create Project
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithCommands(project, null,
                            sutName, sutDesc, sutImage, sutCommands,
                            CommandsOptionEnum.DEFAULT, sutProtocol, sutPort,
                            null);

            // Create TJob
            etDataLoader.createTJob(project, tJobName, resultsPath, tJobImage,
                    false, commands, EXEC_DASHBOARD_CONFIG, null, tss, sut,
                    null);
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
