package br.jus.trf2.xjus.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import br.jus.trf2.xjus.Utils;

public class Prop {
	public interface IPropertyProvider {
		String getProp(String nome);

		void addPrivateProperty(String name);

		void addRestrictedProperty(String name);

		void addPublicProperty(String name);

		void addPrivateProperty(String name, String defaultValue);

		void addRestrictedProperty(String name, String defaultValue);

		void addPublicProperty(String name, String defaultValue);
	}

	static public IPropertyProvider provider = null;

	public static void setProvider(IPropertyProvider prov) {
		provider = prov;
	}

	public static String get(String nome) {
		return provider.getProp(nome);
	}

	public static Boolean getBool(String nome) {
		String p = Prop.get(nome);
		if (p == null)
			return null;
		return Boolean.valueOf(p.trim());
	}

	public static Integer getInt(String nome) {
		String p = Prop.get(nome);
		if (p == null)
			return null;
		return Integer.valueOf(p.trim());
	}

	public static List<String> getList(String nome) {
		String p = Prop.get(nome);
		if (p == null)
			return null;
		return Arrays.asList(p.split(","));
	}

	public static Date getData(String nome) {
		DateFormat formatter = new SimpleDateFormat("dd/MM/yy");

		String s = get(nome);

		try {
			return (Date) formatter.parse(s);
		} catch (Exception nfe) {
			throw new RuntimeException("Erro ao converter propriedade string em data");
		}
	}

	public static void defineProperties() {
		provider.addRestrictedProperty("elasticsearch.url", "http://localhost:9200");
		provider.addRestrictedProperty("elasticsearch.auth.basic.user", null);
		if (get("elasticsearch.auth.basic.user") != null)
			provider.addPrivateProperty("elasticsearch.auth.basic.password");
		else
			provider.addPrivateProperty("elasticsearch.auth.basic.password", null);
		provider.addPublicProperty("elasticsearch.ssl.skip.hostname.verification", "false");

		provider.addPublicProperty("working.hours.start", "8");
		provider.addPublicProperty("working.hours.end", "17");

		provider.addRestrictedProperty("status.dir", "/var/tmp");
		provider.addPublicProperty("indexes");
		for (String i : getList("indexes")) {
			provider.addRestrictedProperty("index." + i + ".api");
			provider.addPublicProperty("index." + i + ".active", "true");
			provider.addPublicProperty("index." + i + ".descr", "");
			provider.addPublicProperty("index." + i + ".build.docs.per.min", "10");
			provider.addPublicProperty("index." + i + ".build.docs.per.min.non.working.hours",
					provider.getProp("index." + i + ".build.docs.per.min"));
			provider.addPublicProperty("index." + i + ".refresh.docs.per.min", "5");
			provider.addPublicProperty("index." + i + ".refresh.docs.per.min.non.working.hours",
					provider.getProp("index." + i + ".refresh.docs.per.min"));
			provider.addPrivateProperty("index." + i + ".secret");
			provider.addPrivateProperty("index." + i + ".token");
			provider.addPublicProperty("index." + i + ".create.json",
					Utils.convertStreamToString(Prop.class.getResourceAsStream("create-index.json")));
			provider.addPublicProperty("index." + i + ".query.json",
					Utils.convertStreamToString(Prop.class.getResourceAsStream("search.json")));
			provider.addPublicProperty("index." + i + ".list.ids.json",
					Utils.convertStreamToString(Prop.class.getResourceAsStream("list-ids.json")));
		}
	}
}