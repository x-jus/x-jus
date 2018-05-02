package br.jus.trf2.xjus.record.api;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.crivano.gae.HttpGAE;
import com.crivano.swaggerservlet.SwaggerCall;
import com.crivano.swaggerservlet.SwaggerServlet;

public class JurindexMockRecordServlet extends SwaggerServlet {
	private static final long serialVersionUID = 1756711359239182178L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		SwaggerCall.setHttp(new HttpGAE());

		super.setAPI(IJurindexRecordAPI.class);

		super.setActionPackage("br.jus.trf2.xjus.record.api");
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doPost(req, resp);
	}

	@Override
	public String getService() {
		return "mock record api";
	}

}
