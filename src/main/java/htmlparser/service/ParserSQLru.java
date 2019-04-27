package htmlparser.service;

import htmlparser.model.Vacancy;
import htmlparser.util.TimeRipper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Парсер раздела вакансий с сайта SQL.ru
 */
public class ParserSQLru {

    private TimeRipper ripper = new TimeRipper();
    private List<Vacancy> vacancyList = new ArrayList<>();

    public List<Vacancy> getVacancyList() {
        return vacancyList;
    }

    /**
     * Основной метод, в котором заполняется список вакансий.
     * 1) При помощи primeFilter получаем все посты с текущей страницы target.
     * 2) Получаем нужные данные из ВСЕХ вакансий(постов) с текущей страницы.
     * 3) Отфильтровываем вакансии по времени их создания(НЕ по времени последнего комментария),
     * итерируем controlCount
     * и закидываем в лист vacancyList нужные нам заявки(определяем по выражению pattern).
     * <p>
     * *controlCount необходима для того, что бы проверить- есть ли на текущей странице
     * вакансии, удовлетворяющие условию предикейта.
     *
     * @param target  страница, которую читаем.
     * @param pattern шаблон, по которому отбираем нужные вакансии.
     * @return количество постов с подходящей датой создания.
     * Если будет возвращён 0 -то дальше страницы на данном форуме не пролистываем,
     * т.к. считаем, что все последующие посты старше того, что нам нужно.
     * @throws IOException
     */
    public List<Vacancy> parsPage(String target, String pattern,
                                  BiPredicate<LocalDateTime, LocalDateTime> predicate,
                                  LocalDateTime prevTime) throws IOException {
        List<Vacancy> result = new ArrayList<>();
        int controlCount = 0;

        List<Element> jobList = this.primeFilter(target);
        boolean grabText;

        for (Element run : jobList) {
            String name = run.getElementsByAttributeValue("class", "postslisttopic")
                    .select("a").first().text();
            String reference = run.select("a").attr("href");
            grabText = name.matches(pattern);
            Map<String, String> mapTextTime = this.textFromVacancy(reference, grabText);
            String text = mapTextTime.get("text");
            String timeString = mapTextTime.get("time");
            LocalDateTime time = this.ripper.getTime(timeString);
            if (predicate.test(time, prevTime)) {
                controlCount++;
                if (grabText) {
                    result.add(new Vacancy(name, text, time, reference));
                }
            }
        }
        if (controlCount == 0) {
            result = null;
        }
        return result;
    }

    /**
     * Этим методом отсекам элементы, содержащие названия столбцов etc.
     *
     * @param targetURL
     * @return
     * @throws IOException
     */
    private List<Element> primeFilter(String targetURL) throws IOException {
        Document document = Jsoup.connect(targetURL).get();
        Elements jobList = document.getElementsByAttributeValue("class", "forumTable").select("tr");

        List<Element> result = jobList
                .stream()
                .filter(x ->
                        x.getElementsByAttributeValue("class", "postslisttopic").size() > 0
                )
                .collect(Collectors.toList());
        return result;
    }

    /**
     * Метод для извлечения текста описания вакансии и времени её создания.
     * Возвращает мапу с ключами "text" и "time" соответственно.
     * Возможна ситуация, когда строка времени у поста будет пустой-
     * в этом случае записываем в возвращаемую map`у "1 янв 00, 00:00".
     * <p>
     * Если на входе получаем grabText == false то текст вакансии не вытаскиваем.
     *
     * @param reference ссылка на вакансию.
     * @return
     * @throws IOException
     */
    private Map<String, String> textFromVacancy(String reference, boolean grabText) throws IOException {
        Map<String, String> result = new HashMap<>();

        Document doc = Jsoup.connect(reference).get();
        Elements msgList = doc.getElementsByAttributeValue("class", "msgTable");
        msgList = msgList.first().select("tr");

        String mainText = "";
        if (grabText) {
            mainText = msgList.get(1).getElementsByAttributeValue("class", "msgBody").get(1).html();
            mainText = mainText.replaceAll("<br>", System.lineSeparator());
        }

        String timeText = msgList.get(2).getElementsByAttributeValue("class", "msgFooter").text();
        if (timeText.equals("")) {
            timeText = "1 янв 00, 00:00";
        } else {
            timeText = timeText.replaceFirst("\\[.+", "").trim();
        }
        result.put("text", mainText);
        result.put("time", timeText);
        return result;
    }

    /**
     * Определяем общее количество страниц в нужной теме.
     *
     * @param targetURL
     * @return
     * @throws IOException
     */
    public int countOfPages(String targetURL) throws IOException {
        Document document = Jsoup.connect(targetURL).get();
        String buffer = document.getElementsByAttributeValue("class", "sort_options").select("a").text();
        int cut = buffer.lastIndexOf(" ");
        String borderStr = buffer.substring(cut + 1);
        Integer border = Integer.parseInt(borderStr);
        return border;
    }

    /**
     * Если запускаем в первый раз - то берём вакансии с начала текущего года.
     *
     * @param time
     * @return
     */
    public static boolean predicateForFirstRun(LocalDateTime time, LocalDateTime prev) {
        boolean result = true;
        int currentYear = prev.getYear();
        int inputYear = time.getYear();
        if (currentYear - inputYear != 0) {
            result = false;
        }
        return result;
    }

    /**
     * Проверяем вакансии, появившиеся уже после последнего сеанса парсинга.
     *
     * @param time
     * @param prev
     * @return
     */
    public static boolean predicateForAnotherRun(LocalDateTime time, LocalDateTime prev) {
        return prev.compareTo(time) < 0;
    }
}