package io.elastest.etm.config;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.service.ProjectService;
import io.elastest.etm.service.SutService;
import io.elastest.etm.service.TJobService;

@Service
public class EtSampleDataLoader {
    final Logger logger = getLogger(lookup().lookupClass());

    private ProjectService projectService;
    private TJobService tJobService;
    private SutService sutService;
    private EsmService esmService;

    private static final String EXEC_DASHBOARD_CONFIG = "{\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":false}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_WITH_SUT = "{\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":true}]}}";
    private static final String EXEC_DASHBOARD_CONFIG_FULLTEACHING = "{\"showAllInOne\":false,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"etType\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"etType\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"etType\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"etType\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"etType\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"etType\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"etType\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":false},{\"component\":\"sut_full_teaching_mysql\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_full_teaching_mysql_default_log_log\",\"activated\":true},{\"component\":\"sut_full_teaching\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_full_teaching_default_log_log\",\"activated\":true},{\"component\":\"sut_full_teaching_openvidu_server_kms\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_full_teaching_openvidu_server_kms_default_log_log\",\"activated\":true}]}}";

    public EtSampleDataLoader(ProjectService projectService,
            TJobService tJobService, SutService sutService,
            EsmService esmService) {
        this.projectService = projectService;
        this.tJobService = tJobService;
        this.sutService = sutService;
        this.esmService = esmService;
    }

    @PostConstruct
    public void createData() {
        this.createHelloWorld();
        this.createRestApi();
        this.createWebapp();
        this.createOpenVidu();
        this.createFullteaching();
    }

    public void createHelloWorld() {
        String pjName = "Hello World";
        if (!projectExists(pjName)) {
            String tJobName = "My first TJob";
            String resultsPath = "/demo-projects/unit-java-test/target/surefire-reports/";
            String image = "elastest/test-etm-alpinegitjava";
            String commands = "git clone https://github.com/elastest/demo-projects\ncd demo-projects/unit-java-test\nmvn -B test\n";

            this.printLog(pjName);
            // Create Hello World Project
            Project project = this.createProject(pjName);

            // Create Hello World TJob associated with the Hello project
            this.createTJob(project, tJobName, resultsPath, image, false,
                    commands, EXEC_DASHBOARD_CONFIG, null, null, null);
        }
    }

    public void createRestApi() {
        String pjName = "REST API";
        if (!projectExists(pjName)) {
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
            Project project = this.createProject(pjName);

            // Create Sut
            SutSpecification sut = this
                    .createSutDeployedByElastestWithDockerImage(project,
                            sutName, sutDesc, sutImage, sutProtocol, sutPort,
                            null);

            // Create TJob
            this.createTJob(project, tJobName, resultsPath, tJobImage, false,
                    commands, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, null, sut);
        }
    }

    public void createWebapp() {
        String pjName = "Webapp";
        if (!projectExists(pjName)) {
            String sutName = "Webapp";
            String sutDesc = "SpringBoot app with a form";
            String sutImage = "elastest/demo-web-java-test-sut";
            ProtocolEnum sutProtocol = ProtocolEnum.HTTP;
            String sutPort = "8080";

            String tJob1Name = "Chrome Test";
            String resultsPath = "/demo-projects/web-java-test/target/surefire-reports/";
            String tJobImage = "elastest/test-etm-alpinegitjava";
            String commands1 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/web-java-test;\nmvn -Dtest=MultipleWebAppTests -B -Dbrowser=chrome test;";
            List<String> tss = Arrays.asList("EUS");

            String tJob2Name = "Firefox Test";
            String commands2 = "git clone https://github.com/elastest/demo-projects;\ncd demo-projects/web-java-test;\nmvn -Dtest=MultipleWebAppTests -B -Dbrowser=firefox test;";

            this.printLog(pjName);
            // Create Project
            Project project = this.createProject(pjName);

            // Create Sut
            SutSpecification sut = this
                    .createSutDeployedByElastestWithDockerImage(project,
                            sutName, sutDesc, sutImage, sutProtocol, sutPort,
                            null);

            // Create TJob1
            this.createTJob(project, tJob1Name, resultsPath, tJobImage, false,
                    commands1, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut);
            // Create TJob2
            this.createTJob(project, tJob2Name, resultsPath, tJobImage, false,
                    commands2, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut);
        }
    }

    public void createOpenVidu() {
        String pjName = "OpenVidu WebRTC";
        if (!projectExists(pjName)) {
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
            Project project = this.createProject(pjName);

            // Create Sut
            SutSpecification sut = this
                    .createSutDeployedByElastestWithDockerImage(project,
                            sutName, sutDesc, sutImage, sutProtocol, sutPort,
                            null);

            // Create TJob
            this.createTJob(project, tJobName, resultsPath, tJobImage, false,
                    commands, EXEC_DASHBOARD_CONFIG_WITH_SUT, null, tss, sut);
        }
    }

    public void createFullteaching() {
        String pjName = "FullTeaching";
        if (!projectExists(pjName)) {
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
            Project project = this.createProject(pjName);

            // Create Sut
            SutSpecification sut = this.createSutDeployedByElastestWithCompose(
                    project, sut1Name, sut1Desc, sut1Compose, "full-teaching",
                    sut1Protocol, sut1Port, null);

            // Create TJob
            this.createTJob(project, tJobName, resultsPath, tJobImage, false,
                    commands, EXEC_DASHBOARD_CONFIG_FULLTEACHING, null, tss, sut);
        }
    }

    public void printLog(String pjName) {
        logger.debug("Creating '{}' Sample Data...", pjName);
    }

    /* ********************** */
    /* *** Create Methods *** */
    /* ********************** */

    /* *** Project *** */
    public boolean projectExists(String name) {
        return projectService.getProjectByName(name) != null;
    }

    public Project createProject(String projectName) {
        Project project = new Project();
        project.setName(projectName);
        return this.createProject(project);
    }

    public Project createProject(Project project) {
        return projectService.createProject(project);
    }

    /* *** TJob *** */

    public TJob createTJob(Project project, String name, String resultsPath,
            String image, boolean useImageCommands, String commands,
            String execDashboardConfig, List<Parameter> parameters,
            List<String> activatedTSSList, SutSpecification sut) {
        TJob tJob = new TJob();

        tJob.setProject(project);
        tJob.setName(name);
        if (resultsPath != null && !"".equals(resultsPath)) {
            tJob.setResultsPath(resultsPath);
        }
        tJob.setImageName(image);
        if (!useImageCommands) {
            tJob.setCommands(commands);
        }
        tJob.setExecDashboardConfigPath(execDashboardConfig);

        if (parameters != null) {
            tJob.setParameters(parameters);
        }

        if (sut != null && sut.getId() > 0) {
            tJob.setSut(sut);
        }

        // TSS
        if (activatedTSSList != null && activatedTSSList.size() > 0) {
            List<String> selectedServices = new ArrayList<>();

            List<SupportService> servicesList = esmService
                    .getRegisteredServices();

            for (String activatedTSSName : activatedTSSList) {
                for (SupportService tss : servicesList) {
                    if (tss.getName().toLowerCase()
                            .equals(activatedTSSName.toLowerCase())) {
                        String selectedService = "{" + "\"id\":" + "\""
                                + tss.getId() + "\"," + "\"name\":" + "\""
                                + tss.getName() + "\"," + "\"selected\":true"
                                + "}";
                        selectedServices.add(selectedService);
                        break;
                    }
                }
            }

            String selectedServicesString = "[";
            boolean first = true;
            for (String selectedService : selectedServices) {
                if (!first) {
                    selectedServicesString += ",";
                } else {
                    first = false;
                }
                selectedServicesString += selectedService;
            }
            selectedServicesString += "]";
            tJob.setSelectedServices(selectedServicesString);

        }
        return this.createTJob(tJob);
    }

    public TJob createTJob(TJob tJob) {
        return tJobService.createTJob(tJob);
    }

    /* *** Sut *** */

    public SutSpecification createSutDeployedByElastestWithCommands(
            Project project, String name, String desc, String image,
            String commands, CommandsOptionEnum commandsOption,
            ProtocolEnum protocol, String port, List<Parameter> parameters) {
        SutSpecification sut = new SutSpecification();
        sut.setSutType(SutTypeEnum.MANAGED);
        sut.setManagedDockerType(ManagedDockerType.COMMANDS);

        sut.setProject(project);
        sut.setName(name);
        sut.setDescription(desc);
        sut.setSpecification(image);
        sut.setCommands(commands);
        sut.setCommandsOption(commandsOption);

        if (protocol != null) {
            sut.setProtocol(protocol);
        }

        if (port != null && !"".equals(port)) {
            sut.setPort(port);
        }

        if (parameters != null) {
            sut.setParameters(parameters);
        }

        return this.createSut(sut);
    }

    public SutSpecification createSutDeployedByElastestWithDockerImage(
            Project project, String name, String desc, String image,
            ProtocolEnum protocol, String port, List<Parameter> parameters) {
        SutSpecification sut = new SutSpecification();
        sut.setSutType(SutTypeEnum.MANAGED);
        sut.setManagedDockerType(ManagedDockerType.IMAGE);

        sut.setProject(project);
        sut.setName(name);
        sut.setDescription(desc);
        sut.setSpecification(image);

        if (protocol != null) {
            sut.setProtocol(protocol);
        }

        if (port != null && !"".equals(port)) {
            sut.setPort(port);
        }

        if (parameters != null) {
            sut.setParameters(parameters);
        }

        return this.createSut(sut);
    }

    public SutSpecification createSutDeployedByElastestWithCompose(
            Project project, String name, String desc, String compose,
            String mainServiceName, ProtocolEnum protocol, String port,
            List<Parameter> parameters) {
        SutSpecification sut = new SutSpecification();
        sut.setSutType(SutTypeEnum.MANAGED);
        sut.setManagedDockerType(ManagedDockerType.COMPOSE);

        sut.setProject(project);
        sut.setName(name);
        sut.setDescription(desc);
        sut.setSpecification(compose);
        sut.setMainService(mainServiceName);

        if (protocol != null) {
            sut.setProtocol(protocol);
        }

        if (port != null && !"".equals(port)) {
            sut.setPort(port);
        }

        if (parameters != null) {
            sut.setParameters(parameters);
        }

        return this.createSut(sut);
    }

    public SutSpecification createSut(SutSpecification sut) {
        return this.sutService.createSutSpecification(sut);
    }

    /* **************** */
    /* *** Services *** */
    /* **************** */

    public ProjectService getProjectService() {
        return projectService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public TJobService gettJobService() {
        return tJobService;
    }

    public void settJobService(TJobService tJobService) {
        this.tJobService = tJobService;
    }

    public SutService getSutService() {
        return sutService;
    }

    public void setSutService(SutService sutService) {
        this.sutService = sutService;
    }

    public EsmService getEsmService() {
        return esmService;
    }

    public void setEsmService(EsmService esmService) {
        this.esmService = esmService;
    }

}
