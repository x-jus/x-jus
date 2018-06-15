package br.jus.trf2.xjus.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

@Entity
@Unindex
public class IndexStatus {
	@Id
	public String idx;

	public Date lastModified;
	public Long records;
	public Long size;
}
