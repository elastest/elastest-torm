package io.elastest.etm.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.InstrumentedByEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class SutApiItTest extends EtmApiItTest {

    static final Logger log = LoggerFactory.getLogger(SutApiItTest.class);

    @LocalServerPort
    int serverPort;

    @Autowired
    TestRestTemplate httpClient;

    long projectId;

    @BeforeEach
    void setup() {
        log.info("App started on port {}", serverPort);
        projectId = createProject("Project").getId();
    }

    @AfterEach
    void reset() {
        deleteProject(projectId);
    }

    @Test
    public void testCreateSut() throws JsonProcessingException {
        log.info("Start the test testCreateSut");

        SutSpecification sut = getSampleSut(projectId);

        ResponseEntity<SutSpecification> response = createSutByGiven(sut);
        log.info("Sut creation response: " + response);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        deleteSut(response.getBody().getId());

        assertAll("Validating sutSpecification Properties",
                () -> assertTrue(response.getBody().getName()
                        .equals("sut_definition_1")),
                () -> assertNotNull(response.getBody().getId()),
                () -> assertTrue(response.getBody().getId() > 0));
    }

    @Test
    public void testModifySut() throws JsonProcessingException {
        log.info("Start the test testModifySut");

        SutSpecification sut = createSut(projectId);
        sut.setName("sut_definition_2");

        log.info("Sut to modify:" + sut.toString());
        modifySutByGiven(sut);
        SutSpecification modifiedSut = getSutById(sut.getId());

        deleteSut(sut.getId());

        assertAll("Validating sutSpecification Properties",
                () -> assertTrue(
                        modifiedSut.getName().equals("sut_definition_2")),
                () -> assertNotNull(modifiedSut.getId()),
                () -> assertTrue(modifiedSut.getId() > 0));
    }

    @Test
    public void testGetSuts() throws JsonProcessingException {
        log.info("Start the test testGetSuts");

        List<SutSpecification> tSutsToGet = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            tSutsToGet.add(createSut(projectId));
        }

        log.debug("GET /api/sut");
        ResponseEntity<SutSpecification[]> response = httpClient
                .getForEntity("/api/sut", SutSpecification[].class);
        SutSpecification[] suts = response.getBody();

        for (SutSpecification sut : suts) {
            deleteSut(sut.getId());
        }

        log.info("Suts Array size:" + suts.length);
        assertTrue(suts.length > 0);
    }

    @Test
    public void testDeleteSut() throws JsonProcessingException {
        log.info("Start the test testDeleteSut");

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("sutId", createSut(projectId).getId());

        log.info("DELETE /api/sut/{sutId}");
        ResponseEntity<Long> response = httpClient.exchange("/api/sut/{sutId}",
                HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted sut:" + response.getBody().longValue());

        assertTrue(response.getBody().longValue() == urlParams.get("sutId"));
    }

    @Test
    public void testCreateSutWithCommandsContainer()
            throws JsonProcessingException {
        log.info("Start the test testCreateSutWithCommandsContainer");

        Project project = new Project();
        project.setId(projectId);
        String name = "sut_definition_Webapp";

        SutSpecification sut = new SutSpecification();
        sut.setId(new Long(0));
        sut.setName(name);
        sut.setDescription("Webapp Description");
        sut.setProject(project);
        sut.setSutType(SutTypeEnum.MANAGED);
        sut.setManagedDockerType(ManagedDockerType.IMAGE);
        sut.setSpecification("elastest/demo-web-java-test-sut");
        sut.setCommandsOption(CommandsOptionEnum.DEFAULT);
        sut.setInstrumentalize(false);
        sut.setInstrumentalized(false);
        sut.setCurrentSutExec(null);
        sut.setInstrumentedBy(InstrumentedByEnum.WITHOUT);
        sut.setProtocol(ProtocolEnum.HTTP);
        sut.setPort("8080");

        ResponseEntity<SutSpecification> response = createSutByGiven(sut);
        log.info("Sut creation response: " + response);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        deleteSut(response.getBody().getId());

        assertAll("Validating sutSpecification Properties",
                () -> assertTrue(response.getBody().getName().equals(name)),
                () -> assertNotNull(response.getBody().getId()),
                () -> assertTrue(response.getBody().getId() > 0));
    }
}
