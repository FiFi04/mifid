package pl.rg.utils.logger;

import pl.rg.utils.exception.RepositoryException;

public interface Logger {

  void log(String message, Object... additionalInfo);

  void logAnException(Throwable exception, String message, Object... additionalArguments);

  <T extends RepositoryException> T logAndThrowRepositoryException(T exception);

  <T extends RuntimeException> T logAndThrowRuntimeException(T exception);
}
