package pl.rg.utils.logger;

import pl.rg.utils.exception.RepositoryException;

public interface Logger {

  void log(LogLevel logLevel, String message, Object... additionalInfo);

  void log(LogLevel logLevel, String message, boolean isSqlLog);

  void logAnException(LogLevel logLevel, Throwable exception, String message, Object... additionalArguments);

  void logAnException(LogLevel logLevel, Throwable exception, String message, boolean isSqlLog);

  <T extends RepositoryException> T logAndThrowRepositoryException(LogLevel logLevel, T exception);

  <T extends RepositoryException> T logAndThrowRepositoryException(LogLevel logLevel, T exception, boolean isSqlLog);

  <T extends RuntimeException> T logAndThrowRuntimeException(LogLevel logLevel, T exception);

  <T extends RuntimeException> T logAndThrowRuntimeException(LogLevel logLevel, T exception, boolean isSqlLog);
}
