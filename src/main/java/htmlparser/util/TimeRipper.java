package htmlparser.util;

import htmlparser.service.ParserSQLru;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс, отвечающий за конвертацию строки с датой в LocalDateTime объект.
 * Т.к. имена месяцев в существующей локали не соответствуют именам,
 * которые мы вытаскиваем с сайта.
 */
public class TimeRipper {

    private static final Logger logger = Logger.getLogger(ParserSQLru.class);

    private static final Map<String, String> mapMonths = mapMonthsInit();

    private static Map<String, String> mapMonthsInit() {
        Map<String, String> map = new HashMap<>();
        map.put("янв", "1");
        map.put("фев", "2");
        map.put("мар", "3");
        map.put("апр", "4");
        map.put("май", "5");
        map.put("июн", "6");
        map.put("июл", "7");
        map.put("авг", "8");
        map.put("сен", "9");
        map.put("окт", "10");
        map.put("ноя", "11");
        map.put("дек", "12");
        return map;
    }

    /**
     * Получаем LocalDateTime.
     *
     * @param inputTime
     * @return
     */
    public LocalDateTime getTime(String inputTime) {
        inputTime = inputTime.trim();
        LocalDateTime result = null;
        try {
            String buffer = this.replaceMonth(inputTime);
            if (buffer == null) {
                result = this.todayOrYesterdayConvert(inputTime);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d L yy, HH:mm", Locale.getDefault());
                result = LocalDateTime.parse(buffer, formatter);
            }
        } catch (NullPointerException e) {
            logger.error(e.getMessage() + " : " + inputTime, e);
        }
        return result;
    }

    /**
     * С помощью regex заменяем имя месяца на соответствующее
     * ему числовое значение.
     *
     * @param inputString
     * @return
     */
    private String replaceMonth(String inputString) {
        Pattern pattern = Pattern.compile("[а-я]{3}");
        Matcher matcher = pattern.matcher(inputString);
        String result = null;
        String buffer = null;
        while (matcher.find()) {
            buffer = matcher.group();
        }
        if (this.mapMonths.containsKey(buffer)) {
            result = inputString.replaceFirst("[а-я]{3}", this.mapMonths.get(buffer));
        }
        return result;
    }

    /**
     * На случай, когда встречаем сегодняшний\вчерашний пост.
     *
     * @param input
     * @return
     */
    private LocalDateTime todayOrYesterdayConvert(String input) {
        String buffer;
        LocalDate ld = null;
        if (input.contains("сегодня")) {
            ld = LocalDate.now();
        } else if (input.contains("вчера")) {
            ld = LocalDate.now().minus(Period.ofDays(1));
        }
        buffer = input.substring(input.indexOf(",") + 1).replaceAll(" ", "");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
        LocalTime lt = LocalTime.parse(buffer, formatter);
        return LocalDateTime.of(ld, lt);
    }
}
