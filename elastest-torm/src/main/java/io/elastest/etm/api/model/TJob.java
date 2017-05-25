package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import io.elastest.etm.api.model.TestService;
import io.elastest.etm.model.ElasEtmTjobexec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * TJob
 */

@Entity
@Table(name="ELAS_ETM_TJOB")
@NamedQuery(name="TJob.findAll", query="SELECT e FROM TJob e")
public class TJob {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ELAS_ETM_TJOB_ID")
	@JsonProperty("id")
	private Long id = null;

	@Column(name="ELAS_ETM_TJOB_NAME")
	@JsonProperty("name")
	private String name = null;

	@Column(name="ELAS_ETM_TJOB_TSERV")
	@JsonProperty("testServices")
//	private List<TestService> testServices = new ArrayList<TestService>();
	private Long testServices = null;

	@Column(name="ELAS_ETM_TJOB_IMNAME")
	@JsonProperty("imageName")
	private String imageName = null;

	@Column(name="ELAS_ETM_TJOB_SUT")
	@JsonProperty("sut")
	private Integer sut = null;
		
	//bi-directional many-to-one association to ElasEtmTjobexec
	@OneToMany(mappedBy="tJob")
	private List<TJobExecution> tjobExecs;

	public TJob() {	}
	
	public TJob(Long id, String name, Long testServices, String imageName, Integer sut) {
		this.id = id;
		this.name = name;
		this.testServices = testServices;
		this.imageName = imageName;
		this.sut = sut;
	}

	public TJob id(Long id) {
		this.id = id;
		return this;
	}

	/**
	 * Get id
	 * 
	 * @return id
	 **/
	@ApiModelProperty(example = "", value = "")

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TJob name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Get name
	 * 
	 * @return name
	 **/
	@ApiModelProperty(example = "testApp1", required = true, value = "")
	@NotNull

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TJob testServices(Long testServices) {
		this.testServices = testServices;
		return this;
	}

//	public TJob addTestServicesItem(TestService testServicesItem) {
//		this.testServices.add(testServicesItem);
//		return this;
//	}

	/**
	 * Get testServices
	 * 
	 * @return testServices
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	@Valid

	public Long getTestServices() {
		return testServices;
	}

	public void setTestServices(Long testServices) {
		this.testServices = testServices;
	}

	public TJob imageName(String imageName) {
		this.imageName = imageName;
		return this;
	}

	/**
	 * Get imageName
	 * 
	 * @return imageName
	 **/
	@ApiModelProperty(example = "edujgurjc/torm-test-01", value = "")

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public TJob sut(Integer sut) {
		this.sut = sut;
		return this;
	}

	/**
	 * Get sut
	 * 
	 * @return sut
	 **/
	@ApiModelProperty(example = "12345678", value = "")

	public Integer getSut() {
		return sut;
	}

	public void setSut(Integer sut) {
		this.sut = sut;
	}
	
	
	
	public List<TJobExecution> getTjobExecs() {
		return this.tjobExecs;
	}

	public void setTjobExecs(List<TJobExecution> tjobExec) {
		this.tjobExecs = tjobExec;
	}
	
	
	public TJobExecution addTjobExec(TJobExecution tjobExec) {
		getTjobExecs().add(tjobExec);
		tjobExec.setTjob(this);

		return tjobExec;
	}

	public TJobExecution removeElasEtmTjobexec(TJobExecution tjobExec) {
		getTjobExecs().remove(tjobExec);
		tjobExec.setTjob(null);
		return tjobExec;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TJob tjob = (TJob) o;
		return Objects.equals(this.id, tjob.id) && Objects.equals(this.name, tjob.name)
				&& Objects.equals(this.testServices, tjob.testServices)
				&& Objects.equals(this.imageName, tjob.imageName) && Objects.equals(this.sut, tjob.sut);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, testServices, imageName, sut);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class TJob {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    testServices: ").append(toIndentedString(testServices)).append("\n");
		sb.append("    imageName: ").append(toIndentedString(imageName)).append("\n");
		sb.append("    sut: ").append(toIndentedString(sut)).append("\n");
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
