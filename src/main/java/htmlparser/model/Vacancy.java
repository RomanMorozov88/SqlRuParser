package htmlparser.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сюда закидываем информацию с сайта или из базы.
 * <p>
 * name - имя вакансии.
 * text - описание вакансии.
 * data - время создания вакансии.
 * reference - ссылка на вакансию.
 */
public class Vacancy {

    private String name;
    private String text;
    private LocalDateTime time;
    private String reference;

    public Vacancy(String name, String text, LocalDateTime time, String reference) {
        this.name = name;
        this.text = text;
        this.time = time;
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime data) {
        this.time = data;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Переопредялем equals и hashCode таким образом,
     * что бы при сравнении учитывалось только имя вакансии.
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vacancy vacancy = (Vacancy) o;
        return Objects.equals(name, vacancy.name);
    }
    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}