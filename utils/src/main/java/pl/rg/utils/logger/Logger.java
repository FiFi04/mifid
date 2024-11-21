package pl.rg.utils.logger;

import pl.rg.utils.exception.RepositoryException;

public interface Logger {

  void log(LogLevel logLevel, String message, Object... additionalInfo);

  void logSql(LogLevel logLevel, String message);

  void logAnException(LogLevel logLevel, Throwable exception, String message, Object... additionalArguments);

  <T extends RepositoryException> T logAndThrowRepositoryException(LogLevel logLevel, T exception);

  <T extends RuntimeException> T logAndThrowRuntimeException(LogLevel logLevel, T exception);
}
