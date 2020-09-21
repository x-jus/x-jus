package br.jus.trf2.xjus.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexBuildStatus;
import br.jus.trf2.xjus.model.IndexRefreshStatus;
import br.jus.trf2.xjus.model.IndexStatus;
import br.jus.trf2.xjus.services.IPersistence;

public class Dao implements Closeable, IPersistence {
	private EntityManager em;
	public static ThreadLocal<Dao> current = new ThreadLocal<>();

	public Dao() {
		current.set(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#sysDate()
	 */
	@Override
	public Date sysDate() {
		return new Date();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#loadIndexes()
	 */
	@Override
	public List<Index> loadIndexes() {
		List<Index> l = new ArrayList<>();
		for (String i : Prop.getList("indexes")) {
			l.add(loadIndex(i));
		}
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#loadIndex(java.lang.String)
	 */
	@Override
	public Index loadIndex(String i) {
		Index idx = new Index();
		idx.setIdx(i);
		idx.setApi(Prop.get("index." + i + ".api"));
		idx.setActive(Prop.getBool("index." + i + ".active"));
		idx.setDescr(Prop.get("index." + i + ".descr"));
		idx.setMaxBuild(Prop.getInt("index." + i + ".build.docs.per.min"));
		idx.setMaxRefresh(Prop.getInt("index." + i + ".refresh.docs.per.min"));
		idx.setSecret(Prop.get("index." + i + ".secret"));
		idx.setToken(Prop.get("index." + i + ".token"));
		return idx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#loadIndexStatus(java.lang.String)
	 */
	@Override
	public IndexStatus loadIndexStatus(String idx) {
		return fileLoad(filenameStatus(idx), IndexStatus.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.jus.trf2.xjus.util.IPersistence#loadIndexBuildStatus(java.lang.String)
	 */
	@Override
	public IndexBuildStatus loadIndexBuildStatus(String idx) {
		return fileLoad(filenameBuildStatus(idx), IndexBuildStatus.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.jus.trf2.xjus.util.IPersistence#loadIndexRefreshStatus(java.lang.String)
	 */
	@Override
	public IndexRefreshStatus loadIndexRefreshStatus(String idx) {
		return fileLoad(filenameRefreshStatus(idx), IndexRefreshStatus.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#removeIndex(java.lang.String)
	 */
	@Override
	public void removeIndex(String idx) {
		removeFile(filenameStatus(idx));
	}

	@Override
	public void removeIndexBuildStatus(String idx) {
		removeFile(filenameBuildStatus(idx));
	}

	@Override
	public void removeIndexRefreshStatus(String idx) {
		removeFile(filenameRefreshStatus(idx));
	}

	private void removeFile(String filename) {
		new File(filename).delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.jus.trf2.xjus.util.IPersistence#saveIndex(br.jus.trf2.xjus.model.Index)
	 */
	@Override
	public void saveIndex(Index idx) {
		fileSave(filenameStatus(idx.getIdx()), idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.jus.trf2.xjus.util.IPersistence#saveIndexBuildStatus(br.jus.trf2.xjus.
	 * model.IndexBuildStatus)
	 */
	@Override
	public void saveIndexBuildStatus(IndexBuildStatus sts) {
		fileSave(filenameBuildStatus(sts.getIdx()), sts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.jus.trf2.xjus.util.IPersistence#saveIndexRefreshStatus(br.jus.trf2.xjus.
	 * model.IndexRefreshStatus)
	 */
	@Override
	public void saveIndexRefreshStatus(IndexRefreshStatus sts) {
		fileSave(filenameRefreshStatus(sts.getIdx()), sts);
	}

	private <T> T fileLoad(String filename, Class<T> clazz) {
		File file = new File(filename);
		if (!file.exists())
			return null;
		try (FileInputStream fis = new FileInputStream(filename)) {
			if (fis == null)
				return null;
			Yaml yaml = new Yaml(new Constructor(clazz)); // add to threadlocal
			T sts = (T) yaml.load(fis);
			return sts;
		} catch (Exception e) {
			throw new RuntimeException("Erro lendo " + filename, e);
		}
	}

	private void fileSave(String filename, Object sts) {
		try (FileOutputStream fis = new FileOutputStream(filename)) {
			Yaml yaml = new Yaml(); // add to threadlocal
			fis.write(yaml.dumpAs(sts, Tag.MAP, null).getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new RuntimeException("Erro gravando " + filename, e);
		}
	}

	private String filenameStatus(String idx) {
		return Prop.get("status.dir") + "/index-" + idx + "-status.yaml";
	}

	private String filenameBuildStatus(String idx) {
		return Prop.get("status.dir") + "/index-" + idx + "-status-build.yaml";
	}

	private String filenameRefreshStatus(String idx) {
		return Prop.get("status.dir") + "/index-" + idx + "-status-refresh.yaml";
	}

}
