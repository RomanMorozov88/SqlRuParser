package htmlparser;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class ParserSQLruTest {

    @Test
    public void countOfPages() {
    }

    @Test
    public void whenPredicateForFirstRunIsTrue() {
        LocalDateTime inputOne = LocalDateTime.of(2019, 04, 10, 10, 10);
        LocalDateTime inputTwo = LocalDateTime.of(2019, 03, 01, 01, 01);
        assertThat(ParserSQLru.predicateForFirstRun(inputOne, inputTwo), is(true));
    }

    @Test
    public void whenPredicateForFirstRunIsFalse() {
        LocalDateTime inputOne = LocalDateTime.of(2018, 04, 10, 10, 10);
        LocalDateTime inputTwo = LocalDateTime.of(2019, 03, 01, 01, 01);
        assertThat(ParserSQLru.predicateForFirstRun(inputOne, inputTwo), is(false));
    }

    @Test
    public void whenPredicateForAnotherRunIsTrue() {
        LocalDateTime inputOne = LocalDateTime.of(2019, 04, 10, 10, 10);
        LocalDateTime inputTwo = LocalDateTime.of(2019, 03, 01, 01, 01);
        assertThat(ParserSQLru.predicateForAnotherRun(inputOne, inputTwo), is(true));
    }

    @Test
    public void whenPredicateForAnotherRunIsFalse() {
        LocalDateTime inputOne = LocalDateTime.of(2018, 04, 10, 10, 10);
        LocalDateTime inputTwo = LocalDateTime.of(2019, 03, 01, 01, 01);
        assertThat(ParserSQLru.predicateForFirstRun(inputOne, inputTwo), is(false));
    }
}