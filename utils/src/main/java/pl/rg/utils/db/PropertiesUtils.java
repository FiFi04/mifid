package pl.rg.utils.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

  public final static String PRIVATE_KEY = "privateKey.directory";

  private final static String PROPERTIES_FILE = "app.properties";

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
