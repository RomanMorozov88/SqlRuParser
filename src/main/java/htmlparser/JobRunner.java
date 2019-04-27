package htmlparser;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Создаём работу с методом execute в котором и происходят все нужные нам действия.
 */
@PersistJobDataAfterExecution
public class JobRunner implements Job {

    private static final Logger logger = Logger.getLogger(JobRunner.class);

    public static final String COUNT = "count";
    public static final String PREVDATE = "prevdate";

    @Override
    public void execute(JobExecutionContext context) {

        logger.info(">>>Start");

        JobDataMap data = context.getJobDetail().getJobDataMap();
        int count = data.getInt(COUNT);
        LocalDateTime prev = (LocalDateTime) data.get(PREVDATE);

        BiPredicate<LocalDateTime, LocalDateTime> timeFilter;
        if (count == 1) {
            timeFilter = ParserSQLru::predicateForFirstRun;
        } else {
            timeFilter = ParserSQLru::predicateForAnotherRun;
        }

        Config config = new Config();
        config.init();
        String target = config.get("SQL.ru");

        ParserSQLru sm = new ParserSQLru();
        int pages = 0;
        try {
            pages = sm.countOfPages(target);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.info(">>>TOTAL PAGES: " + pages);

        try (DBworker worker = new DBworker()) {
            worker.init(config);
            ThreadPool pool = new ThreadPool();
            for (int i = 1; i <= pages; i++) {
                pool.work(new ParsingTask(sm, config, timeFilter, prev, i));
            }
            logger.info(">>>Parsing by threads.");
            pool.threadPoolInit();
            pool.joiningThreads();
            pool.shutdown();
            List<Vacancy> finalVacanciesList = pool.getResultList();
            logger.info(">>>Add in DB.");
            worker.add(finalVacanciesList);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }

        count++;
        prev = LocalDateTime.now();
        data.put(COUNT, count);
        data.put(PREVDATE, prev);

        logger.info(">>>End.");
    }
}
