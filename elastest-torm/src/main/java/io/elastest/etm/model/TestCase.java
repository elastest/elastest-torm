package io.elastest.etm.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.elastest.etm.model.TestSuite.BasicTestSuite;

@Entity
public class TestCase {
	public interface BasicTestCase {
	}

	@JsonView({ BasicTestCase.class, BasicTestSuite.class, BasicAttTJobExec.class })
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	@JsonProperty("id")
	private Long id = null;

	@JsonView({ BasicTestCase.class, BasicTestSuite.class, BasicAttTJobExec.class })
	@Column(name = "name")
	@JsonProperty("name")
	private String name;

	@JsonView({ BasicTestCase.class, BasicTestSuite.class, BasicAttTJobExec.class })
	@Column(name = "time")
	@JsonProperty("time")
	private double time;

	@JsonView({ BasicTestCase.class, BasicTestSuite.class, BasicAttTJobExec.class })
	@Column(name = "failureMessage", columnDefinition = "TEXT", length = 65535)
	@JsonProperty("failureMessage")
	private String failureMessage;

	@JsonView({ BasicTestCase.class, BasicTestSuite.class, BasicAttTJobExec.class })
	@Column(name = "failureType")
	@JsonProperty("failureType")
	private String failureType;

	@JsonView({ BasicTestCase.class, BasicTestSuite.class, BasicAttTJobExec.class })
	@Column(name = "failureErrorLine")
	@JsonProperty("failureErrorLine")
	private String failureErrorLine;

	@JsonView({ BasicTestCase.class, BasicTestSuite.class, BasicAttTJobExec.class })
	@Column(name = "failureDetail", columnDefinition = "TEXT", length = 65535)
	@JsonProperty("failureDetail")
	private String failureDetail;

	// bi-directional many-to-one association to testSuite
	@JsonView({ BasicTestCase.class })
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "testSuite")
	private TestSuite testSuite;

	// Constructors
	public TestCase() {
	}

	public TestCase(String name, double time, String failureMessage, String failureType, String failureErrorLine,
			String failureDetail, TestSuite testSuite) {
		super();
		this.id = id == null ? 0 : id;
		this.name = name;
		this.time = time;
		this.failureMessage = failureMessage;
		this.failureType = failureType;
		this.failureErrorLine = failureErrorLine;
		this.failureDetail = failureDetail;
		this.testSuite = testSuite;
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

	public TestCase id(Long id) {
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

	public TestCase name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Get/Set time
	 * 
	 * @return time
	 **/
	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public TestCase time(double time) {
		this.time = time;
		return this;
	}

	/**
	 * Get/Set failureMessage
	 * 
	 * @return failureMessage
	 **/
	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public TestCase failureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
		return this;
	}

	/**
	 * Get/Set failureType
	 * 
	 * @return failureType
	 **/
	public String getFailureType() {
		return failureType;
	}

	public void setFailureType(String failureType) {
		this.failureType = failureType;
	}

	public TestCase failureType(String failureType) {
		this.failureType = failureType;
		return this;
	}

	/**
	 * Get/Set failureErrorLine
	 * 
	 * @return failureErrorLine
	 **/
	public String getFailureErrorLine() {
		return failureErrorLine;
	}

	public void setFailureErrorLine(String failureErrorLine) {
		this.failureErrorLine = failureErrorLine;
	}

	public TestCase failureErrorLine(String failureErrorLine) {
		this.failureErrorLine = failureErrorLine;
		return this;
	}

	/**
	 * Get/Set failureDetail
	 * 
	 * @return failureDetail
	 **/
	public String getFailureDetail() {
		return failureDetail;
	}

	public void setFailureDetail(String failureDetail) {
		this.failureDetail = failureDetail;
	}

	public TestCase failureDetail(String failureDetail) {
		this.failureDetail = failureDetail;
		return this;
	}

	/**
	 * Get/Set testSuite
	 * 
	 * @return testSuite
	 **/
	public TestSuite getTestSuite() {
		return testSuite;
	}

	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}

	public TestCase testSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
		return this;
	}

	// Others

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((failureDetail == null) ? 0 : failureDetail.hashCode());
		result = prime * result + ((failureErrorLine == null) ? 0 : failureErrorLine.hashCode());
		result = prime * result + ((failureMessage == null) ? 0 : failureMessage.hashCode());
		result = prime * result + ((failureType == null) ? 0 : failureType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((testSuite == null) ? 0 : testSuite.hashCode());
		long temp;
		temp = Double.doubleToLongBits(time);
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
		TestCase other = (TestCase) obj;
		if (failureDetail == null) {
			if (other.failureDetail != null)
				return false;
		} else if (!failureDetail.equals(other.failureDetail))
			return false;
		if (failureErrorLine == null) {
			if (other.failureErrorLine != null)
				return false;
		} else if (!failureErrorLine.equals(other.failureErrorLine))
			return false;
		if (failureMessage == null) {
			if (other.failureMessage != null)
				return false;
		} else if (!failureMessage.equals(other.failureMessage))
			return false;
		if (failureType == null) {
			if (other.failureType != null)
				return false;
		} else if (!failureType.equals(other.failureType))
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
		if (testSuite == null) {
			if (other.testSuite != null)
				return false;
		} else if (!testSuite.equals(other.testSuite))
			return false;
		if (Double.doubleToLongBits(time) != Double.doubleToLongBits(other.time))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class TestSuite {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    time: ").append(toIndentedString(time)).append("\n");
		sb.append("    failureMessage: ").append(toIndentedString(failureMessage)).append("\n");
		sb.append("    failureType: ").append(toIndentedString(failureType)).append("\n");
		sb.append("    failureErrorLine: ").append(toIndentedString(failureErrorLine)).append("\n");
		sb.append("    failureDetail: ").append(toIndentedString(failureDetail)).append("\n");
		sb.append("    testSuite: ").append(toIndentedString(testSuite)).append("\n");
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
