package br.jus.trf2.xjus.record.api;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.crivano.swaggerservlet.SwaggerServlet;

public class XjusMockRecordServlet extends SwaggerServlet {
	private static final long serialVersionUID = 1756711359239182178L;

	@Override
	public void initialize(ServletConfig config) throws ServletException {
		super.setAPI(IXjusRecordAPI.class);

		super.setActionPackage("br.jus.trf2.xjus.record.api");
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}

}
