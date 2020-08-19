package br.jus.trf2.xjus.services;

public interface ITask {
	void addRefreshIndex(String idx);

	void addRefreshDocument(String idx, String id);

	int getRefreshTaskCount();

	int getBuildTaskCount();

	void addBuildIndex(String idx);

	void addBuildDocument(String idx, String id);

}
