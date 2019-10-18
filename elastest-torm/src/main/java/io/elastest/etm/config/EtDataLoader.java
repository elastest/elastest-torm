package io.elastest.etm.config;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionType;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.TestImportance;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestCaseStep;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import io.elastest.etm.model.EimBeatConfig;
import io.elastest.etm.model.EimConfig;
import io.elastest.etm.model.EimMonitoringConfig;
import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.model.MultiConfig;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.InstrumentedByEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.service.ProjectService;
import io.elastest.etm.service.SutService;
import io.elastest.etm.service.TJobService;
import io.elastest.etm.service.TSSService;
import io.elastest.etm.service.TestLinkService;

@Service
public class EtDataLoader {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.user}")
    private String etUser;

    private ProjectService projectService;
    private TJobService tJobService;
    private SutService sutService;
    private TSSService esmService;
    public TestLinkService testLinkService;

    public EtDataLoader(ProjectService projectService, TJobService tJobService,
            SutService sutService, TSSService esmService,
            TestLinkService testLinkService) {
        this.projectService = projectService;
        this.tJobService = tJobService;
        this.sutService = sutService;
        this.esmService = esmService;
        this.testLinkService = testLinkService;
    }

    public void printLog(String pjName) {
        logger.debug("Creating '{}' Sample Data...", pjName);
    }

    /* ********************************************************************** */
    /* ************************** Creation Methods ************************** */
    /* ********************************************************************** */

    /* *** Project *** */
    public boolean projectExists(String name) {
        return getProject(name) != null;
    }

    public Project getProject(String name) {
        return projectService.getProjectByName(name);
    }

    public Project createProject(String projectName) {
        Project project = new Project();
        project.setName(projectName);
        return this.createProject(project);
    }

    public Project createProject(Project project) {
        return projectService.saveProject(project);
    }

    /* ************ */
    /* *** TJob *** */
    /* ************ */

    public TJob createTJob(Project project, String name, String resultsPath,
            String image, boolean useImageCommands, String commands,
            String execDashboardConfig, List<Parameter> parameters,
            List<String> activatedTSSList, SutSpecification sut,
            List<MultiConfig> multiConfigurations) {
        TJob tJob = new TJob();
        tJob.setMaxExecutions(new Long(15));
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

        if (multiConfigurations != null) {
            tJob.setMultiConfigurations(multiConfigurations);
            tJob.setMulti(true);
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

    /* ********************* */
    /* ******** Sut ******** */
    /* ********************* */

    public boolean sutExistsInProject(String name, Project project) {
        return sutService.getSutsByNameAndProject(name, project).size() > 0;
    }

    public SutSpecification getSut(String name, Project project) {
        List<SutSpecification> suts = sutService.getSutsByNameAndProject(name,
                project);
        if (suts.size() > 0) {
            return suts.get(0);
        } else {
            return null;
        }
    }

    private SutSpecification initCommonSutFields(SutSpecification sut,
            Project project, ExternalProject exProject, String name,
            String desc, String specification, String port,
            ProtocolEnum protocol, List<Parameter> parameters) {

        if (project != null) {
            sut.setProject(project);
        } else if (exProject != null) {
            sut.setExProject(exProject);
        }

        sut.setName(name);
        sut.setDescription(desc);
        sut.setSpecification(specification);

        if (protocol != null) {
            sut.setProtocol(protocol);
        }

        if (port != null && !"".equals(port)) {
            sut.setPort(port);
        }

        if (parameters != null) {
            sut.setParameters(parameters);
        }

        // Default (cannot be null)
        sut.setManagedDockerType(ManagedDockerType.COMMANDS);

        return sut;
    }

    public SutSpecification createSut(SutSpecification sut) {
        return this.sutService.createSutSpecification(sut);
    }

    public SutSpecification createSutDeployedByElastestWithCommands(
            Project project, ExternalProject exProject, String name,
            String desc, String image, String commands,
            CommandsOptionEnum commandsOption, ProtocolEnum protocol,
            String port, List<Parameter> parameters) {
        SutSpecification sut = new SutSpecification();
        sut = this.initCommonSutFields(sut, project, exProject, name, desc,
                image, port, protocol, parameters);

        sut.setSutType(SutTypeEnum.MANAGED);
        sut.setManagedDockerType(ManagedDockerType.COMMANDS);

        sut.setCommands(commands);
        sut.setCommandsOption(commandsOption);

        return this.createSut(sut);
    }

    public SutSpecification createSutDeployedByElastestWithDockerImage(
            Project project, ExternalProject exProject, String name,
            String desc, String image, ProtocolEnum protocol, String port,
            List<Parameter> parameters) {
        SutSpecification sut = new SutSpecification();
        sut = this.initCommonSutFields(sut, project, exProject, name, desc,
                image, port, protocol, parameters);

        sut.setSutType(SutTypeEnum.MANAGED);
        sut.setManagedDockerType(ManagedDockerType.IMAGE);

        return this.createSut(sut);
    }

    public SutSpecification createSutDeployedByElastestWithCompose(
            Project project, ExternalProject exProject, String name,
            String desc, String compose, String mainServiceName,
            ProtocolEnum protocol, String port, List<Parameter> parameters) {
        SutSpecification sut = new SutSpecification();
        sut = this.initCommonSutFields(sut, project, exProject, name, desc,
                compose, port, protocol, parameters);

        sut.setSutType(SutTypeEnum.MANAGED);
        sut.setManagedDockerType(ManagedDockerType.COMPOSE);

        sut.setMainService(mainServiceName);

        return this.createSut(sut);
    }

    public SutSpecification createSutDeployedOutsideAndInstrumentedByElastest(
            Project project, ExternalProject exProject, String name,
            String desc, String ip, ProtocolEnum protocol, String port,
            List<Parameter> parameters, String user, String password,
            String privateKey, List<String> logPaths,
            List<String> dockerizedLogPaths,
            List<String> dockerizedDockersockPaths, boolean dockerized) {
        SutSpecification sut = new SutSpecification();
        sut = this.initCommonSutFields(sut, project, exProject, name, desc, ip,
                port, protocol, parameters);

        sut.setSutType(SutTypeEnum.DEPLOYED);
        sut.setInstrumentedBy(InstrumentedByEnum.ELASTEST);

        // Eim config
        EimConfig eimConfig = new EimConfig(null, user, password, privateKey,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null);
        sut.setEimConfig(eimConfig);

        // Eim monitoring config
        if (dockerizedLogPaths == null || dockerizedLogPaths.size() == 0) {
            dockerizedLogPaths = Arrays.asList("/var/lib/docker/containers/");
        }

        if (dockerizedDockersockPaths == null
                || dockerizedDockersockPaths.size() == 0) {
            dockerizedDockersockPaths = Arrays.asList("/var/run/docker.sock");
        }

        Map<String, EimBeatConfig> beats = new HashMap<String, EimBeatConfig>();
        beats.put("packetbeat",
                new EimBeatConfig("packetbeat", "et_packetbeat", null, null));
        beats.put("filebeat", new EimBeatConfig("filebeat", "default_log",
                logPaths, dockerizedLogPaths));
        beats.put("metricbeat", new EimBeatConfig("metricbeat", "et_metricbeat",
                null, dockerizedDockersockPaths));

        EimMonitoringConfig eimMonitoringConfig = new EimMonitoringConfig(null,
                null, "sut", dockerized, beats);
        sut.setEimMonitoringConfig(eimMonitoringConfig);

        sut.setInstrumentalize(false);

        return this.createSut(sut);
    }

    /* ********************************************************************* */
    /* ********************* TestLink Creation Methods ********************* */
    /* ********************************************************************* */

    /* *** Project *** */

    public boolean tlProjectExists(String name) {
        return testLinkService.projectExistsByName(name);
    }

    public TestProject createTlTestProject(String name, String prefix,
            String notes) {
        return this.createTlTestProject(0, name, prefix, notes);
    }

    public TestProject createTlTestProject(Integer id, String name,
            String prefix, String notes) {
        TestProject project = new TestProject(id, name, prefix, notes, false,
                false, false, false, true, true);
        return this.createTlTestProject(project);
    }

    public TestProject createTlTestProject(TestProject project) {
        return testLinkService.createProject(project);
    }

    /* *** Suite *** */
    public TestSuite createTlTestSuite(Integer projectId, String name,
            String details) {
        return this.createTlTestSuite(0, projectId, name, details);
    }

    public TestSuite createTlTestSuite(Integer id, Integer projectId,
            String name, String details) {
        TestSuite suite = new TestSuite(id, projectId, name, details, null,
                null, true, ActionOnDuplicate.BLOCK);
        return this.createTlTestSuite(suite);
    }

    public TestSuite createTlTestSuite(TestSuite suite) {
        return testLinkService.createTestSuite(suite);
    }

    /* *** Plan *** */

    public TestPlan createTlTestPlan(String name, String projectName,
            String notes) {
        return this.createTlTestPlan(0, name, projectName, notes);
    }

    public TestPlan createTlTestPlan(Integer id, String name,
            String projectName, String notes) {
        TestPlan plan = new TestPlan(id, name, projectName, notes, true, true);
        return this.createTlTestPlan(plan);
    }

    public TestPlan createTlTestPlan(TestPlan plan) {
        return testLinkService.createTestPlan(plan);
    }

    /* *** Build *** */

    public Build createTlBuild(Integer testPlanId, String name, String notes) {
        return this.createTlBuild(0, testPlanId, name, notes);
    }

    public Build createTlBuild(Integer id, Integer testPlanId, String name,
            String notes) {
        Build build = new Build(0, testPlanId, name, notes);
        return this.createTlBuild(build);
    }

    public Build createTlBuild(Build build) {
        return testLinkService.createBuild(build);
    }

    /* *** Test Case *** */

    public TestCase createTlTestCase(TestCase tCase) {
        if (tCase.getId() == null) {
            tCase.setId(0);
        }
        tCase.setTestCaseStatus(TestCaseStatus.DRAFT);
        tCase.setAuthorLogin(etUser);
        tCase.setTestImportance(TestImportance.MEDIUM);
        tCase.setExecutionType(ExecutionType.MANUAL);
        // tCase.setOrder();
        // tCase.setInternalId();
        tCase.setCheckDuplicatedName(true);
        tCase.setActionOnDuplicatedName(ActionOnDuplicate.BLOCK);

        return testLinkService.createTestCase(tCase, tCase.getTestSuiteId());
    }

    public Integer addTlTestCaseToPlan(TestCase tCase, TestPlan plan) {
        return testLinkService.addTestCaseToTestPlan(tCase, plan.getId());
    }

    /* *** Test Case Step *** */
    public TestCaseStep getNewCaseStep(String actions, String expectedResults,
            Integer number) {
        TestCaseStep step = new TestCaseStep();
        step.setId(0);
        step.setActions(actions);
        step.setActive(true);
        step.setExpectedResults(expectedResults);
        step.setExecutionType(ExecutionType.MANUAL);
        // step.setTestCaseVersionId(testCaseVersionId);
        step.setNumber(number);
        return step;
    }

    public Map<String, Object> createTestCaseSteps(
            List<TestCaseStep> testCaseSteps, TestCase tCase) {
        return testLinkService.createTestCaseSteps(testCaseSteps, tCase);
    }

    public boolean syncTestLink() {
        return this.testLinkService.syncTestLink();
    }

    public ExternalProject getExternalProjectByTestProjectId(
            Integer projectId) {
        return testLinkService.getExternalProjectByTestProjectId(projectId);
    }

    public ExternalTJob getExternalTJobByPlanId(Integer planId) {
        return testLinkService.getExternalTJobByPlanId(planId);
    }

    public boolean isStartedTestLink() {
        return testLinkService.isStarted();
    }

    public boolean isReadyTestLink() {
        return testLinkService.isReady();
    }

}
