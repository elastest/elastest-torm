package io.elastest.etm.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pl.pojo.tester.api.FieldPredicate.exclude;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsForAll;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutExecution.DeployStatusEnum;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.InstrumentedByEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.service.DockerExecution;
import pl.pojo.tester.api.assertion.Method;

@RunWith(JUnitPlatform.class)
@SpringBootTest(properties = "io.reflectoring.scheduling.enabled=false")
public class ModelsTest {

    @Test
    public void tTobTest() {

        Project project = new Project(3l, "name", new ArrayList<TJob>(),
                new ArrayList<SutSpecification>());
        Project project2 = new Project(3l, "name", new ArrayList<TJob>(),
                new ArrayList<SutSpecification>());

        SutSpecification sut = new SutSpecification(34l, "name",
                "specification", "description", project, new ArrayList<>(),
                SutTypeEnum.REPOSITORY, false, false, null,
                InstrumentedByEnum.WITHOUT, null, ManagedDockerType.IMAGE,
                CommandsOptionEnum.DEFAULT, ProtocolEnum.HTTP);
        SutSpecification sut2 = new SutSpecification(34l, "name",
                "specification", "description", project2, new ArrayList<>(),
                SutTypeEnum.REPOSITORY, false, false, null,
                InstrumentedByEnum.WITHOUT, null, ManagedDockerType.IMAGE,
                CommandsOptionEnum.DEFAULT, ProtocolEnum.HTTP);

        TJob tjob = new TJob(34l, "name", "imageName", sut, project, false,
                "execDashboardConfig", null, 0l);
        TJob tjob2 = new TJob(34l, "name", "imageName", sut2, project2, false,
                "execDashboardConfig", null, 0l);

        assertEquals(tjob, tjob2);
        assertEquals(tjob.hashCode(), tjob2.hashCode());
    }

    @Test
    public void executionTest() {

        Project project = new Project(3l, "name", new ArrayList<TJob>(),
                new ArrayList<SutSpecification>());
        SutSpecification sut = new SutSpecification(34l, "name",
                "specification", "description", project, new ArrayList<>(),
                SutTypeEnum.REPOSITORY, false, false, null,
                InstrumentedByEnum.WITHOUT, null, ManagedDockerType.IMAGE,
                CommandsOptionEnum.DEFAULT, ProtocolEnum.HTTP);
        TJob tjob = new TJob(34l, "name", "imageName", sut, project, false,
                "execDashboardConfig", null, 0l);

        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter("param1", "value1"));
        tjob.setParameters(params);

        TJobExecution exec = new TJobExecution(45l, 34543534l,
                ResultEnum.SUCCESS);
        List<Parameter> paramsExec = new ArrayList<>();
        paramsExec.add(new Parameter("param1", "value1"));
        exec.setParameters(paramsExec);

        TJobExecution exec2 = new TJobExecution(45l, 34543534l,
                ResultEnum.SUCCESS);
        List<Parameter> paramsExec2 = new ArrayList<>();
        paramsExec2.add(new Parameter("param1", "value1"));
        exec2.setParameters(paramsExec2);

        exec.setTjob(tjob);
        exec2.setTjob(tjob);

        SutExecution sutExec = new SutExecution(45l, sut, "ssss",
                DeployStatusEnum.DEPLOYED);
        SutExecution sutExec2 = new SutExecution(45l, sut, "ssss",
                DeployStatusEnum.DEPLOYED);

        exec.setSutExecution(sutExec);
        exec2.setSutExecution(sutExec2);

