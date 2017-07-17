package io.elastest.etm.api.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.api.model.TJobExecution.BasicAttTJobExec;
import io.elastest.etm.api.model.TestCase.BasicTestCase;

@Entity
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

	public TestSuite id(Long id) {
		this.id = id == null ? 0 : id;
		return this;
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

	public TestSuite name(String name) {
		this.name = name;
		return this;
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

	public TestSuite timeElapsed(double timeElapsed) {
		this.timeElapsed = timeElapsed;
		return this;
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

	public TestSuite errors(int errors) {
		this.errors = errors;
		return this;
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

	public TestSuite failures(int failures) {
		this.failures = failures;
		return this;
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

	public TestSuite skipped(int skipped) {
		this.skipped = skipped;
		return this;
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

	public TestSuite flakes(int flakes) {
		this.flakes = flakes;
		return this;
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

	public TestSuite numTests(int numTests) {
		this.numTests = numTests;
		return this;
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

	public TestSuite tJobExec(TJobExecution tJobExec) {
		this.tJobExec = tJobExec;
		return this;
	}

	// Others

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + errors;
		result = prime * result + failures;
		result = prime * result + flakes;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + numTests;
		result = prime * result + skipped;
		result = prime * result + ((tJobExec == null) ? 0 : tJobExec.hashCode());
		result = prime * result + ((testCases == null) ? 0 : testCases.hashCode());
		long temp;
		temp = Double.doubleToLongBits(timeElapsed);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestSuite other = (TestSuite) obj;
		if (errors != other.errors)
			return false;
		if (failures != other.failures)
			return false;
		if (flakes != other.flakes)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (numTests != other.numTests)
			return false;
		if (skipped != other.skipped)
			return false;
		if (tJobExec == null) {
			if (other.tJobExec != null)
				return false;
		} else if (!tJobExec.equals(other.tJobExec))
			return false;
		if (testCases == null) {
			if (other.testCases != null)
				return false;
		} else if (!testCases.equals(other.testCases))
			return false;
		if (Double.doubleToLongBits(timeElapsed) != Double.doubleToLongBits(other.timeElapsed))
			return false;
		return true;
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

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
