package pl.rg.logger;

import pl.rg.db.DBConnector;
import pl.rg.exceptions.ValidationException;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;

public class LoggerImpl implements Logger{

    private static LoggerImpl loggerImpl;

    private String logDirectory;

    private LogType logType;

    private BufferedWriter writer;

    private File logFile;

    private final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private LoggerImpl() {
        initializeLogger();
    }

    public static LoggerImpl getInstance() {
        if (loggerImpl == null) {
            loggerImpl = new LoggerImpl();
        }
        return loggerImpl;
    }

    public void log(String message, Object... additionalInfo) {
        StringBuilder logMessage = createLogMessage(message, additionalInfo);
        logMessage(logMessage);
    }

    public void logAnException(Throwable exception, String message, Object... additionalInfo) {
        StringBuilder logMessage = createExceptionLogMessage(message, exception, additionalInfo);
        logMessage(logMessage);
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
            Properties properties = new Properties();
            properties.load(LoggerImpl.class.getClassLoader().getResourceAsStream("logger.properties"));
            logType = LogType.valueOf(properties.getProperty("log.type").toUpperCase());
            logDirectory = properties.getProperty("log.directory");
            initializeLogFile();
            writer = new BufferedWriter(new FileWriter(logFile, true));
        } catch (IOException e) {
            logAnException(e, "Błąd inicjalizacji loggera");
        }
    }

    private void saveLogToFile(StringBuilder log) {
        try {
            writer.write(log.toString());
            writer.flush();
        } catch (IOException e) {
            String exceptionMessage = "Błąd zapisu do pliku";
            System.err.println(exceptionMessage);
            logAnException(e, exceptionMessage);
        }
    }

    private void initializeLogFile() {
        String currentDate = LocalDate.now().toString();
        String logFilePath = logDirectory + "/" + currentDate + ".log";
        logFile = new File(logFilePath);
    }

    private StringBuilder createLogMessage(String message, Object... additionalInfo) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        String formattedMessage = formatMessageWithArguments(message, additionalInfo);
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(currentTime);
        logMessage.append(" " + getClassName());
        logMessage.append(" - ");
        logMessage.append(formattedMessage);
        logMessage.append("\n");
        return logMessage;
    }

    private StringBuilder createExceptionLogMessage(String message, Throwable exception, Object... additionalInfo) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        String formattedMessage = formatMessageWithArguments(message, additionalInfo);
        StringBuilder logMessage = new StringBuilder();
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
