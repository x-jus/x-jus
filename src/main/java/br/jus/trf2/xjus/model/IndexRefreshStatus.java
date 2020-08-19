package br.jus.trf2.xjus.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "index-refresh-status")
public class IndexRefreshStatus {
	@Id
	Long id;

	private String idx;

	private Date refreshLastModified;
	private String refreshLastId;

	private boolean refreshComplete;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getRefreshLastModified() {
		return refreshLastModified;
	}

	public void setRefreshLastModified(Date refreshLastModified) {
		this.refreshLastModified = refreshLastModified;
	}

	public String getRefreshLastId() {
		return refreshLastId;
	}

	public void setRefreshLastId(String refreshLastId) {
		this.refreshLastId = refreshLastId;
	}

	public boolean isRefreshComplete() {
		return refreshComplete;
	}

	public void setRefreshComplete(boolean refreshComplete) {
		this.refreshComplete = refreshComplete;
	}

	public String getIdx() {
		return idx;
	}

	public void setIdx(String idx) {
		this.idx = idx;
	}
}
