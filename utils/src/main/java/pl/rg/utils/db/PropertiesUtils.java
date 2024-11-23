package pl.rg.utils.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

  public static final String PRIVATE_KEY = "privateKey.directory";

  public static final String LOG_TYPE = "log.type";

  public static final String LOG_DIRECTORY = "log.directory";

  public static final String LOG_LEVEL = "log.level";

  public static final String LOG_SQL = "log.sql";

  private static final String PROPERTIES_FILE = "app.properties";

  public static String getProperty(String key) {
    Properties properties = new Properties();
    try (InputStream inputStream = PropertiesUtils.class.getClassLoader()
        .getResourceAsStream(PROPERTIES_FILE)) {
      properties.load(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return properties.getProperty(key);
  }
}
