package io.elastest.etm.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TestCaseRepository;
import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.utils.ElastestConstants;

@Service
public class EpmIntegrationService {
	private static final Logger logger = LoggerFactory.getLogger(EpmIntegrationService.class);

	@Value("${elastest.elasticsearch.host}")
	private String elasticsearchHost;

	@Value("${elastest.execution.mode}")
	public String ELASTEST_EXECUTION_MODE;

	private final DockerService dockerService;
	private final TestSuiteRepository testSuiteRepo;
	private final TestCaseRepository testCaseRepo;

	private final TJobExecRepository tJobExecRepositoryImpl;

	private DatabaseSessionManager dbmanager;
	private final EsmService esmService;

	public EpmIntegrationService(DockerService dockerService, TestSuiteRepository testSuiteRepo,
			TestCaseRepository testCaseRepo, TJobExecRepository tJobExecRepositoryImpl,
			DatabaseSessionManager dbmanager, EsmService esmService) {
		super();
		this.dockerService = dockerService;
		this.testSuiteRepo = testSuiteRepo;
		this.testCaseRepo = testCaseRepo;
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.dbmanager = dbmanager;
		this.esmService = esmService;
	}

	@Async
	public void executeTJob(TJobExecution tJobExec, String tJobServices) {
		dbmanager.bindSession();
		tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());

		if (tJobServices != null && tJobServices != "") {
			provideServices(tJobServices, tJobExec);
		}

		DockerExecution dockerExec = new DockerExecution(tJobExec);

		try {
			// Create queues and load basic services
			dockerService.loadBasicServices(dockerExec);

			List<ReportTestSuite> testSuites;
			// Start Test
			testSuites = dockerService.executeTest(dockerExec);
			tJobExec.setResult(TJobExecution.ResultEnum.FINISHED);
			saveTestResults(testSuites, tJobExec);

			// End and purge services
			dockerService.endAllExec(dockerExec);
			if (tJobServices != null && tJobServices != "") {
				deprovideServices(tJobExec);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (!e.getMessage().equals("end error")) { // TODO customize
														// exception
				tJobExec.setResult(TJobExecution.ResultEnum.FAILURE);
			}
		}

		// Setting execution data
		tJobExec.setSutExecution(dockerExec.getSutExec());

		// Saving execution data
		tJobExecRepositoryImpl.save(tJobExec);
		dbmanager.unbindSession();
	}

	public void saveTestResults(List<ReportTestSuite> testSuites, TJobExecution tJobExec) {
		System.out.println("Saving test results " + tJobExec.getId());

		TestSuite tSuite;
		TestCase tCase;
		if (testSuites != null && testSuites.size() > 0) {
			ReportTestSuite reportTestSuite = testSuites.get(0);
			tSuite = new TestSuite();
			tSuite.setTimeElapsed(reportTestSuite.getTimeElapsed());
			tSuite.setErrors(reportTestSuite.getNumberOfErrors());
			tSuite.setFailures(reportTestSuite.getNumberOfFailures());
			tSuite.setFlakes(reportTestSuite.getNumberOfFlakes());
			tSuite.setSkipped(reportTestSuite.getNumberOfSkipped());
			tSuite.setName(reportTestSuite.getName());
			tSuite.setnumTests(reportTestSuite.getNumberOfTests());

			tSuite = testSuiteRepo.save(tSuite);

			for (ReportTestCase reportTestCase : reportTestSuite.getTestCases()) {
				tCase = new TestCase();
				tCase.setName(reportTestCase.getName());
				tCase.setTime(reportTestCase.getTime());
				tCase.setFailureDetail(reportTestCase.getFailureDetail());
				tCase.setFailureErrorLine(reportTestCase.getFailureErrorLine());
				tCase.setFailureMessage(reportTestCase.getFailureMessage());
				tCase.setFailureType(reportTestCase.getFailureType());
				tCase.setTestSuite(tSuite);

				testCaseRepo.save(tCase);
			}

			testSuiteRepo.save(tSuite);
			tJobExec.setTestSuite(tSuite);
		}
	}

