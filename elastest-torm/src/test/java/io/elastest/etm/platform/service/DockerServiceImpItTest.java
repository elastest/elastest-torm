package io.elastest.etm.platform.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutExecution.DeployStatusEnum;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobExecution.TypeEnum;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.service.EtPluginsService;
import io.elastest.etm.service.ProjectService;
import io.elastest.etm.service.SutService;
import io.elastest.etm.service.TJobService;
import io.elastest.etm.test.api.EtmApiItTest;
import io.elastest.etm.utils.UtilTools;

@TestInstance(Lifecycle.PER_CLASS)
public class DockerServiceImpItTest extends EtmApiItTest {
    static final Logger log = getLogger(lookup().lookupClass());

    @Value("${et.shared.folder}")
    private String sharedFolder;

    @Autowired
    PlatformService platformService;
    @Autowired
    TJobService tJobService;
    @Autowired
    ProjectService projectService;
    @Autowired
    SutService sutService;
    @Autowired
    TJobExecRepository tJobExecRepositoryImpl;
    @Autowired
    EsmService esmService;
    @Autowired
    EtPluginsService etPluginService;

    Project project;
    Execution execution;
    String tssDummyYml = "version: '2.1'\n" + "services:\n" + "   dummy-tss:\n"
            + "      image: elastest/etm-dummy-tss\n" + "      environment:\n"
            + "         - USE_TORM=true\n" + "      expose:\n"
            + "         - 8095\n" + "      networks:\n"
            + "         - elastest_elastest\n" + "      volumes:\n"
            + "         - /var/run/docker.sock:/var/run/docker.sock\n"
            + "         - ${ET_DATA_IN_HOST}:${ET_SHARED_FOLDER}\n"
            + "      labels:\n" + "         - io.elastest.type=tss\n"
            + "         - io.elastest.tjob.tss.id=dummy-tss\n"
            + "         - io.elastest.tjob.tss.type=main\n" + "networks:\n"
            + "  elastest_elastest:\n" + "    external: true";
    String serviceId = "873f23e8-256d-11e9-ab14-d663bd873d93";
    String instanceId;
    String path;
    String tmpTssInstancesYmlFolder;
    String elastestNetwork = "elastest_elastest";

    @BeforeAll
    public void setUp() {
        log.info("*** Starting initial test configuration ***");
        project = new Project();
        project.setName("Test project");
        project.setId(new Long(0));
        log.info("* Project Id: {} *", project.getId());
        path = sharedFolder.endsWith("/") ? sharedFolder : sharedFolder + "/";
        tmpTssInstancesYmlFolder = path + "tmp-support-services-yml";
    }

    @BeforeEach
    @Transactional
    public void setUpTest(TestInfo testInfo) {
        log.info("* Initial configuration for the test {} *",
                testInfo.getTestMethod().get().getName());
        project = projectService.saveProject(project);
        execution = new Execution();
    }

