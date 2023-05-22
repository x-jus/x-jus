package br.jus.trf2.xjus.services.jboss;

import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import br.jus.trf2.xjus.XjusServlet;

//@JMSDestinationDefinitions(value = {
//@JMSDestinationDefinition(name = "java:/queue/Xjus", interfaceName = "javax.jms.Queue", destinationName = "Xjus") })

@MessageDriven(name = "Xjus", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/Xjus"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class XjusWorker implements MessageListener {

	private static final Logger LOGGER = Logger.getLogger(XjusWorker.class.toString());

	/**
	 * @see MessageListener#onMessage(Message)
	 */
	public void onMessage(Message rcvMessage) {
		try {
			if (rcvMessage instanceof ObjectMessage) {
				Object o = ((ObjectMessage) rcvMessage).getObject();
				if (o instanceof JbossMessage) {
					JbossMessage msg = (JbossMessage) o;
					LOGGER.fine("received Message from queue");
					try {
						XjusServlet.getInstance().execute(msg.method, msg.pathInfo, null);
					} catch (Exception ex) {
						throw new RuntimeException("Erro executando " + msg.method + ": " + msg.pathInfo, ex);
					}
				}
			} else {
				LOGGER.warning("Message of wrong type: " + rcvMessage.getClass().getName());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}