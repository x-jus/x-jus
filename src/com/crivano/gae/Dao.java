package com.crivano.gae;

import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

public class Dao {

	public <T> T load(String key, Class<T> clazz) {
		Key<T> k = Key.create(key);
		T v = ofy().load().key(k).now();
		return v;
	}

	public <T> T load(T pojo) {
		Key<T> k = Key.create(pojo);
		T v = ofy().load().key(k).now();
		return v;
	}

	public <T> T load(Key<T> key) {
		Key<T> k = key;
		T v = ofy().load().key(k).now();
		return v;
	}

	public <T> List<T> loadAll(Class<T> clazz) {
		List<T> l = ofy().load().type(clazz).list();
		return l;
	}

	public <T> void save(final T data) {
		ofy().save().entity(data).now();
	}

	public <T> T del(String key, Class<T> clazz) {
		T v = load(key, clazz);
		return del(v);
	}

	public <T> T del(final T data) {
		ofy().delete().entity(data).now();
		return data;
	}

	public <T> String webSafeKey(final T data) {
		return Key.create(data).toWebSafeString();
	}

	public Objectify ofy() {
		return ObjectifyService.ofy();
	}

}
