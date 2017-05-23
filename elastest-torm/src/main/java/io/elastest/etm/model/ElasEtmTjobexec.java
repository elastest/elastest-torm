package io.elastest.etm.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the elas_etm_tjobexec database table.
 * 
 */
@Entity
@Table(name="elas_etm_tjobexec")
@NamedQuery(name="ElasEtmTjobexec.findAll", query="SELECT e FROM ElasEtmTjobexec e")
public class ElasEtmTjobexec implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ELAS_ETM_TJOBEXEC_ID")
	private Long elasEtmTjobexecId;

	@Column(name="ELAS_ETM_TJOBEXEC_DURATION")
	private Long elasEtmTjobexecDuration;

	@Column(name="ELAS_ETM_TJOBEXEC_ERROR_EXEC")
	private String elasEtmTjobexecErrorExec;

	@Column(name="ELAS_ETM_TJOBEXEC_LOGS")
	private int elasEtmTjobexecLogs;

	@Column(name="ELAS_ETM_TJOBEXEC_RESULT")
	private String elasEtmTjobexecResult;

	@Column(name="ELAS_ETM_TJOBEXEC_SUT_EXEC")
	private int elasEtmTjobexecSutExec;

	//bi-directional many-to-one association to ElasEtmTjob
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ELAS_ETM_TJOBEXEC_TJOB")
	private ElasEtmTjob elasEtmTjob;

	public ElasEtmTjobexec() {
	}

	public Long getElasEtmTjobexecId() {
		return this.elasEtmTjobexecId;
	}

	public void setElasEtmTjobexecId(Long elasEtmTjobexecId) {
		this.elasEtmTjobexecId = elasEtmTjobexecId;
	}

	public Long getElasEtmTjobexecDuration() {
		return this.elasEtmTjobexecDuration;
	}

	public void setElasEtmTjobexecDuration(Long elasEtmTjobexecDuration) {
		this.elasEtmTjobexecDuration = elasEtmTjobexecDuration;
	}

	public String getElasEtmTjobexecErrorExec() {
		return this.elasEtmTjobexecErrorExec;
	}

	public void setElasEtmTjobexecErrorExec(String elasEtmTjobexecErrorExec) {
		this.elasEtmTjobexecErrorExec = elasEtmTjobexecErrorExec;
	}

	public int getElasEtmTjobexecLogs() {
		return this.elasEtmTjobexecLogs;
	}

	public void setElasEtmTjobexecLogs(int elasEtmTjobexecLogs) {
		this.elasEtmTjobexecLogs = elasEtmTjobexecLogs;
	}

	public String getElasEtmTjobexecResult() {
		return this.elasEtmTjobexecResult;
	}

	public void setElasEtmTjobexecResult(String elasEtmTjobexecResult) {
		this.elasEtmTjobexecResult = elasEtmTjobexecResult;
	}

	public int getElasEtmTjobexecSutExec() {
		return this.elasEtmTjobexecSutExec;
	}

	public void setElasEtmTjobexecSutExec(int elasEtmTjobexecSutExec) {
		this.elasEtmTjobexecSutExec = elasEtmTjobexecSutExec;
	}

	public ElasEtmTjob getElasEtmTjob() {
		return this.elasEtmTjob;
	}

	public void setElasEtmTjob(ElasEtmTjob elasEtmTjob) {
		this.elasEtmTjob = elasEtmTjob;
	}

}