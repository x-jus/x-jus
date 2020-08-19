package br.jus.trf2.xjus.services.jboss;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.crivano.swaggerservlet.SwaggerUtils;

/**
 * Demonstrates how to use the EJB's @Timeout.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@Singleton
@Startup
public class JBossTimer {

	@Resource
	private TimerService timerService;

	@Timeout
	public void scheduler(Timer timer) throws Exception {
		Date currentTime = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
		System.out.println("TimeoutExample.scheduler() " + timer.getInfo() + simpleDateFormat.format(currentTime));
		Context envContext = null;
		try {
			Context initContext = new InitialContext();
			envContext = (Context) initContext.lookup("java:");
			ConnectionFactory connectionFactory = (ConnectionFactory) envContext
					.lookup("java:jboss/DefaultJMSConnectionFactory");
			Destination destination = (Destination) envContext.lookup("java:jboss/exported/jms/queue/Xjus");
			try (JMSContext context = connectionFactory.createContext("test", "test123")) {
				JbossMessage msg = new JbossMessage();
				msg.method = "get";
				msg.pathInfo = "/api/v1/task/build-step";
				System.out.println("Sending message with content: " + msg);
				// Send the specified number of messages
				context.createProducer().send(destination, msg);
			} catch (Exception ex) {
				SwaggerUtils.log(this.getClass()).error("Não consegui enviar mensagem para a fila", ex);
			}
		} finally {
			if (envContext != null) {
				envContext.close();
			}
		}
	}

	@PostConstruct
	public void initialize() {
		ScheduleExpression se = new ScheduleExpression();
		// Set schedule to every 3 seconds (starting at second 0 of every minute).
		se.hour("*").minute("*").second("0/15");
		timerService.createCalendarTimer(se, new TimerConfig("EJB timer service timeout at ", false));
	}

	@PreDestroy
	public void stop() {
		System.out.println("EJB Timer: Stop timers.");
		for (Timer timer : timerService.getTimers()) {
			System.out.println("Stopping timer: " + timer.getInfo());
			timer.cancel();
		}
	}
}