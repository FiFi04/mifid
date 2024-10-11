package pl.rg.security.validator;

import java.lang.reflect.Field;
import pl.rg.utils.annotation.Validate;
import pl.rg.utils.validator.impl.BaseValidator;

public class PasswordValidator extends BaseValidator {

  private final String passwordRegex =
      "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[" + SPECIAL_CHARACTERS + "])[A-Za-z\\d"
          + SPECIAL_CHARACTERS + "]{8,20}$";

  @Override
  public boolean valid(String value, Field field) {
    int maxTextLength = field.getAnnotation(Validate.class).maxTextLength();
    if (value.length() > maxTextLength || !value.matches(passwordRegex)) {
      return false;
    }
    return true;
  }
}
