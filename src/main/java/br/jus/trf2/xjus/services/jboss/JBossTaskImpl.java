package br.jus.trf2.xjus.services.jboss;

import java.util.Properties;
import java.util.logging.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import br.jus.trf2.xjus.services.ITask;

public class JBossTaskImpl implements ITask {
	private static final Logger log = Logger.getLogger(JBossTaskImpl.class.getName());

	// Set up all the default values
	private static final String DEFAULT_MESSAGE = "Hello, World!";
	private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String DEFAULT_DESTINATION = "jms/queue/Xjus";
	private static final String DEFAULT_MESSAGE_COUNT = "1";
	private static final String DEFAULT_USERNAME = "test";
	private static final String DEFAULT_PASSWORD = "test123";
	private static final String INITIAL_CONTEXT_FACTORY = "org.wildfly.naming.client.WildFlyInitialContextFactory";
	private static final String PROVIDER_URL = "http-remoting://127.0.0.1:8080";

	public void add(String method, String url) {

		Context namingContext = null;

		try {
			String userName = System.getProperty("username", DEFAULT_USERNAME);
			String password = System.getProperty("password", DEFAULT_PASSWORD);

			// Set up the namingContext for the JNDI lookup
			final Properties env = new Properties();
			env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
			env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
			env.put(Context.SECURITY_PRINCIPAL, userName);
			env.put(Context.SECURITY_CREDENTIALS, password);
			namingContext = new InitialContext(env);

			// Perform the JNDI lookups
			String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
			log.info("Attempting to acquire connection factory \"" + connectionFactoryString + "\"");
			ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(connectionFactoryString);
			log.info("Found connection factory \"" + connectionFactoryString + "\" in JNDI");

			String destinationString = System.getProperty("destination", DEFAULT_DESTINATION);
			log.info("Attempting to acquire destination \"" + destinationString + "\"");
			Destination destination = (Destination) namingContext.lookup(destinationString);
			log.info("Found destination \"" + destinationString + "\" in JNDI");

			int count = Integer.parseInt(System.getProperty("message.count", DEFAULT_MESSAGE_COUNT));
			String content = System.getProperty("message.content", DEFAULT_MESSAGE);

			try (JMSContext context = connectionFactory.createContext(userName, password)) {
				JbossMessage msg = new JbossMessage();
				msg.method = method;
				msg.pathInfo = url;
				log.info("Sending message with content: " + msg);
				// Send the specified number of messages
				context.createProducer().send(destination, content);

				// Create the JMS consumer
				JMSConsumer consumer = context.createConsumer(destination);
				// Then receive the same number of messages that were sent
				JbossMessage msg2 = consumer.receiveBody(JbossMessage.class, 5000);
				log.info("Received message with content " + msg2);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Erro enviando mensagem para a fila", ex);
		} finally {
			if (namingContext != null) {
				try {
					namingContext.close();
				} catch (NamingException e) {
					log.severe(e.getMessage());
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
