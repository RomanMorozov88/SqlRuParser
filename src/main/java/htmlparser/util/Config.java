package htmlparser.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Получаем настройки из файла ParserSQLsettings/app.properties
 */
public class Config {
    private final Properties values = new Properties();

    public void init() {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(
                "ParserSQLsettings/app.properties")) {
            values.load(in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String get(String key) {
        return this.values.getProperty(key);
    }
}