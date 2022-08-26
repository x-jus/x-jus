package br.jus.trf2.xjus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;

import com.crivano.swaggerservlet.SwaggerServlet;

import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ISearch;

@RequestScoped
public class IndexIdxQueryGet implements IXjus.IIndexIdxQueryGet {

	@Override
	public void run(Request req, Response resp, XjusContext ctx) throws Exception {
		Integer page = req.page != null ? Integer.valueOf(req.page) : null;
		Integer perpage = req.perpage != null ? Integer.valueOf(req.perpage) : null;

		ISearch search = XjusFactory.getSearch();

		if (page == null || page < 1)
			page = 1;

		if (perpage == null || perpage > 100)
			perpage = 20;

		String acl = req.acl;

		// Get indexes password
		try (IPersistence dao = XjusFactory.getDao()) {
			Index idx = dao.loadIndex(req.idx);

			// If index is protected by a password, get ACL from the Authorization
			// header's token, else set ACL to PUBLIC
			if (idx.getSecret() != null) {
				String jwt = SwaggerServlet.getHttpServletRequest().getHeader("Authorization");
				if (jwt != null) {
					if (jwt.startsWith("Bearer "))
						jwt = jwt.substring(7);
					Map<String, Object> jwtMap = Utils.jwtVerify(jwt, idx.getSecret());
					String jwtAcl = (String) jwtMap.get("acl");
					if (jwtAcl != null) {
						// It would be great to filter the requested ACL so that it
						// conforms to the
						if (acl != null) {
							String[] splitReq = acl.split(";");
							List<String> splitJwt = Arrays.asList(jwtAcl.split(";"));
							for (String s : splitReq) {
								if (!splitJwt.contains(s))
									throw new Exception("Acesso requisitado '" + s
											+ "' não está presente no cabeçalho de autorização '" + jwtAcl + "'");
							}
						} else
							acl = jwtAcl;
					} else
						acl = "PUBLIC";
				} else {
					if (acl == null)
						acl = "PUBLIC";
					else if (!"PUBLIC".equals(acl))
						throw new Exception("Acesso requisitado '" + acl
								+ "' só pode ser 'PUBLIC' para chamadas sem cabeçalho de autorização");
				}
			}

			search.query(req.idx, req.filter, req.facets, page, perpage, acl, req.code, req.fromDate, req.toDate, resp);
		}

	}

	public String getContext() {
		return "pesquisar";
	}
}
