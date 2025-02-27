package pl.rg.utils.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

public class PropertiesUtils {

  public static final String PRIVATE_KEY = "privateKey.directory";

  public static final String LOG_TYPE = "log.type";

  public static final String LOG_DIRECTORY = "log.directory";

  public static final String LOG_LEVEL = "log.level";

  public static final String LOG_SQL = "log.sql";

  public static final String USER_MAX_LOGIN_ATTEMPTS = "user.maxLoginAttempts";

  public static final String USER_BLOCKED_HOURS = "user.blockedHours";

  public static final String EMAIL_SERVER = "email.server";

  public static final String EMAIL_USERNAME = "email.username";

  public static final String EMAIL_PASSWORD = "email.password";

  public static final String EMAIL_PORT = "email.port";

  public static final String EMAIL_AUTH = "email.authentication";

  public static final String EMAIL_STARTTLS = "email.starttls";

  public static final String SESSION_MAXTIME = "session.maxTime";

  private static final String PROPERTIES_FILE = "app.properties";

  private static Logger logger = LoggerImpl.getInstance();

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

  public static int getIntProperty(String key) {
    String property = getProperty(key);
    try {
      return Integer.parseInt(property);
    } catch (NumberFormatException e) {
      throw logger.logAndThrowRuntimeException(LogLevel.DEBUG, new ApplicationException("X343D",
          "Błąd pobrania parametru " + property + ". Brak parametru lub błędny format."));
    }
  }
}
