package br.jus.trf2.xjus;

import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ISearch;
import br.jus.trf2.xjus.services.ITask;
import br.jus.trf2.xjus.services.jboss.JBossElastic;
import br.jus.trf2.xjus.services.jboss.JBossTaskImpl;
import br.jus.trf2.xjus.util.Dao;

public class XjusFactory {

	public static IPersistence getDao() {
		return new Dao();
	}

	public static ISearch getSearch() {
		return JBossElastic.INSTANCE;
	}

	public static ITask getQueue() {
		return new JBossTaskImpl();
	}

}
