package br.jus.trf2.xjus.services.jboss;

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

import br.jus.trf2.xjus.XjusFactory;
import br.jus.trf2.xjus.XjusServlet;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.util.Prop;

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
		try (IPersistence dao = XjusFactory.getDao()) {
			if (dao.tryToTouchBuildLock())
				JBossTaskImpl.add("get", "/api/v1/task/build-step");
		}
	}

	@PostConstruct
	public void initialize() {
		Runnable r = new StartTimer();
		Thread th = new Thread(r, "Start-Timer-Build");
		th.start();
	}

	private class StartTimer implements Runnable {
		public void run() {
			while (XjusServlet.getInstance() == null) {
				System.out.println("Aguardando inicialização do webservice para iniciar o timer de build...");
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			ScheduleExpression se = new ScheduleExpression();
			// Set schedule to every 60 seconds (starting at second 30 of every minute).
			Integer mins = Prop.getInt("wake.up.timer.in.min");
			String m = (mins / 2) + "/" + mins;
			String s = (mins & 1) == 0 ? "0" : "30";
			se.hour("*").minute(m).second(s);
			timerService.createCalendarTimer(se, new TimerConfig("EJB timer service timeout at ", false));
		}
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