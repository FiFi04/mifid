package pl.rg.logger;

public interface Logger {

    void log(String message, Object... additionalInfo);

    void logAnException(Throwable exception, String message, Object... additionalArguments);
}
