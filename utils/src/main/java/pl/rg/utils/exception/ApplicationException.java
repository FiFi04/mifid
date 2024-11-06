package pl.rg.utils.exception;

public class ApplicationException extends RuntimeException{

  public ApplicationException() {
  }

  public ApplicationException(String code, String message) {
    super(code + ": " + message);
  }
}
