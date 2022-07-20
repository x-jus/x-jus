package br.jus.trf2.xjus.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import br.jus.trf2.xjus.util.Prop;

@Entity
@Table(name = "index")
public class Index {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String idx;

	private String descr;
	private String api;
	private String token;
	private Boolean active;
	private Integer maxBuild;
	private Integer maxBuildNonWorkingHours;
	private Integer maxRefresh;
	private Integer maxRefreshNonWorkingHours;

	// Secret to verify the JWT that authenticates the request
	private String secret;

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

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Integer getMaxBuild() {
		return maxBuild;
	}

	public void setMaxBuild(Integer maxBuild) {
		this.maxBuild = maxBuild;
	}

	public Integer getMaxBuildNonWorkingHours() {
		return maxBuildNonWorkingHours;
	}

	public void setMaxBuildNonWorkingHours(Integer maxBuildNonWorkingHours) {
		this.maxBuildNonWorkingHours = maxBuildNonWorkingHours;
	}

	public Integer getCurrentMaxBuild() {
		if (isNonWorkingHours())
			return getMaxBuildNonWorkingHours();
		return getMaxBuild();
	}

	private boolean isNonWorkingHours() {
		Date dt = new Date();
		boolean f = dt.getHours() < Prop.getInt("working.hours.start")
				|| dt.getHours() >= Prop.getInt("working.hours.end") || dt.getDay() == 0 || dt.getDay() == 6;
		return f;
	}

	public Integer getMaxRefresh() {
		return maxRefresh;
	}

	public void setMaxRefresh(Integer maxRefresh) {
		this.maxRefresh = maxRefresh;
	}

	public Integer getMaxRefreshNonWorkingHours() {
		return maxRefreshNonWorkingHours;
	}

	public void setMaxRefreshNonWorkingHours(Integer maxRefreshNonWorkingHours) {
		this.maxRefreshNonWorkingHours = maxRefreshNonWorkingHours;
	}

	public Integer getCurrentMaxRefresh() {
		if (isNonWorkingHours())
			return getMaxRefreshNonWorkingHours();
		return getMaxRefresh();
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

}
