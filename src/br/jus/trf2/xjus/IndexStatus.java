package br.jus.trf2.xjus;

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
	public String last;
	public Long records;
	public Long size;
}
