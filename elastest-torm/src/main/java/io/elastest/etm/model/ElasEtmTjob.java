package io.elastest.etm.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the elas_etm_tjob database table.
 * 
 */
@Entity
@Table(name="ELAS_ETM_TJOB")
@NamedQuery(name="ElasEtmTjob.findAll", query="SELECT e FROM ElasEtmTjob e")
public class ElasEtmTjob implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ELAS_ETM_TJOB_ID")
	private Long elasEtmTjobId;

	@Column(name="ELAS_ETM_TJOB_IMNAME")
	private String elasEtmTjobImname;

	@Column(name="ELAS_ETM_TJOB_NAME")
	private String elasEtmTjobName;

	@Column(name="ELAS_ETM_TJOB_SUT")
	private int elasEtmTjobSut;

	@Column(name="ELAS_ETM_TJOB_TJOBSEXEC")
	private int elasEtmTjobTjobsexec;

	@Column(name="ELAS_ETM_TJOB_TSERV")
	private int elasEtmTjobTserv;

	//bi-directional many-to-one association to ElasEtmTjobexec
	@OneToMany(mappedBy="elasEtmTjob")
	private List<ElasEtmTjobexec> elasEtmTjobexecs;

	public ElasEtmTjob() {
	}

	public Long getElasEtmTjobId() {
		return this.elasEtmTjobId;
	}

	public void setElasEtmTjobId(Long elasEtmTjobId) {
		this.elasEtmTjobId = elasEtmTjobId;
	}

	public String getElasEtmTjobImname() {
		return this.elasEtmTjobImname;
	}

	public void setElasEtmTjobImname(String elasEtmTjobImname) {
		this.elasEtmTjobImname = elasEtmTjobImname;
	}

	public String getElasEtmTjobName() {
		return this.elasEtmTjobName;
	}

	public void setElasEtmTjobName(String elasEtmTjobName) {
		this.elasEtmTjobName = elasEtmTjobName;
	}

	public int getElasEtmTjobSut() {
		return this.elasEtmTjobSut;
	}

	public void setElasEtmTjobSut(int elasEtmTjobSut) {
		this.elasEtmTjobSut = elasEtmTjobSut;
	}

	public int getElasEtmTjobTjobsexec() {
		return this.elasEtmTjobTjobsexec;
	}

	public void setElasEtmTjobTjobsexec(int elasEtmTjobTjobsexec) {
		this.elasEtmTjobTjobsexec = elasEtmTjobTjobsexec;
	}

	public int getElasEtmTjobTserv() {
		return this.elasEtmTjobTserv;
	}

	public void setElasEtmTjobTserv(int elasEtmTjobTserv) {
		this.elasEtmTjobTserv = elasEtmTjobTserv;
	}

	public List<ElasEtmTjobexec> getElasEtmTjobexecs() {
		return this.elasEtmTjobexecs;
	}

	public void setElasEtmTjobexecs(List<ElasEtmTjobexec> elasEtmTjobexecs) {
		this.elasEtmTjobexecs = elasEtmTjobexecs;
	}

	public ElasEtmTjobexec addElasEtmTjobexec(ElasEtmTjobexec elasEtmTjobexec) {
		getElasEtmTjobexecs().add(elasEtmTjobexec);
		elasEtmTjobexec.setElasEtmTjob(this);

		return elasEtmTjobexec;
	}

	public ElasEtmTjobexec removeElasEtmTjobexec(ElasEtmTjobexec elasEtmTjobexec) {
		getElasEtmTjobexecs().remove(elasEtmTjobexec);
		elasEtmTjobexec.setElasEtmTjob(null);

		return elasEtmTjobexec;
	}

}