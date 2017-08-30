package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.swagger.annotations.ApiModel;

@Entity
@ApiModel(description = "Object that contains the information of a test suit results.")
public class TestSuite {
	public interface BasicTestSuite {
	}

	@JsonView({ BasicAttTJobExec.class, BasicTestSuite.class })
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	@JsonProperty("id")
	private Long id = null;

	@JsonView({ BasicAttTJobExec.class, BasicTestSuite.class })
	@Column(name = "name")
	@JsonProperty("name")
	private String name;

	@JsonView({ BasicAttTJobExec.class, BasicTestSuite.class })
	@Column(name = "timeElapsed")
	@JsonProperty("timeElapsed")
	private double timeElapsed;

	@JsonView({ BasicAttTJobExec.class, BasicTestSuite.class })
	@Column(name = "errors")
	@JsonProperty("errors")
	private int errors;

	@JsonView({ BasicAttTJobExec.class, BasicTestSuite.class })
	@Column(name = "failures")
	@JsonProperty("failures")
	private int failures;

	@JsonView({ BasicAttTJobExec.class, BasicTestSuite.class })
	@Column(name = "skipped")
	@JsonProperty("skipped")
	private int skipped;

	@JsonView({ BasicAttTJobExec.class, BasicTestSuite.class })
	@Column(name = "flakes")
	@JsonProperty("flakes")
	private int flakes;

	@JsonView({ BasicAttTJobExec.class, BasicTestSuite.class })
	@Column(name = "numTests")
	@JsonProperty("numTests")
	private int numTests;

	// bi-directional many-to-one association to TestCase
	@JsonView({ BasicAttTJobExec.class, BasicTestSuite.class })
	@OneToMany(mappedBy = "testSuite", cascade = CascadeType.REMOVE)
	private List<TestCase> testCases;

	@JsonView({ BasicTestSuite.class })
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "testSuite")
	@JoinColumn(name = "tJobExec")
	private TJobExecution tJobExec;

	// Constructors
	public TestSuite() {
	}

	public TestSuite(Long id, String name, double timeElapsed, int errors, int failures, int skipped, int flakes,
			int numTests, List<TestCase> testCases) {
		super();
		this.id = id == null ? 0 : id;
		this.name = name;
		this.timeElapsed = timeElapsed;
		this.errors = errors;
		this.failures = failures;
		this.skipped = skipped;
		this.flakes = flakes;
		this.numTests = numTests;
		this.testCases = testCases;
	}

	// Methods

	/**
	 * Get/Set id
	 * 
	 * @return id
	 **/
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id == null ? 0 : id;
	}

	/**
	 * Get/Set name
	 * 
	 * @return name
	 **/
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get/Set timeElapsed
	 * 
	 * @return timeElapsed
	 **/

	public double getTimeElapsed() {
		return timeElapsed;
	}

	public void setTimeElapsed(double timeElapsed) {
		this.timeElapsed = timeElapsed;
	}

	/**
	 * Get/Set errors
	 * 
	 * @return errors
	 **/
	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	/**
	 * Get/Set failures
	 * 
	 * @return failures
	 **/
	public int getFailures() {
		return failures;
	}

	public void setFailures(int failures) {
		this.failures = failures;
	}

	/**
	 * Get/Set skipped
	 * 
	 * @return skipped
	 **/
	public int getSkipped() {
		return skipped;
	}

	public void setSkipped(int skipped) {
		this.skipped = skipped;
	}

	/**
	 * Get/Set flakes
	 * 
	 * @return flakes
	 **/
	public int getFlakes() {
		return flakes;
	}

	public void setFlakes(int flakes) {
		this.flakes = flakes;
	}

	/**
	 * Get/Set numTests
	 * 
	 * @return numTests
	 **/
	public int getnumTests() {
		return numTests;
	}

	public void setnumTests(int numTests) {
		this.numTests = numTests;
	}

	/**
	 * Get/Set testCases
	 * 
	 * @return testCases
	 **/
	public List<TestCase> getTestCases() {
		return testCases;
	}

	public void setTestCases(List<TestCase> testCases) {
		this.testCases = testCases;
	}

	public TestSuite testCases(List<TestCase> testCases) {
		this.testCases = testCases;
		return this;
	}

	public TestCase addTestCase(TestCase testCase) {
		getTestCases().add(testCase);
		testCase.setTestSuite(this);

		return testCase;
	}

	public TestCase removeTestCase(TestCase testCase) {
		getTestCases().remove(testCase);
		testCase.setTestSuite(null);
		return testCase;
	}

	/**
	 * Get/Set tJobExec
	 * 
	 * @return tJobExec
	 **/

	public TJobExecution gettJobExec() {
		return tJobExec;
	}

	public void settJobExec(TJobExecution tJobExec) {
		this.tJobExec = tJobExec;
	}

	// Others

	@Override
	public int hashCode() {	
		return Objects.hash(id, name, timeElapsed, errors, failures, skipped, flakes, numTests, testCases, tJobExec);
	}

	@Override
	public boolean equals(Object o) {		
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TestSuite testSuite = (TestSuite) o;
		
		return Objects.equals(this.id, testSuite.id) && Objects.equals(this.name, testSuite.name)
				&& Objects.equals(this.timeElapsed, testSuite.timeElapsed) && Objects.equals(this.errors, testSuite.errors)
				&& Objects.equals(this.failures, testSuite.failures) && Objects.equals(this.skipped, testSuite.skipped)
				&& Objects.equals(this.flakes, testSuite.flakes) && Objects.equals(this.numTests, testSuite.numTests)
				&& Objects.equals(this.testCases, testSuite.testCases) && Objects.equals(this.tJobExec, testSuite.tJobExec);

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class TestSuite {\n");
		
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    timeElapsed: ").append(toIndentedString(timeElapsed)).append("\n");
		sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
		sb.append("    failures: ").append(toIndentedString(failures)).append("\n");
		sb.append("    skipped: ").append(toIndentedString(skipped)).append("\n");
		sb.append("    flakes: ").append(toIndentedString(flakes)).append("\n");
		sb.append("    numTests: ").append(toIndentedString(numTests)).append("\n");
		sb.append("    testCases: ").append(toIndentedString(testCases)).append("\n");
		sb.append("    tJobExec: ").append(toIndentedString(tJobExec)).append("\n");
		sb.append("}");
		
		return sb.toString();
	}

}
