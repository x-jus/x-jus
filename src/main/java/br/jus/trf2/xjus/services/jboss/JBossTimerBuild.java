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
public class JBossTimerBuild {

	@Resource
	private TimerService timerService;

	@Timeout
	public void scheduler(Timer timer) throws Exception {
		JBossTaskImpl.add("get", "/api/v1/task/build-step");
	}

	@PostConstruct
	public void initialize() {
		ScheduleExpression se = new ScheduleExpression();
		// Set schedule to every 60 seconds (starting at second 30 of every minute).
		se.hour("*").minute("*").second("30/60");
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