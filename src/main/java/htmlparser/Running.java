package htmlparser;

import htmlparser.util.Config;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.LocalDateTime;

/**
 * Здесь создаём планировщик с работой и триггером а так же МАЙН-метод.
 */
public class Running {

    private static final Logger logger = Logger.getLogger(Running.class);

    public void run() throws SchedulerException {
        Config config = new Config();
        config.init();

        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = null;
        try {
            scheduler = sf.getScheduler();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }

        JobDetail job = JobBuilder.newJob(JobRunner.class).build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(config.get("cron.time")))
                .build();
        job.getJobDataMap().put(JobRunner.COUNT, 1);
        job.getJobDataMap().put(JobRunner.PREVDATE, LocalDateTime.now());

        scheduler.scheduleJob(job, trigger);
        scheduler.start();
    }

    public static void main(String[] args) {
        Running running = new Running();
        try {
            running.run();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }
}