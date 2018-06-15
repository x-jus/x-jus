package com.crivano.gae;

import java.util.HashSet;
import java.util.Set;

import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexBuildStatus;
import br.jus.trf2.xjus.model.IndexRefreshStatus;
import br.jus.trf2.xjus.model.IndexStatus;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;

public class ObjectifyFactoryCreator {

	public static final ObjectifyFactory instance = ObjectifyService.factory();
	private static final Set<String> classes = new HashSet<>();

	private static void register(Class clazz) {
		if (classes.contains(clazz.getSimpleName()))
			throw new RuntimeException(
					"Can't accept two classes with the same simple name: "
							+ clazz.getSimpleName());
		classes.add(clazz.getSimpleName());
		instance.register(clazz);
	}

	public static void create() {
		JodaTimeTranslators.add(instance);

		register(Index.class);
		register(IndexStatus.class);
		register(IndexBuildStatus.class);
		register(IndexRefreshStatus.class);
	}

	public static ObjectifyFactory getInstance() {
		return instance;
	}

}