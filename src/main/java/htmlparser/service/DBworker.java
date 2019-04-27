package htmlparser.service;

import htmlparser.model.Vacancy;
import htmlparser.util.Config;

import java.sql.*;
import java.util.List;

/**
 * Работа с базой данных.
 */
public class DBworker implements AutoCloseable {

    private Connection connection;

    public boolean init(Config config) throws ClassNotFoundException, SQLException {
        config.init();
        Class.forName(config.get("jdbc.driver"));
        this.connection = DriverManager.getConnection(
                config.get("jdbc.url"),
                config.get("jdbc.username"),
                config.get("jdbc.password")
        );
        this.tableExistCheck();
        return this.connection != null;
    }

    /**
     * Добавляем в базу вакансии из листа, который получим после парсинга подфорума сайта.
     * @param list
     */
    public void add(List<Vacancy> list) {
        try (
                PreparedStatement statement = this.connection.prepareStatement(
                        "INSERT INTO vacancies (vacancy_name, vacancy_text, vacancy_link, vacancy_date) " +
                                "VALUES (?, ?, ?, ?) ON CONFLICT (vacancy_name) DO NOTHING;")) {
            this.connection.setAutoCommit(false);
            for (Vacancy v : list) {
                statement.setString(1, v.getName());
                statement.setString(2, v.getText());
                statement.setString(3, v.getReference());
                statement.setTimestamp(4, Timestamp.valueOf(v.getTime()));
                statement.addBatch();
            }
            statement.executeBatch();
            this.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Проверяем наличие нужной для работы таблицы vacancy в базе данных.
     * Если её нет- создаём.
     *
     * @throws SQLException
     */
    private void tableExistCheck() throws SQLException {
        DatabaseMetaData metadata = this.connection.getMetaData();
        ResultSet resultSet = metadata.getTables(
                null, null, "vacancies", null);
        if (!resultSet.next()) {
            try (Statement statement = this.connection.createStatement()) {
                statement.execute("CREATE TABLE vacancies(id serial primary key, "
                        + "vacancy_name VARCHAR UNIQUE, vacancy_text VARCHAR, "
                        + "vacancy_link VARCHAR, vacancy_date TIMESTAMP );");
            }
        }
    }

    public void close() throws SQLException {
        if (this.connection != null) {
            this.connection.close();
        }
    }
}