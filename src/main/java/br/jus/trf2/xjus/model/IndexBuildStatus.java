package br.jus.trf2.xjus.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "index-build-status")
public class IndexBuildStatus {
	@Id
	Long id;
	
	private Date buildLastModified;
	private Date buildLastdate;
	private String buildLastid;
	private String buildCursor;
	private Long buildRecords;
	private Long buildSize;
	private Integer buildLastCount;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getIdx() {
		return idx;
	}
	public void setIdx(String idx) {
		this.idx = idx;
	}
	public Date getBuildLastModified() {
		return buildLastModified;
	}
	public void setBuildLastModified(Date buildLastModified) {
		this.buildLastModified = buildLastModified;
	}
	public Date getBuildLastdate() {
		return buildLastdate;
	}
	public void setBuildLastdate(Date buildLastdate) {
		this.buildLastdate = buildLastdate;
	}
	public String getBuildLastid() {
		return buildLastid;
	}
	public void setBuildLastid(String buildLastid) {
		this.buildLastid = buildLastid;
	}
	public String getBuildCursor() {
		return buildCursor;
	}
	public void setBuildCursor(String buildCursor) {
		this.buildCursor = buildCursor;
	}
	public Long getBuildRecords() {
		return buildRecords;
	}
	public void setBuildRecords(Long buildRecords) {
		this.buildRecords = buildRecords;
	}
	public Long getBuildSize() {
		return buildSize;
	}
	public void setBuildSize(Long buildSize) {
		this.buildSize = buildSize;
	}
	public Integer getBuildLastCount() {
		return buildLastCount;
	}
	public void setBuildLastCount(Integer buildLastCount) {
		this.buildLastCount = buildLastCount;
	}
	private String idx;


}