        assertEquals(exec, exec2);
        assertEquals(exec.hashCode(), exec2.hashCode());
    }

    @Test
    public void testCreateExternalJob() {
        ExternalJob extJob = new ExternalJob("Job1", "htt://localhost:8090",
                "http://localhost:8090", 1L, "9200", "192.168.1.1", null, null,
                0, false, null, null, null, null, null, false, null, null,
                null);
        assertTrue(extJob.getJobName().equals("Job1"));
    }

    @Test
    public void testEqualsExternalJobs() {
        ExternalJob extJob1 = new ExternalJob("Job1", "htt://localhost:8090",
                "http://localhost:8090", 1L, "9200", "192.168.1.1", null, null,
                0, false, null, null, null, null, null, false, null, null,
                null);
        ExternalJob extJob2 = new ExternalJob("Job1", "htt://localhost:8090",
                "http://localhost:8090", 1L, "9200", "192.168.1.1", null, null,
                0, false, null, null, null, null, null, false, null, null,
                null);
        assertTrue(extJob1.equals(extJob2));
        assertEquals(extJob1.hashCode(), extJob2.hashCode());
    }

    @Test
    public void testCreateSutExecution() {
        SutExecution sutExec = new SutExecution(1L, new SutSpecification(),
                "http://localhost:8090", DeployStatusEnum.DEPLOYING);
        assertTrue(sutExec.getId() == 1L);
    }

    @Test
    public void testEqualsSutExecution() {
        SutExecution sutExec1 = new SutExecution(1L, new SutSpecification(),
                "http://localhost:8090", DeployStatusEnum.DEPLOYING);
        SutExecution sutExec2 = new SutExecution(1L, new SutSpecification(),
                "http://localhost:8090", DeployStatusEnum.DEPLOYING);
        assertTrue(sutExec1.equals(sutExec2));
        assertEquals(sutExec1.hashCode(), sutExec2.hashCode());

    }

    @Test
    public void testEqualsTestCases() {
        TestCase testCase1 = new TestCase("", 100L, "", "", "", "",
                new TestSuite());
        TestCase testCase2 = new TestCase("", 100L, "", "", "", "",
                new TestSuite());

        assertTrue(testCase1.equals(testCase2));
        assertEquals(testCase1, testCase2);
        assertEquals(testCase1.hashCode(), testCase2.hashCode());
    }

    @Test
    public void testCreateTestSuit() {
        TestSuite testSuite = new TestSuite(1L, "", 100L, 0, 0, 0, 0, 0,
                new ArrayList<TestCase>());
        assertTrue(testSuite.getId() == 1L);
    }

    @Test
    public void testEqualsTestSuites() {
        TestSuite testSuite1 = new TestSuite(1L, "", 100L, 0, 0, 0, 0, 0,
                new ArrayList<TestCase>());
        TestSuite testSuite2 = new TestSuite(1L, "", 100L, 0, 0, 0, 0, 0,
                new ArrayList<TestCase>());

        assertTrue(testSuite1.equals(testSuite2));
        assertEquals(testSuite1.hashCode(), testSuite2.hashCode());
    }

    @Test
    public void testPojoTestSuite() {
        final Class<?> pojoClass = TestSuite.class;
        // assertPojoMethodsFor(testCaseClass).areWellImplemented();

        assertPojoMethodsFor(pojoClass, exclude("numTests", "tJobExec"))
                .testing(Method.GETTER, Method.SETTER)
                // .testing(Method.EQUALS)
                // .testing(Method.HASH_CODE)
                .testing(Method.CONSTRUCTOR).areWellImplemented();
    }

    @Test
    public void testPojoTestCase() {
        final Class<?> pojoClass = TestCase.class;
        // assertPojoMethodsFor(testCaseClass).areWellImplemented();

        assertPojoMethodsFor(pojoClass).testing(Method.GETTER, Method.SETTER)
                .testing(Method.EQUALS).testing(Method.HASH_CODE)
                .testing(Method.CONSTRUCTOR).areWellImplemented();
    }

    @Test
    public void testPojoDockerExecution() {
        final Class<?> pojoClass = DockerExecution.class;
        // assertPojoMethodsFor(testCaseClass).areWellImplemented();

        assertPojoMethodsFor(pojoClass,
                exclude("tJobExec", "externalTJob", "externalTJobExec", "tJob",
                        "sut", "isExternal"))
                                .testing(Method.GETTER, Method.SETTER)
                                // .testing(Method.EQUALS)
                                // .testing(Method.HASH_CODE)
                                // .testing(Method.CONSTRUCTOR)
                                .areWellImplemented();

    }

    @Test
    public void testPojoExternalJob() {
        final Class<?> pojoClass = ExternalJob.class;
        // assertPojoMethodsFor(testCaseClass).areWellImplemented();

        assertPojoMethodsFor(pojoClass, exclude("tJobExecId"))
                .testing(Method.GETTER, Method.SETTER)
                // .testing(Method.HASH_CODE)
                .testing(Method.CONSTRUCTOR).areWellImplemented();

    }

    @Test
    public void testPojoParameter() {
        final Class<?> pojoClass = Parameter.class;
        // assertPojoMethodsFor(testCaseClass).areWellImplemented();

        assertPojoMethodsForAll(pojoClass).testing(Method.GETTER, Method.SETTER)
                .testing(Method.EQUALS).testing(Method.HASH_CODE)
                .testing(Method.CONSTRUCTOR).areWellImplemented();
    }

    // @Test
    // public void testAllPojos() {
    // // given
    // final Class<Pojo> classUnderTest = Pojo.class;
    //
    // // when
    //
    // // then
    // assertPojoMethodsForAll(classUnderTest, classUnderTest, classUnderTest,
    // classUnderTest).areWellImplemented();
    // }

}
