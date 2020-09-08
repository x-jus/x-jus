package br.jus.trf2.xjus;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.crivano.swaggerservlet.SwaggerServlet;

import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ITask;
import br.jus.trf2.xjus.services.jboss.JBossTaskImpl;
import br.jus.trf2.xjus.util.Prop;
import br.jus.trf2.xjus.util.Prop.IPropertyProvider;

public class XjusServlet extends SwaggerServlet implements IPropertyProvider {
	private static final long serialVersionUID = 1756711359239182178L;
	public static IPersistence dao = XjusFactory.getDao();

	public static ITask task = new JBossTaskImpl();
	public static XjusServlet instance;

	@Override
	public void initialize(ServletConfig config) throws ServletException {
		instance = this;
//		SwaggerCall.setHttp(new HttpGAE());

		super.setAPI(IXjus.class);

		super.setActionPackage("br.jus.trf2.xjus");

		Prop.setProvider(this);

		Prop.defineGlobalProperties();

//		ObjectifyFactoryCreator.create();
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}

	@Override
	public String getProp(String nome) {
		return getProperty(nome);
	}

	public static XjusServlet getInstance() {
		return (XjusServlet) instance;
	}

}
