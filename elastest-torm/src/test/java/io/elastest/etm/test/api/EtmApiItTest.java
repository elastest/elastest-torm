package io.elastest.etm.test.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.InstrumentedByEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;

public class EtmApiItTest {

    private static final Logger log = LoggerFactory
            .getLogger(EtmApiItTest.class);

    @Autowired
    TestRestTemplate httpClient;

    @LocalServerPort
    int serverPort;

    protected String baseUrl() {
        return "http://localhost:" + serverPort;
    }

    /* *************** */
    /* *** Project *** */
    /* *************** */

    protected Project createProject(String projectName) {

        String requestJson = "{ \"id\": 0,\"name\": \"" + projectName + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(requestJson,
                headers);

        log.info("POST /project");
        ResponseEntity<Project> response = httpClient
                .postForEntity("/api/project", entity, Project.class);

        return response.getBody();

    }

    protected void deleteProject(Long projectToDeleteId) {

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("projectId", projectToDeleteId);

        log.info("DELETE /project");
        ResponseEntity<Long> response = httpClient.exchange(
                "/api/project/{projectId}", HttpMethod.DELETE, null, Long.class,
                urlParams);
        log.info("Deleted project:" + response.getBody());

    }

    /* ************ */
    /* *** TJob *** */
    /* ************ */

    protected TJob getSampleTJob(long projectId, long sutId) {
        Project project = new Project();
        project.setId(projectId);

        Parameter param = new Parameter();
        param.setName("Param1");
        param.setValue("Value1");

        TJob tJob = new TJob();
        tJob.setId(new Long(0));
        tJob.setName("testApp1");
        tJob.setImageName("elastest/test-etm-test1");
        tJob.setParameters(Arrays.asList(param));
        tJob.setResultsPath("/app1TestJobsJenkins/target/surefire-reports/");
        tJob.setProject(project);

        if (sutId > -1) {
            SutSpecification sut = new SutSpecification();
            sut.setId(sutId);
            tJob.setSut(sut);
        }

        tJob.setSelectedServices("[]");

        return tJob;
    }

    protected TJob createTJob(long projectId) throws JsonProcessingException {
        return createTJob(projectId, -1);
    }

    protected ResponseEntity<TJob> createTJobByGiven(TJob tJob)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        String requestJson = mapper.writeValueAsString(tJob);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(requestJson,
                headers);

        log.info("POST /api/tjob");
        ResponseEntity<TJob> response = httpClient.postForEntity("/api/tjob",
                entity, TJob.class);

        return response;
    }

    protected TJob createTJob(long projectId, long sutId)
            throws JsonProcessingException {
        TJob tJob = this.getSampleTJob(projectId, sutId);
        ResponseEntity<TJob> response = createTJobByGiven(tJob);
        log.info("TJob creation response: " + response);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Error creating TJob: " + response);
        }

        return response.getBody();

    }

    protected void modifyTJob(TJob tJob) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        String requestJson = mapper.writeValueAsString(tJob);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(requestJson,
                headers);

        log.info("PUT /api/tjob");
        httpClient.put("/api/tjob", entity, TJob.class);
    }

    protected void deleteTJob(Long tJobId) {

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("tjobId", tJobId);

        log.info("DELETE /api/tjob/{tjobId");
        ResponseEntity<Long> response = httpClient.exchange(
                "/api/tjob/{tjobId}", HttpMethod.DELETE, null, Long.class,
                urlParams);
        log.info("Deleted tjob:" + response.getBody().longValue());

    }

    protected TJob getTJobById(Long tJobId) {

        log.info("Start the method getTJobById");

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("tJobId", tJobId);

        log.info("GET /api/tjob/{tJobId}");
        ResponseEntity<TJob> response = httpClient
                .getForEntity("/api/tjob/{tJobId}", TJob.class, urlParams);

        return response.getBody();

    }

    /* **************** */
    /* *** TJobExec *** */
    /* **************** */

    protected void deleteTJobExecution(Long tJobExecId, Long tJobId) {

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("tJobExecId", tJobExecId);
        urlParams.put("tJobId", tJobId);

        log.info("DELETE /api/tjob/{tJobId}/exec/{tJobExecId}");
        ResponseEntity<Long> response = httpClient.exchange(
                "/api/tjob/{tJobId}/exec/{tJobExecId}", HttpMethod.DELETE, null,
                Long.class, urlParams);
        log.info("Deleted tJobExec:" + response.getBody().longValue());

    }

    protected ResponseEntity<TJobExecution> getTJobExecutionById(
            Long tJobExecId, Long tJobId) {

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("tJobExecId", tJobExecId);
        urlParams.put("tJobId", tJobId);

        log.info("GET /api/tjob/{tJobId}/exec/{tJobExecId}");
        ResponseEntity<TJobExecution> response = httpClient.getForEntity(
                "/api/tjob/{tJobId}/exec/{tJobExecId}", TJobExecution.class,
                urlParams);

        return response;
    }

    /* *********** */
    /* *** SuT *** */
    /* *********** */

    protected ResponseEntity<SutSpecification> createSutByGiven(
            SutSpecification sut) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        String requestJson = mapper.writeValueAsString(sut);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(requestJson,
                headers);

        log.info("POST /api/sut");
        ResponseEntity<SutSpecification> response = httpClient
                .postForEntity("/api/sut", entity, SutSpecification.class);
        log.info("Sut created:" + response.getBody());

        return response;
    }

    protected SutSpecification createSut(long projectId)
            throws JsonProcessingException {
        SutSpecification sut = this.getSampleSut(projectId);
        ResponseEntity<SutSpecification> response = createSutByGiven(sut);
        log.info("Sut creation response: " + response);

        return response.getBody();
    }

    protected void modifySutByGiven(SutSpecification sut)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        String requestJson = mapper.writeValueAsString(sut);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(requestJson,
                headers);

        log.info("PUT /api/sut");
        httpClient.put("/api/sut", entity, SutSpecification.class);
    }

    protected SutSpecification getSampleSut(long projectId) {
        Project project = new Project();
        project.setId(projectId);

        SutSpecification sut = new SutSpecification();
        sut.setId(new Long(0));
        sut.setName("sut_definition_1");
        sut.setDescription("This is a SuT description example");
        sut.setProject(project);
        sut.setSpecification("https://github.com/EduJGURJC/springbootdemo");
        sut.setSutType(SutTypeEnum.REPOSITORY);
        sut.setInstrumentalize(false);
        sut.setCurrentSutExec(null);
        sut.setInstrumentedBy(InstrumentedByEnum.WITHOUT);
        sut.setPort(null);
        sut.setManagedDockerType(ManagedDockerType.IMAGE);
        sut.setCommandsOption(CommandsOptionEnum.DEFAULT);
        return sut;
    }

    protected Long deleteSut(Long sutId) {

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("sutId", sutId);

        log.info("DELETE /api/sut/{sutId}");
        ResponseEntity<Long> response = httpClient.exchange("/api/sut/{sutId}",
                HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted sutSpecification:" + response.getBody());

        return response.getBody();

    }

}
