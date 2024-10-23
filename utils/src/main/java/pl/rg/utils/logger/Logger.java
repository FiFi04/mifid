package pl.rg.utils.logger;

public interface Logger {

  void log(String message, Object... additionalInfo);

  void logAnException(Throwable exception, String message, Object... additionalArguments);

  void logAndThrowRepositoryException(String message, Throwable exception);

  <T extends RuntimeException> T logAndThrowRuntimeException(T exception);
}
