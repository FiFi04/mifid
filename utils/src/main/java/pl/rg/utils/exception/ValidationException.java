package pl.rg.utils.exception;

import java.util.Map;

public class ValidationException extends RuntimeException {

  private Map<String, String> constraintsMap;

  public ValidationException() {
  }

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ValidationException(String message, Map<String, String> constraintsMap) {
    super(message + constraintsMap);
    this.constraintsMap = constraintsMap;
  }

  public Map<String, String> getConstraintsMap() {
    return constraintsMap;
  }
}
