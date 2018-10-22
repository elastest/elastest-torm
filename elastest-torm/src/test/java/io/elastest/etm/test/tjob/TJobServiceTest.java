package io.elastest.etm.test.tjob;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.TJob;
import io.elastest.etm.service.DatabaseSessionManager;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.service.TJobExecOrchestratorService;
import io.elastest.etm.service.TJobService;
import io.elastest.etm.test.extensions.MockitoExtension;
import io.elastest.etm.utils.UtilsService;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class TJobServiceTest {

    private TJob tJob;
    
    @BeforeAll
    void setUp() {
        tJob = new TJob();
        tJob.setId(0L);
        tJob.setImageName("elastest/test-etm-test1");
        tJob.setName("SimpleTest");
        tJob.setResultsPath("/app1TestJobsJenkins/target/surefire-reports/");
        Project project = new Project();
        project.setId(1L);
        project.setName("TestProject1");
        tJob.setProject(project);
    }

    @Test
    public void createTJobTest(//@Autowired TJob tJob,
            @Mock TJobRepository tJobRepo,
            @Mock TJobExecOrchestratorService epmIntegrationService,
            @Mock TJobExecRepository tJobExecRepo, @Mock EsmService esmService,
            @Mock DatabaseSessionManager dbmanager,
            @Mock UtilsService utilsService) {
        // TJob createdTJob =
        when(tJobRepo.save(tJob)).thenReturn(tJob);
        TJobService tJobService = new TJobService(tJobRepo, tJobExecRepo,
                epmIntegrationService, esmService, dbmanager, utilsService);
        TJob tJob1 = tJobService.createTJob(tJob);
        System.out.println("ImageName:" + tJob1.getImageName());
        assertNotNull(tJob1.getId());
        assertAll("Validating Project Properties",
                () -> assertTrue(tJob1.getName().equals("SimpleTest")),
                () -> assertTrue(
                        tJob1.getProject().getName().equals("TestProject1")));
    }

}
