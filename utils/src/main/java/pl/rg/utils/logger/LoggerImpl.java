package pl.rg.utils.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import pl.rg.utils.db.DBConnector;
import pl.rg.utils.db.PropertiesUtils;
import pl.rg.utils.exception.RepositoryException;
import pl.rg.utils.exception.ValidationException;

public class LoggerImpl implements Logger {

  private static LoggerImpl loggerImpl;

  private String logDirectory;

  private LogType logType;

  private BufferedWriter writer;

  private File logFile;

  private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

  private LogLevel logLevel = LogLevel.valueOf(PropertiesUtils.getProperty("log.level"));

  private boolean logSql = Boolean.parseBoolean(PropertiesUtils.getProperty("log.sql"));

  private LoggerImpl() {
    initializeLogger();
  }

  public static LoggerImpl getInstance() {
    if (loggerImpl == null) {
      loggerImpl = new LoggerImpl();
    }
    return loggerImpl;
  }

  @Override
  public void log(LogLevel logLevel, String message, Object... additionalInfo) {
    if (logLevel.ordinal() >= this.logLevel.ordinal()) {
      StringBuilder logMessage = createLogMessage(logLevel, message, additionalInfo);
      logMessage(logMessage);
    }
  }

  @Override
  public void log(LogLevel logLevel, String message, boolean isSqlLog) {
    if (logSql && isSqlLog && logLevel.ordinal() >= this.logLevel.ordinal()) {
      StringBuilder logMessage = createLogMessage(logLevel, message);
      logMessage(logMessage);
    }
  }

  @Override
  public void logAnException(LogLevel logLevel, Throwable exception, String message,
      Object... additionalInfo) {
    if (logLevel.ordinal() >= this.logLevel.ordinal()) {
      StringBuilder logMessage = createExceptionLogMessage(logLevel, message, exception,
          additionalInfo);
      logMessage(logMessage);
    }
  }

  @Override
  public void logAnException(LogLevel logLevel, Throwable exception, String message,
      boolean isSqlLog) {
    if (logSql && isSqlLog && logLevel.ordinal() >= this.logLevel.ordinal()) {
      StringBuilder logMessage = createExceptionLogMessage(logLevel, message, exception);
      logMessage(logMessage);
    }
  }

  @Override
  public <T extends RepositoryException> T logAndThrowRepositoryException(LogLevel logLevel,
      T exception) {
    if (logLevel.ordinal() >= this.logLevel.ordinal()) {
      logAnException(logLevel, exception, exception.getMessage());
    }
    return exception;
  }

  @Override
  public <T extends RepositoryException> T logAndThrowRepositoryException(LogLevel logLevel,
      T exception, boolean isSqlLog) {
    if (logSql && isSqlLog && logLevel.ordinal() >= this.logLevel.ordinal()) {
      logAnException(logLevel, exception, exception.getMessage());
    }
    return exception;
  }

  @Override
  public <T extends RuntimeException> T logAndThrowRuntimeException(LogLevel logLevel,
      T exception) {
    if (logLevel.ordinal() >= this.logLevel.ordinal()) {
      logAnException(logLevel, exception, exception.getMessage());
    }
    return exception;
  }

  @Override
  public <T extends RuntimeException> T logAndThrowRuntimeException(LogLevel logLevel, T exception,
      boolean isSqlLog) {
    if (logSql && isSqlLog && logLevel.ordinal() >= this.logLevel.ordinal()) {
      logAnException(logLevel, exception, exception.getMessage());
    }
    return exception;
  }

  private void logMessage(StringBuilder logMessage) {
    try {
      if (logType.equals(LogType.CONSOLE) || DBConnector.getInstance().getConnection().isClosed()) {
        System.out.print(logMessage);
      } else if (logType.equals(LogType.FILE)) {
        saveLogToFile(logMessage);
      }
    } catch (SQLException e) {
      throw new ValidationException(e.getMessage(), e);
    }
  }

  private void initializeLogger() {
    try {
      logType = LogType.valueOf(PropertiesUtils.getProperty("log.type").toUpperCase());
      logDirectory = PropertiesUtils.getProperty("log.directory");
      initializeLogFile();
      writer = new BufferedWriter(new FileWriter(logFile, true));
    } catch (IOException e) {
      logAnException(LogLevel.ERROR, e, "Błąd inicjalizacji loggera");
    }
  }

  private void saveLogToFile(StringBuilder log) {
    try {
      writer.write(log.toString());
      writer.flush();
    } catch (IOException e) {
      String exceptionMessage = "Błąd zapisu do pliku";
      System.err.println(exceptionMessage);
      logAnException(LogLevel.ERROR, e, exceptionMessage);
    }
  }

  private void initializeLogFile() {
    Path projectRoot = Paths.get(System.getProperty("user.dir"));
    while (!Files.exists(projectRoot.resolve("loggerFiles"))) {
      projectRoot = projectRoot.getParent();
    }
    String currentDate = LocalDate.now().toString();
    String logFilePath = projectRoot + File.separator + logDirectory + currentDate + ".log";
    logFile = new File(logFilePath);
    if (!logFile.getParentFile().exists()) {
      logFile.getParentFile().mkdirs();
    }
  }

  private StringBuilder createLogMessage(LogLevel logLevel, String message,
      Object... additionalInfo) {
    String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    String formattedMessage = formatMessageWithArguments(message, additionalInfo);
    StringBuilder logMessage = new StringBuilder();
    logMessage.append("[" + logLevel + "]" + " ");
    logMessage.append(currentTime);
    logMessage.append(" " + getClassName());
    logMessage.append(" - ");
    logMessage.append(formattedMessage);
    logMessage.append("\n");
    return logMessage;
  }

  private StringBuilder createExceptionLogMessage(LogLevel logLevel, String message,
      Throwable exception,
      Object... additionalInfo) {
    String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    String formattedMessage = formatMessageWithArguments(message, additionalInfo);
    StringBuilder logMessage = new StringBuilder();
    logMessage.append("[" + logLevel + "]" + " ");
    logMessage.append(currentTime);
    logMessage.append(" " + getClassName());
    logMessage.append(" - exception message: ");
    logMessage.append(formattedMessage);
    logMessage.append(": \n");
    logMessage.append(getExceptionStackTrace(exception));
    return logMessage;
  }

  private String getClassName() {
    StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    for (StackTraceElement element : stackTraceElements) {
      if (!element.getClassName().equals(LoggerImpl.class.getName()) &&
          !element.getClassName().equals(Thread.class.getName())) {
        return element.getClassName();
      }
    }
    return "Nieznana klasa";
  }

  private String formatMessageWithArguments(String message, Object... additionalArguments) {
    for (Object arg : additionalArguments) {
      if (arg instanceof Map<?, ?> && !message.contains(arg.toString())) {
        message += arg.toString();
      } else {
        message = message.replaceFirst("\\{\\}", arg.toString());
      }
    }
    return message;
  }

  private String getExceptionStackTrace(Throwable exception) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    exception.printStackTrace(printWriter);
    return stringWriter.toString();
  }
}