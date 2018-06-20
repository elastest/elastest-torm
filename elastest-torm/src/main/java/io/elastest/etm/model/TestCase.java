package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.elastest.etm.model.TestSuite.BasicTestSuite;
import io.swagger.annotations.ApiModel;

@Entity
@ApiModel(description = "Object that contains the information of the test results.")
public class TestCase {
    public interface BasicTestCase {
    }

    @JsonView({ BasicTestCase.class, BasicTestSuite.class,
            BasicAttTJobExec.class, BasicAttTJob.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ BasicTestCase.class, BasicTestSuite.class,
            BasicAttTJobExec.class, BasicAttTJob.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name;

    @JsonView({ BasicTestCase.class, BasicTestSuite.class,
            BasicAttTJobExec.class, BasicAttTJob.class })
    @Column(name = "time")
    @JsonProperty("time")
    private double time;

    @JsonView({ BasicTestCase.class, BasicTestSuite.class,
            BasicAttTJobExec.class, BasicAttTJob.class })
    @Column(name = "failureMessage", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("failureMessage")
    private String failureMessage;

    @JsonView({ BasicTestCase.class, BasicTestSuite.class,
            BasicAttTJobExec.class, BasicAttTJob.class })
    @Column(name = "failureType")
    @JsonProperty("failureType")
    private String failureType;

    @JsonView({ BasicTestCase.class, BasicTestSuite.class,
            BasicAttTJobExec.class, BasicAttTJob.class })
    @Column(name = "failureErrorLine")
    @JsonProperty("failureErrorLine")
    private String failureErrorLine;

    @JsonView({ BasicTestCase.class, BasicTestSuite.class,
            BasicAttTJobExec.class, BasicAttTJob.class })
    @Column(name = "failureDetail", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("failureDetail")
    private String failureDetail;

    // bi-directional many-to-one association to testSuite
    @JsonView({ BasicTestCase.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testSuite")
    private TestSuite testSuite;

    @JsonView({ BasicAttTJobExec.class, BasicAttTJob.class,
            BasicAttProject.class })
    @Column(name = "startDate")
    private Date startDate = null;

    @JsonView({ BasicAttTJobExec.class, BasicAttTJob.class,
            BasicAttProject.class })
    @Column(name = "endDate")
    private Date endDate = null;

    // Constructors
    public TestCase() {
    }

    public TestCase(String name, double time, String failureMessage,
            String failureType, String failureErrorLine, String failureDetail,
            TestSuite testSuite) {
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

    public void cleanNameAndSet(String name) {
        name = name.split("\\(")[0];
        this.setName(name);
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

    /**
     * Get/Set startDate
     * 
     * @return startDate
     **/
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Get/Set endDate
     * 
     * @return endDate
     **/
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    // Others

    @Override
    public int hashCode() {
        return Objects.hash(id, name, time, failureMessage, failureType,
                failureErrorLine, failureDetail, testSuite, startDate, endDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestCase testCase = (TestCase) o;

        return Objects.equals(this.id, testCase.id)
                && Objects.equals(this.name, testCase.name)
                && Objects.equals(this.time, testCase.time)
                && Objects.equals(this.failureMessage, testCase.failureMessage)
                && Objects.equals(this.failureType, testCase.failureType)
                && Objects.equals(this.failureErrorLine,
                        testCase.failureErrorLine)
                && Objects.equals(this.failureDetail, testCase.failureDetail)
                && Objects.equals(this.testSuite, testCase.testSuite)
                && Objects.equals(this.startDate, testCase.startDate)
                && Objects.equals(this.endDate, testCase.endDate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TestSuite {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    time: ").append(toIndentedString(time)).append("\n");
        sb.append("    failureMessage: ")
                .append(toIndentedString(failureMessage)).append("\n");
        sb.append("    failureType: ").append(toIndentedString(failureType))
                .append("\n");
        sb.append("    failureErrorLine: ")
                .append(toIndentedString(failureErrorLine)).append("\n");
        sb.append("    failureDetail: ").append(toIndentedString(failureDetail))
                .append("\n");
        sb.append("    testSuite: ").append(toIndentedString(testSuite))
                .append("\n");
        sb.append("    startDate: ").append(toIndentedString(startDate))
                .append("\n");
        sb.append("    endDate: ").append(toIndentedString(endDate))
                .append("\n");
        sb.append("}");
        return sb.toString();
    }
}
