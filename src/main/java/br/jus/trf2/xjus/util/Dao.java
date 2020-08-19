package br.jus.trf2.xjus.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexBuildStatus;
import br.jus.trf2.xjus.model.IndexRefreshStatus;
import br.jus.trf2.xjus.model.IndexStatus;
import br.jus.trf2.xjus.services.IPersistence;

public class Dao implements Closeable, IPersistence {
	private EntityManager em;
	public static ThreadLocal<Dao> current = new ThreadLocal<>();

	public Dao() {
		this.em = PersistenceManager.INSTANCE.getEntityManager();
		current.set(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#sysDate()
	 */
	@Override
	public Date sysDate() {
		return (Date) em.createNativeQuery("select sysdate() from dual").getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#loadIndexes()
	 */
	@Override
	public List<Index> loadIndexes() {
		return (List<Index>) em.createQuery("select from Index").getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#loadIndex(java.lang.String)
	 */
	@Override
	public Index loadIndex(String idx) {
		return (Index) em.createQuery("select from Index is where is.idx = :idx").setParameter("idx", idx)
				.getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#loadIndexStatus(java.lang.String)
	 */
	@Override
	public IndexStatus loadIndexStatus(String idx) {
		return (IndexStatus) em.createQuery("select from IndexStatus is where is.idx = :idx").setParameter("idx", idx)
				.getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.jus.trf2.xjus.util.IPersistence#loadIndexBuildStatus(java.lang.String)
	 */
	@Override
	public IndexBuildStatus loadIndexBuildStatus(String idx) {
		return (IndexBuildStatus) em.createQuery("select from IndexBuildStatus is where is.idx = :idx")
				.setParameter("idx", idx).getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.jus.trf2.xjus.util.IPersistence#loadIndexRefreshStatus(java.lang.String)
	 */
	@Override
	public IndexRefreshStatus loadIndexRefreshStatus(String idx) {
		return (IndexRefreshStatus) em.createQuery("select from IndexRefreshStatus is where is.idx = :idx")
				.setParameter("idx", idx).getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#removeIndex(java.lang.String)
	 */
	@Override
	public void removeIndex(String idx) {
		beginTransaction();
		Index entity = (Index) em.createQuery("select from IndexStatus is where is.idx = :idx").setParameter("idx", idx)
				.getSingleResult();
		em.remove(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.jus.trf2.xjus.util.IPersistence#saveIndex(br.jus.trf2.xjus.model.Index)
	 */
	@Override
	public void saveIndex(Index idx) {
		persist(idx);
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
		persist(sts);
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
		persist(sts);
	}

	private void beginTransaction() {
		this.em.getTransaction().begin();
	}

	private void rollbackTransaction() {
		if (this.em.getTransaction().isActive())
			this.em.getTransaction().rollback();
	}

	private static void rollbackCurrentTransaction() {
		current.get().rollbackTransaction();
	}

	private <T> T find(Class<T> clazz, Long id) {
		return em.find(clazz, id);
	}

	private void persist(Object o) {
		if (!em.getTransaction().isActive())
			beginTransaction();
		this.em.persist(o);
		this.em.flush();
	}

	private void remove(Object o) {
		if (!em.getTransaction().isActive())
			beginTransaction();
		this.em.remove(o);
		this.em.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.jus.trf2.xjus.util.IPersistence#close()
	 */
	@Override
	public void close() throws IOException {
		if (em != null) {
			if (em.getTransaction().isActive()) {
				em.flush();
				em.getTransaction().commit();
			}
			em.close();
		}
	}

	@Override
	public void removeIndexRefreshStatus(String idx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeIndexBuildStatus(String idx) {
		// TODO Auto-generated method stub

	}

}
