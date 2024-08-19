package pl.rg.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

    public static final String PROPERTIES_FILE_UTILS = "app.properties";

    public final static String PROPERTIES_FILE_SECURITY = "security.properties";

    public static String getProperty(String key, String propertiesFileName) {
        Properties properties = new Properties();
        try (InputStream inputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties.getProperty(key);
    }
}