	/**
	 * 
	 * @param tJobServices
	 * @param tJobExec
	 */
	private void provideServices(String tJobServices, TJobExecution tJobExec) {
		logger.info("Start the service provision.");
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<ObjectNode> services = Arrays.asList(mapper.readValue(tJobServices, ObjectNode[].class));
			for (ObjectNode service : services) {
				if (service.get("selected").toString().equals(Boolean.toString(true))) {
					String instanceId = esmService
							.provisionServiceInstance(service.get("id").toString().replaceAll("\"", ""), true)
							.getInstanceId();
					tJobExec.getServicesInstances().add(instanceId);
					setTssEnvVar(instanceId, tJobExec);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param String
	 * @param tJobExec
	 */
	private void setTssEnvVar(String instanceId, TJobExecution tJobExec) {
		SupportServiceInstance ssi = esmService.gettJobServicesInstances().get(instanceId);
		// String envNamePrefix = "ET_" +
		// ssi.getServiceShortName().toUpperCase().replaceAll("-", "_");
		String servicePrefix = ssi.getServiceName().toUpperCase().replaceAll("-", "_");
		String envNamePrefix = "ET_" + servicePrefix;

		for (Map.Entry<String, JsonNode> entry : ssi.getEndpointsData().entrySet()) {
			String endpointName = ssi.getEndpointName().toUpperCase().replaceAll("-", "_");
			String prefix = envNamePrefix;
			if (!endpointName.equals(servicePrefix)) { // Example: EBS and Spark
				prefix += "_" + endpointName;
			}
			setTssEnvVarByEndpoint(ssi, tJobExec, prefix, entry);
		}

		for (SupportServiceInstance subssi : ssi.getSubServices()) {
			String envNamePrefixSubSSI = envNamePrefix + "_"
					+ subssi.getEndpointName().toUpperCase().replaceAll("-", "_");

			for (Map.Entry<String, JsonNode> entry : subssi.getEndpointsData().entrySet()) {
				setTssEnvVarByEndpoint(subssi, tJobExec, envNamePrefixSubSSI, entry);
			}
		}
	}

	private void setTssEnvVarByEndpoint(SupportServiceInstance ssi, TJobExecution tJobExec, String prefix,
			Map.Entry<String, JsonNode> entry) {
		if (!entry.getKey().toLowerCase().equals("gui")) {
			try {

				if (entry.getValue().findValue("name") != null) {
					prefix += "_" + entry.getValue().findValue("name").toString().toUpperCase().replaceAll("\"", "")
							.replaceAll("-", "_");
				}

				String envNameHost = prefix + "_HOST";
				String envValueHost = ssi.getContainerIp();

				String envNamePort = prefix + "_PORT";
				String envValuePort = entry.getValue().findValue("port").toString();

				String protocol = entry.getValue().findValue("protocol").toString().toLowerCase().replaceAll("\"", "");
				if (protocol.equals("http") || protocol.equals("ws")) {
					String envNameAPI = prefix + "_" + entry.getKey().toUpperCase();
					String path = entry.getValue().findValue("path").toString().replaceAll("\"", "");
					if (!path.startsWith("/")) {
						path = "/" + path;
					}
					String envValueAPI = protocol + "://" + envValueHost + ":" + envValuePort + path;
					tJobExec.getTssEnvVars().put(envNameAPI, envValueAPI);
				} else {
					tJobExec.getTssEnvVars().put(envNameHost, envValueHost);
					tJobExec.getTssEnvVars().put(envNamePort, envValuePort);
				}

			} catch (Exception e) {
			}
		}
	}

	/**
	 * 
	 * @param tJobExec
	 */
	private void deprovideServices(TJobExecution tJobExec) {
		logger.info("Start the service deprovision.");
		for (String instance_id : tJobExec.getServicesInstances()) {
			esmService.deprovisionServiceInstance(instance_id, true);
		}
	}

}
