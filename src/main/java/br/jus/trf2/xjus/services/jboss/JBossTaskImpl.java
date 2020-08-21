package br.jus.trf2.xjus.services.jboss;

import java.util.logging.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.crivano.swaggerservlet.SwaggerUtils;

import br.jus.trf2.xjus.services.ITask;

public class JBossTaskImpl implements ITask {
	private static final Logger log = Logger.getLogger(JBossTaskImpl.class.getName());

	// Set up all the default values
	private static final String DEFAULT_CONNECTION_FACTORY = "java:jboss/DefaultJMSConnectionFactory";
	private static final String DEFAULT_DESTINATION = "java:jboss/exported/jms/queue/Xjus";
	private static final String DEFAULT_USERNAME = "test";
	private static final String DEFAULT_PASSWORD = "test123";

	public static void add(String method, String pathInfo) {
		Context envContext = null;
		try {
			Context initContext = new InitialContext();
			envContext = (Context) initContext.lookup("java:");
			ConnectionFactory connectionFactory = (ConnectionFactory) envContext.lookup(DEFAULT_CONNECTION_FACTORY);
			Destination destination = (Destination) envContext.lookup(DEFAULT_DESTINATION);
			try (JMSContext context = connectionFactory.createContext(DEFAULT_USERNAME, DEFAULT_PASSWORD)) {
				JbossMessage msg = new JbossMessage();
				msg.method = method;
				msg.pathInfo = pathInfo;
//				System.out.println("Sending message with content: " + msg);
				context.createProducer().send(destination, msg);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Não consegui enviar mensagem para a fila", ex);
		} finally {
			if (envContext != null) {
				try {
					envContext.close();
				} catch (NamingException e) {
					SwaggerUtils.log(JBossTaskImpl.class).error("Erro fechando contexto", e);
				}
			}
		}
	}

	@Override
	public void addRefreshIndex(String idx) {
		add("post", "/api/v1/task/" + idx + "/refresh-step");

	}

	@Override
	public void addRefreshDocument(String idx, String id) {
		add("post", "/api/v1/task/" + idx + "/record/" + id + "/refresh");

	}

	@Override
	public int getRefreshTaskCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBuildTaskCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addBuildIndex(String idx) {
		add("post", "/api/v1/task/" + idx + "/build-step");
	}

	@Override
	public void addBuildDocument(String idx, String id) {
		add("post", "/api/v1/task/" + idx + "/record/" + id + "/refresh");
	}
}
