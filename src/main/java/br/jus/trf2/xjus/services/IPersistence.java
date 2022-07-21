package br.jus.trf2.xjus.services;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexBuildStatus;
import br.jus.trf2.xjus.model.IndexRefreshStatus;
import br.jus.trf2.xjus.model.IndexStatus;

public interface IPersistence extends Closeable {

	Date sysDate();

	List<Index> loadIndexes();

	Index loadIndex(String idx);

	IndexStatus loadIndexStatus(String idx);

	IndexBuildStatus loadIndexBuildStatus(String idx);

	IndexRefreshStatus loadIndexRefreshStatus(String idx);

	void removeIndex(String idx);

	void removeIndexRefreshStatus(String idx);

	void removeIndexBuildStatus(String idx);

	void saveIndex(Index idx);

	void saveIndexBuildStatus(IndexBuildStatus sts);

	void saveIndexRefreshStatus(IndexRefreshStatus sts);

	void deleteIndexRefreshStatus(IndexRefreshStatus sts);

	boolean tryToTouchBuildLock();

	boolean tryToTouchRefreshLock();

	default void close() throws IOException {
	}

	default void rollback() {
	}

}