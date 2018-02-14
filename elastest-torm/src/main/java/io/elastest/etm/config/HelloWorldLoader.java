package io.elastest.etm.config;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import io.elastest.etm.model.Project;
import io.elastest.etm.model.TJob;
import io.elastest.etm.service.ProjectService;
import io.elastest.etm.service.TJobService;

@Service
public class HelloWorldLoader {

    private ProjectService projectService;
    private TJobService tJobService;
    private static final String PROJECT_NAME = "Hello World";
    private static final String TJOB_NAME = "My first TJob";
    private static final String TEST_RESULTS_PATH = "/demo-projects/unit-java-test/target/surefire-reports/";
    private static final String BASE_IMAGE = "elastest/test-etm-alpinegitjava";
    private static final String TJOB_COMMANDS = "git clone https://github.com/elastest/demo-projects\ncd demo-projects/unit-java-test\nmvn -B test\n";
    private static final String EXEC_DASHBOARD_CONFIG = "{\"showComplexMetrics\":true,\"allMetricsFields\":{\"fieldsList\":[{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_cpu_totalUsage\",\"activated\":false,\"type\":\"cpu\",\"subtype\":\"totalUsage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_usage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"usage\",\"unit\":\"percent\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_memory_maxUsage\",\"activated\":false,\"type\":\"memory\",\"subtype\":\"maxUsage\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_read_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"read_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_write_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"write_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_blkio_total_ps\",\"activated\":false,\"type\":\"blkio\",\"subtype\":\"total_ps\",\"unit\":\"bytes\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_rxPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"rxPackets_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txBytes_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txBytes_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txErrors_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txErrors_ps\",\"unit\":\"amount/sec\"},{\"component\":\"\",\"stream\":\"et_dockbeat\",\"streamType\":\"composed_metrics\",\"name\":\"et_dockbeat_net_txPackets_ps\",\"activated\":false,\"type\":\"net\",\"subtype\":\"txPackets_ps\",\"unit\":\"amount/sec\"}]},\"allLogsTypes\":{\"logsList\":[{\"component\":\"test\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"test_default_log_log\",\"activated\":true},{\"component\":\"sut\",\"stream\":\"default_log\",\"streamType\":\"log\",\"name\":\"sut_default_log_log\",\"activated\":false}]}}";

    public HelloWorldLoader(ProjectService projectService,
            TJobService tJobService) {
        this.projectService = projectService;
        this.tJobService = tJobService;
    }

    @PostConstruct
    public void createHelloWorldProject() {

        if (projectService.getProjectByName(PROJECT_NAME) == null) {
            // Create Hello World Project
            Project project = new Project();
            project.setName(PROJECT_NAME);
            project = projectService.createProject(project);

            // Create Hello World TJob associated with the Hellow project
            TJob tJob = new TJob();
            tJob.setProject(project);
            tJob.setName(TJOB_NAME);
            tJob.setResultsPath(TEST_RESULTS_PATH);
            tJob.setImageName(BASE_IMAGE);
            tJob.setCommands(TJOB_COMMANDS);
            tJob.setExecDashboardConfigPath(EXEC_DASHBOARD_CONFIG);

            tJobService.createTJob(tJob);
        }
    }

    public ProjectService getProjectService() {
        return projectService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public TJobService getTJobService() {
        return tJobService;
    }

    public void setTJobService(TJobService tJobService) {
        this.tJobService = tJobService;
    }

}
