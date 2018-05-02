package br.jus.trf2.xjus;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

@Entity
@Unindex
public class Index {
	@Id
	public String idx;

	public String descr;
	public String api;
	public String token;
	public Boolean active;
}
