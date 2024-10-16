package pl.rg.utils.validator.impl;

import java.lang.reflect.Field;
import pl.rg.utils.annotation.Validate;

public class TextValidator extends BaseValidator {

  public final static String ADDRESS_MESSAGE = "Adres nie jest poprawny (max długość 20 znaków, dokładny adres budynku, kod pocztowy)";

  public final static String TEXT_REGEX = "^[\\p{L}\\s]+$";

  public final static String NUMBERS_REGEX = "^[0-9]+$";

  @Override
  public boolean valid(String value, Field field) {
    int maxTextLength = field.getAnnotation(Validate.class).maxTextLength();
    String fieldRegex = field.getAnnotation(Validate.class).format();
    fieldRegex = fieldRegex.equals(".*") ? TEXT_REGEX : fieldRegex;
    if (value.length() > maxTextLength || !value.matches(fieldRegex)) {
      return false;
    }
    return true;
  }
}