package br.jus.trf2.xjus.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import com.crivano.swaggerservlet.SwaggerUtils;

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
		idx.setMaxBuildNonWorkingHours(Prop.getInt("index." + i + ".build.docs.per.min.non.working.hours"));
		idx.setMaxRefresh(Prop.getInt("index." + i + ".refresh.docs.per.min"));
		idx.setMaxRefreshNonWorkingHours(Prop.getInt("index." + i + ".refresh.docs.per.min.non.working.hours"));
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

	@Override
	public void deleteIndexRefreshStatus(IndexRefreshStatus sts) {
		fileDelete(filenameRefreshStatus(sts.getIdx()));
	}

	private <T> T fileLoad(String filename, Class<T> clazz) {
		File file = new File(filename);
		if (!file.exists())
			return null;
		try (FileInputStream fis = new FileInputStream(filename)) {
			if (fis == null)
				return null;
			Yaml yaml = new Yaml(new Constructor(clazz)); // add to threadlocal
			try {
				T sts = (T) yaml.load(fis);
				return sts;
			} catch (Exception ex) {
				file.delete();
				throw new Exception("Arquivo removido", ex);
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro lendo " + filename, e);
		}
	}

	private void fileSave(String filename, Object sts) {
		Yaml yaml = new Yaml(); // add to threadlocal
		byte[] bytes = yaml.dumpAs(sts, Tag.MAP, null).getBytes(StandardCharsets.UTF_8);
		ByteBuffer buff = ByteBuffer.wrap(bytes);
		try (FileOutputStream fis = new FileOutputStream(filename, false);
				FileChannel channel = fis.getChannel();
				FileLock lock = channel.lock()) {
			channel.write(buff);
		} catch (Exception e) {
			throw new RuntimeException("Erro gravando " + filename, e);
		}
	}

	public boolean tryToTouchBuildLock() {
		String filename = filenameBuildLock();
		return tryToTouchLock(filename);
	}

	public boolean tryToTouchRefreshLock() {
		String filename = filenameRefreshLock();
		return tryToTouchLock(filename);
	}

	private boolean tryToTouchLock(String filename) {
		Path path = Paths.get(filename);

		try (RandomAccessFile file = new RandomAccessFile(filename, "rw");
				FileChannel channel = file.getChannel();
				FileLock lock = channel.tryLock()) {
			if (lock != null && lock.isValid()) {
				Long time = null;
				if (file.length() >= 8)
					time = file.readLong();
				Instant now = Instant.now();
				if (time != null) {
					Instant lastStart = Instant.ofEpochMilli(time);
					if (lastStart.plusSeconds(50).isAfter(now)) {
						SwaggerUtils.log(this.getClass())
								.info("Outra instância atualizou " + filename + " às " + lastStart.toString());
						return false;
					}
				}
				file.seek(0L);
				file.writeLong(now.toEpochMilli());
				SwaggerUtils.log(this.getClass()).info("Esta instância atualizou " + filename + " às " + now.toString());
				return true;
			}
		} catch (OverlappingFileLockException e) {
			SwaggerUtils.log(this.getClass()).info("Outra instância bloqueou " + filename);

		} catch (Exception e) {
			throw new RuntimeException("Erro acessando " + filename, e);
		}
		return false;
	}

	private void fileDelete(String filename) {
		File file = new File(filename);
		file.delete();
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

	private String filenameBuildLock() {
		return Prop.get("status.dir") + "/lock-build.yaml";
	}

	private String filenameRefreshLock() {
		return Prop.get("status.dir") + "/lock-refresh.yaml";
	}

}