    @AfterEach
    @Transactional
    public void reset() {
        log.info("* Clean environment by test *");
        try {
            projectService.deleteProject(project.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        execution = null;
    }

    @AfterAll
    public void deleteAll() {
        log.info("*** Clean test environment at the end *** ");
    }

    @Test
    @Transactional
    public void testDeploySut() throws Exception {
        log.info("Start the test to check a sut deploy");
        execution = prepareTJobEnvironment("elastest/etm-dummy-tss", "8095",
                "sutFromImage", null, null);
        platformService.deploySut(execution);
        assertNotNull(execution.getSutExec().getIp());
    }

    @Test
    @Transactional
    public void testUndeploySut() throws Exception {
        log.info("Starting test to check a sut undeployment");
        execution = prepareTJobEnvironment("elastest/etm-dummy-tss", "8095",
                "sutFromImage", null, null);
        platformService.deploySut(execution);
        platformService.undeploySut(execution, true);
        assertEquals(DeployStatusEnum.UNDEPLOYED,
                execution.getSutExec().getDeployStatus());
    }

    @Test
    @Transactional
    @Disabled
    public void testDeployService() throws Exception {
        log.info("Start the test to check a TSS deployment");
        prepareTssEnvironment();
        assertTrue(platformService.deployService(instanceId, true));
        platformService.undeployService(instanceId);
    }

    @Test
    @Transactional
    @Disabled
    public void testUndeployService() throws Exception {
        log.info("Start the test to check a TSS undeployment");
        prepareTssEnvironment();
        platformService.deployService(instanceId, true);
        assertTrue(platformService.undeployService(instanceId));
    }

    @Test
    @Transactional
    @Disabled
    public void testDeployAndRunTJobExecution() throws Exception {
        log.info("Start the test to check a TJob deployment");
        execution = prepareTJobEnvironment(null, null, null,
                "elastest/dummy-tjob-simple", null);
        execution.gettJob().setResultsPath(null);
        execution.gettJob().setCommands(null);
        platformService.deployAndRunTJobExecution(execution);
        assertEquals(ResultEnum.SUCCESS, tJobService
                .getTJobExecById(execution.getTJobExec().getId()).getResult());
    }

    @Test
    @Transactional
    @Disabled
    public void testGetServiceDeploymentImages() throws Exception {
        log.info(
                "Start the test to check if the images associated to a service are retrived");
        deployDummyTSS();
        assertEquals("elastest/etm-dummy-tss",
                platformService.getServiceDeploymentImages(instanceId).get(0));
        platformService.undeployService(instanceId);
    }

    @Test
    @Transactional
    @Disabled
    public void testGetContainers() throws Exception {
        log.info("Start the test to retrive containers by project name");
        deployDummyTSS();
        DockerContainerInfo containerInfo = platformService
                .getContainers(instanceId);
        assertEquals(instanceId + "_dummy-tss_1",
                containerInfo.getContainers().get(0).getName());
        platformService.undeployService(instanceId);
    }

    @Test
    @Transactional
    @Disabled
    public void testIsContainerIntoNetwork() throws Exception {
        log.info(
                "Start the test to check if a container is within a specific network");
        deployDummyTSS();
        DockerContainerInfo containerInfo = platformService
                .getContainers(instanceId);
        assertTrue(platformService.isContainerIntoNetwork(elastestNetwork,
                containerInfo.getContainers().get(0).getName()));
        platformService.undeployService(instanceId);
    }

    @Test
    @Transactional
    @Disabled
    public void testGetContainerIpByNetwork() throws Exception {
        log.info("Start the test to retrive the ip of a containers by network");
        deployDummyTSS();
        DockerContainerInfo containerInfo = platformService
                .getContainers(instanceId);
        String ip = platformService.getContainerIpByNetwork(
                containerInfo.getContainers().get(0).getName(),
                elastestNetwork);
        log.info("Container ip: {}", ip);
        assertNotNull(ip);
        platformService.undeployService(instanceId);
    }

    @Test
    @Transactional
    public void testInsertIntoETNetwork() throws Exception {

    }

    @Test
    @Transactional
    @Disabled
    public void testGetContainerName() throws Exception {
        log.info(
                "Start the test to retrive the name of a container by service name and network");
        deployDummyTSS();
        String containerName = platformService.getContainerName("dummy-tss",
                elastestNetwork);
        assertEquals(instanceId + "_dummy-tss_1", containerName.split("/")[1]);
        platformService.undeployService(instanceId);
    }

    @Test
    @Transactional
    public void testEnableServiceMetricMonitoring() throws Exception {
        log.info("Start the test to check the enablement of monitoring");
        execution = prepareTJobEnvironment(null, null, null,
                "elastest/dummy-tjob-simple", null);
        execution.gettJob().setResultsPath(null);
        execution.gettJob().setCommands(null);
        platformService.enableServiceMetricMonitoring(execution);
        assertEquals("elastest_dockbeat_" + execution.getExecutionId(),
                platformService.getContainers("elastest_dockbeat")
                        .getContainers().get(0).getName());
    }

    @Test
    @Transactional
    public void testDisableMetricMonitoring() throws Exception {
        log.info("Start the test to check that the monitor is disabled");
        execution = prepareTJobEnvironment(null, null, null,
                "elastest/dummy-tjob-simple", null);
        execution.gettJob().setResultsPath(null);
        execution.gettJob().setCommands(null);
        platformService.enableServiceMetricMonitoring(execution);
        Thread.sleep(3000);
        platformService.disableMetricMonitoring(execution, true);
        assertEquals(0, platformService.getContainers("elastest_dockbeat")
                        .getContainers().size());
    }

    private void deployDummyTSS() throws Exception {
        prepareTssEnvironment();
        platformService.deployService(instanceId, true);
    }

    private void prepareTssEnvironment() throws Exception {
        instanceId = UtilTools.generateUniqueId();
        SupportServiceInstance supportServiceInstance = esmService
                .createNewServiceInstance(serviceId, null, serviceId);
        platformService.createServiceDeploymentProject(instanceId, tssDummyYml,
                tmpTssInstancesYmlFolder, true,
                supportServiceInstance.getParameters(), false, false);
    }

    private Execution prepareTJobEnvironment(String sutImage, String sutPort,
            String sutType, String tJobImage, String tssImage) {
        TJob tJob = null;
        TJobExecution tJobExec = null;
        SutExecution sutExec = null;
        SutSpecification sutSpec = null;
        if (sutType != null && !sutType.isEmpty()) {
            sutSpec = setUpSut(sutImage, sutType, sutPort);
            log.info("Creating SutExecution");
            sutExec = sutService.createSutExecutionBySut(sutSpec);
        }

        tJob = setUpTJob(tJobImage, sutSpec);
        tJobExec = setUpTJobExecution(tJob, sutExec);
        execution.setSutExec(tJobExec.getSutExecution());
        execution.setSut(tJobExec.getSutExecution() != null
                ? tJobExec.getSutExecution().getSutSpecification()
                : null);
        execution.settJob(tJobExec.getTjob());
        execution.setTJobExec(tJobExec);
        return execution;
    }

    private SutSpecification setUpSut(String sutImage, String sutSpecification,
            String port) {
        log.info("Creating sut");
        SutSpecification sut = sutExamples.get(sutSpecification);
        sut.setParameters(new ArrayList<Parameter>());
        sut.setProject(project);
        if (sutImage != null && !sutImage.isEmpty()) {
            sut.setManagedDockerType(ManagedDockerType.IMAGE);
            sut.setSpecification(sutImage);
            sut.setCommands(null);
        }

        sut.setPort((port != null && !port.isEmpty() ? port : sut.getPort()));
        sut = sutService.prepareSutToSave(sut);
        return sut;
    }

    private TJob setUpTJob(String imageName, SutSpecification sut) {
        log.info("Creating TJob");
        TJob tJob = getSampleTJob(project.getId());
        if (imageName != null && !imageName.isEmpty()) {
            log.info("Job image to use: {}", imageName);
            tJob.setImageName(imageName);
        }
        if (sut != null) {
            tJob.setSut(sut);
        }
        tJob = tJobService.createTJob(tJob);
        return tJob;
    }

    private TJobExecution setUpTJobExecution(TJob tJob, SutExecution sutExec) {
        log.info("Creating TJobExecution");
        TJobExecution tJobExec = new TJobExecution();
        tJobExec.setStartDate(new Date());
        tJobExec.setTjob(tJob);
        tJobExec.setType(TypeEnum.SIMPLE);
        tJobExec.setSutExecution(sutExec);
        List<Parameter> parameters = new ArrayList<Parameter>();
        tJobExec.setParameters(parameters);
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);
        return tJobExec;
    }

}
