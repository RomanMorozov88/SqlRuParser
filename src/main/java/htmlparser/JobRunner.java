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

    public JobRunner() {
    }

    @Override
    public void execute(JobExecutionContext context) {

        JobDataMap data = context.getJobDetail().getJobDataMap();
        int count = data.getInt(COUNT);
        LocalDateTime prev = (LocalDateTime) data.get(PREVDATE);

        System.out.println(count);

        BiPredicate<LocalDateTime, LocalDateTime> timeFilter;
        if (count == 1) {
            timeFilter = ParserSQLru::predicateForFirstRun;
        } else {
            timeFilter = ParserSQLru::predicateForAnotherRun;
        }

        Config config = new Config();
        config.init();
        String target = config.get("SQL.ru");
        String pattern = config.get("filter.pattern");

        DBworker worker = new DBworker();

        ParserSQLru sm = new ParserSQLru();
        int pages = 0;
        try {
            pages = sm.countOfPages(target);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        int controlCheck;
        for (int i = 1; i <= pages; i++) {
            String subTarget = target + "/" + i;
            try {
                controlCheck = sm.parsPage(subTarget, pattern, timeFilter, prev);
                if (controlCheck == 0) {
                    break;
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        List<Vacancy> list = sm.getVacancyList();
        try {
            worker.init(config);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        worker.add(list);
        try {
            worker.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }

        count++;
        prev = LocalDateTime.now();
        data.put(COUNT, count);
        data.put(PREVDATE, prev);
    }
}
