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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

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
public class DockerServiceImpItlTest extends EtmApiItTest {
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

    @BeforeAll
    public void setUp() {
        log.info("Starting initial test configuration");
        project = new Project();
        project.setName("Test project");
        project.setId(new Long(0));
        project = projectService.saveProject(project);
        instanceId = UtilTools.generateUniqueId();
        path = sharedFolder.endsWith("/") ? sharedFolder : sharedFolder + "/";
        tmpTssInstancesYmlFolder = path + "tmp-support-services-yml";
    }

    @BeforeEach
    public void setUpTest(TestInfo testInfo) {
        log.info("Initial configuration for the test {}",
                testInfo.getTestMethod().get().getName());
        execution = new Execution();
    }

    @AfterEach
    public void reset() {
        log.info("Clean environment by test");
        execution = null;
    }

    @AfterAll
    public void deleteAll() {
        log.info("Clean test environment");
    }

    @Test
    @Transactional
    public void testDeploySut() throws Exception {
        log.info("Starting test to check a sut deploy");
        execution = prepareTJobEnvironment("elastest/etm-dummy-tss", "8095",
                "sutFromImage", null, null);
        platformService.deploySut(execution);
        projectService.deleteProject(project.getId());
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
        projectService.deleteProject(project.getId());
        assertEquals(DeployStatusEnum.UNDEPLOYED,
                execution.getSutExec().getDeployStatus());
    }

    @Test
    @Transactional
    public void testDeployService() throws Exception {
        log.info("Starting test to check a TSS deployment");
        SupportServiceInstance supportServiceInstance = esmService
                .createNewServiceInstance(serviceId, null, serviceId);

        platformService.createServiceDeploymentProject(instanceId, tssDummyYml,
                tmpTssInstancesYmlFolder, true,
                supportServiceInstance.getParameters(), false, false);
        assertTrue(platformService.deployService(instanceId, true));
    }

    @Test
    @Transactional
    public void testUndeployService() throws Exception {
        log.info("Starting test to check a TSS undeployment");
        prepareTssEnvironment();
        assertTrue(platformService.deployService(instanceId, true));
    }

    @Test
    @Transactional
    public void testDeployAndRunTJobExecution() throws Exception {
        log.info("Starting test to check a TSS deployment");
        execution = prepareTJobEnvironment(null, null, null,
                "elastest/dummy-tjob-simple", null);
        execution.gettJob().setResultsPath(null);
        execution.gettJob().setCommands(null);
        platformService.deployAndRunTJobExecution(execution);
        projectService.deleteProject(project.getId());
        assertEquals(ResultEnum.SUCCESS, tJobService
                .getTJobExecById(execution.getTJobExec().getId()).getResult());
    }

    private void prepareTssEnvironment() throws Exception {
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
            log.info("*** Job image to use: {}", imageName);
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
