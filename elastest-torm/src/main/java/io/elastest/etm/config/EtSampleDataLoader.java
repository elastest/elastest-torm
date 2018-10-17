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

    private static final String EXEC_DASHBOARD_CONFIG = "{\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":false}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_WITH_SUT = "{\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":true}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_FULLTEACHING = "{\"showAllInOne\":false,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"etType\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"etType\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"etType\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"etType\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"etType\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"etType\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":false},{\"component\":\"sut_full_teaching_mysql\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_full_teaching_mysql_default_log_log\",\"activated\":true},{\"component\":\"sut_full_teaching\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_full_teaching_default_log_log\",\"activated\":true},{\"component\":\"sut_full_teaching_openvidu_server_kms\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_full_teaching_openvidu_server_kms_default_log_log\",\"activated\":true}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_FOR_TESTLINK = "{\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":false},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":true}]}}";

    private static final String webAppSutName = "Webapp";
    private static final String webAppSutDesc = "SpringBoot app with a form";
    private static final String webAppSutImage = "elastest/demo-web-java-test-sut";
    private static final ProtocolEnum webAppSutProtocol = ProtocolEnum.HTTP;
    private static final String webAppSutPort = "8080";

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
            this.createHelloWorld();
            this.createRestApi();
            this.createWebapp();
            this.createOpenVidu();
            this.createFullteaching();
            this.createMulti();
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

    public void createHelloWorld() {
        String pjName = "Hello World";
        if (!etDataLoader.projectExists(pjName)) {
            String tJobName = "My first TJob";
            String resultsPath = "/demo-projects/unit-java-test/target/surefire-reports/";
            String image = "elastest/test-etm-alpinegitjava";
            String commands = "git clone https://github.com/elastest/demo-projects\ncd demo-projects/unit-java-test\nmvn -B test\n";

            this.printLog(pjName);
            // Create Hello World Project
            Project project = etDataLoader.createProject(pjName);

            // Create Hello World TJob associated with the Hello project
            etDataLoader.createTJob(project, tJobName, resultsPath, image,
                    false, commands, EXEC_DASHBOARD_CONFIG, null, null, null,
                    null);
        }
    }

    public void createRestApi() {
        String pjName = "REST API";
        if (!etDataLoader.projectExists(pjName)) {
            String sutName = "REST App";
            String sutDesc = "Simple SpringBoot app";
            String sutImage = "elastest/demo-rest-java-test-sut";
            ProtocolEnum sutProtocol = ProtocolEnum.HTTP;
            String sutPort = "8080";

            String tJobName = "Rest Test";
            String resultsPath = "/demo-projects/rest-java-test/target/surefire-reports/";
            String tJobImage = "elastest/test-etm-alpinegitjava";
            String commands = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/rest-java-test;\nmvn -B test;";

            this.printLog(pjName);
            // Create Project
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithDockerImage(project, null,
                            sutName, sutDesc, sutImage, sutProtocol, sutPort,
                            null);

            // Create TJob
            etDataLoader.createTJob(project, tJobName, resultsPath, tJobImage,
                    false, commands, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null,
                    sut, null);
        }
    }

    public void createWebapp() {
        String pjName = "Webapp";
        if (!etDataLoader.projectExists(pjName)) {

            String tJob1Name = "Chrome Test";
            String resultsPath = "/demo-projects/web-java-test/target/surefire-reports/";
            String tJobImage = "elastest/test-etm-alpinegitjava";
            String commands1 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/web-java-test;\nmvn -Dtest=MultipleWebAppTests -B -Dbrowser=chrome test;";
            List<String> tss = Arrays.asList("EUS");

            String tJob2Name = "Firefox Test";
            String commands2 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/web-java-test;\nmvn -Dtest=MultipleWebAppTests -B -Dbrowser=firefox test;";

            this.printLog(pjName);
            // Create Project
            Project project = etDataLoader.createProject(pjName);

            // Create Sut
            SutSpecification sut = etDataLoader
                    .createSutDeployedByElastestWithDockerImage(project, null,
                            webAppSutName, webAppSutDesc, webAppSutImage,
                            webAppSutProtocol, webAppSutPort, null);

            // Create TJob1
            etDataLoader.createTJob(project, tJob1Name, resultsPath, tJobImage,
                    false, commands1, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss,
                    sut, null);
            // Create TJob2
            etDataLoader.createTJob(project, tJob2Name, resultsPath, tJobImage,
                    false, commands2, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss,
                    sut, null);
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
            String tJobImage = "elastest/test-etm-alpinegitjava";
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
            etDataLoader.createTJob(project, tJobName, resultsPath, tJobImage,
                    false, commands, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss,
                    sut, null);
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
            String tJobImage = "elastest/test-etm-alpinegitjava";
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
            etDataLoader.createTJob(project, tJobName, resultsPath, tJobImage,
                    false, commands, EXEC_DASHBOARD_CONFIG_FULLTEACHING, null,
                    tss, sut, null);
        }
    }

    public void createMulti() {
        String pjName = "Multi Test";
        if (!etDataLoader.projectExists(pjName)) {
            String tJobName = "Multi Java Test";
            String resultsPath = "/demo-projects/multi-java-test/target/surefire-reports/";
            String image = "elastest/test-etm-alpinegitjava";
            String commands = "git clone https://github.com/elastest/demo-projects\ncd demo-projects/multi-java-test\nmvn -B test\n";

            this.printLog(pjName);
            // Create Hello World Project
            Project project = etDataLoader.createProject(pjName);

            // Multi config
            List<MultiConfig> multiConfigs = new ArrayList<>();
            multiConfigs.add(new MultiConfig("LEFT_OPERAND",
                    new ArrayList<String>(Arrays.asList("40", "3", "11"))));
            multiConfigs.add(new MultiConfig("RIGHT_OPERAND",
                    new ArrayList<String>(Arrays.asList("10", "2"))));

            // Create Hello World TJob associated with the Hello project
            etDataLoader.createTJob(project, tJobName, resultsPath, image,
                    false, commands, EXEC_DASHBOARD_CONFIG, null, null, null,
                    multiConfigs);
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
