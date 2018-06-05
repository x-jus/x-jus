package br.jus.trf2.xjus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import br.jus.trf2.xjus.IXjus.Facet;
import br.jus.trf2.xjus.IXjus.FacetValue;
import br.jus.trf2.xjus.IXjus.IndexIdxQueryGetRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxQueryGetResponse;
import br.jus.trf2.xjus.IXjus.Record;

import com.crivano.gae.Dao;
import com.crivano.gae.Search;
import com.crivano.gae.Search.SearchResults;
import com.crivano.swaggerservlet.SwaggerServlet;
import com.google.appengine.api.search.FacetResult;
import com.google.appengine.api.search.FacetResultValue;
import com.google.appengine.api.search.ScoredDocument;
import com.googlecode.objectify.Key;

public class IndexIdxQueryGet implements IXjus.IIndexIdxQueryGet {

	@Override
	public void run(IndexIdxQueryGetRequest req, IndexIdxQueryGetResponse resp)
			throws Exception {
		Integer page = Integer.valueOf(req.page);
		Integer perpage = Integer.valueOf(req.perpage);

		if (page == null || page < 1)
			page = 1;

		if (perpage == null || perpage > 100)
			perpage = 20;

		String acl = req.acl;

		// Get indexes password
		Dao dao = new Dao();
		Key<Index> key = Key.create(Index.class, req.idx);
		Index idx = dao.load(key);

		// If index is protected by a password, get ACL from the Authorization
		// header's token, else set ACL to PUBLIC
		if (idx.secret != null) {
			String jwt = SwaggerServlet.getHttpServletRequest().getHeader(
					"Authorization");
			if (jwt != null) {
				if (jwt.startsWith("Bearer "))
					jwt = jwt.substring(7);
				Map<String, Object> jwtMap = Utils.jwtVerify(jwt, idx.secret);
				String jwtAcl = (String) jwtMap.get("acl");
				if (jwtAcl != null) {
					// It would be great to filter the requested ACL so that it
					// conforms to the
					if (acl != null) {
						String[] splitReq = acl.split(";");
						List<String> splitJwt = Arrays
								.asList(jwtAcl.split(";"));
						for (String s : splitReq) {
							if (!splitJwt.contains(s))
								throw new Exception(
										"Acesso requisitado '"
												+ s
												+ "' não está presente no cabeçalho de autorização '"
												+ jwtAcl + "'");
						}
					} else
						acl = jwtAcl;
				} else
					acl = "PUBLIC";
			} else {
				if (acl == null)
					acl = "PUBLIC";
				else if (!"PUBLIC".equals(acl))
					throw new Exception(
							"Acesso requisitado '"
									+ acl
									+ "' só pode ser 'PUBLIC' para chamadas sem cabeçalho de autorização");
			}
		}

		SearchResults ret = Search.search(req.idx, req.filter, req.facets,
				page, perpage, acl);

		resp.count = (double) ret.result.getNumberFound();

		resp.facets = new ArrayList<>();
		for (FacetResult f : ret.result.getFacets()) {
			Facet facet = new Facet();
			facet.name = f.getName();
			facet.values = new ArrayList<>();
			for (FacetResultValue v : f.getValues()) {
				FacetValue value = new FacetValue();
				value.name = v.getLabel();
				value.count = (double) v.getCount();
				value.refinementToken = v.getRefinementToken();
				facet.values.add(value);
			}
			resp.facets.add(facet);
		}

		resp.results = new ArrayList<>();
		for (ScoredDocument r : ret.result.getResults()) {
			Record rec = new Record();
			rec.url = r.getFields("url").iterator().next().getAtom();
			rec.title = r.getFields("title").iterator().next().getText();
			rec.code = r.getFields("code").iterator().next().getAtom();
			rec.content = r.getExpressions().iterator().next().getHTML();
			resp.results.add(rec);
		}

	}

	public String getContext() {
		return "pesquisar";
	}
}
