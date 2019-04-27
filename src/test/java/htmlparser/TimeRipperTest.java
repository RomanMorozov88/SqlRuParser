package htmlparser;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TimeRipperTest {

    @Test
    public void whenJustGetTime() {
        TimeRipper ripper = new TimeRipper();
        String input = "10 апр 19, 10:45";
        LocalDateTime expection = LocalDateTime.of(2019, 04, 10, 10, 45);
        LocalDateTime result = ripper.getTime(input);
        assertThat(result, is(expection));
    }

    @Test
    public void whenGetTimeFromToday() {
        TimeRipper ripper = new TimeRipper();
        String input = "сегодня, 10:45";
        LocalTime buffer = LocalTime.of(10, 45);
        LocalDateTime expection = LocalDateTime.of(LocalDate.now(), buffer);
        LocalDateTime result = ripper.getTime(input);
        assertThat(result, is(expection));
    }

    @Test
    public void whenGetTimeFromYesterday() {
        TimeRipper ripper = new TimeRipper();
        String input = "вчера, 10:45";
        LocalTime buffer = LocalTime.of(10, 45);
        LocalDateTime expection = LocalDateTime.of(LocalDate.now().minus(Period.ofDays(1)), buffer);
        LocalDateTime result = ripper.getTime(input);
        assertThat(result, is(expection));
    }
}