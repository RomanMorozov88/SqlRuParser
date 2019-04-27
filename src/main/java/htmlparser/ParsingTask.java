package htmlparser;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;

/**
 * Задача для нитей- здесь происходит парсинг единственной страницы и
 * помещение результатов в Базу Данных.
 */
public class ParsingTask implements Callable {

    private static final Logger logger = Logger.getLogger(ThreadPool.class);
    private final ParserSQLru sm;
    private final Config config;
    private final BiPredicate<LocalDateTime, LocalDateTime> timeFilter;
    private final LocalDateTime prev;
    private final int page;

    public ParsingTask(ParserSQLru sm, Config config,
                       BiPredicate<LocalDateTime, LocalDateTime> timeFilter,
                       LocalDateTime prev, int page) {
        this.sm = sm;
        this.config = config;
        this.timeFilter = timeFilter;
        this.prev = prev;
        this.page = page;
    }

    @Override
    public List<Vacancy> call() {
        List<Vacancy> result = new ArrayList<>();
        config.init();
        String target = config.get("SQL.ru");
        String pattern = config.get("filter.pattern");
        logger.info(">>>Read the page №" + page + " by Thread "
                + Thread.currentThread().getName());
        String subTarget = target + "/" + page;
        try {
            result = sm.parsPage(subTarget, pattern, timeFilter, prev);
            if (result == null) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            System.out.println(e.getMessage());
        }
        return result;
    }
}