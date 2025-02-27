package pl.rg.utils.validator.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.annotation.Validate;
import pl.rg.utils.exception.ValidationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.validator.api.ValidatorService;
import pl.rg.utils.validator.enums.PossibleNull;
import pl.rg.utils.validator.enums.ValidatorCase;

@Service
public class ValidatorServiceImpl implements ValidatorService {

  public static final Map<ValidatorCase, String> classNameResolver = new HashMap<>();

  static {
    classNameResolver.put(ValidatorCase.TEXT, "pl.rg.utils.validator.impl.TextValidator");
    classNameResolver.put(ValidatorCase.IDENTIFICATION_NUMBER,
        "pl.rg.utils.validator.impl.IdentificationNumberValidator");
    classNameResolver.put(ValidatorCase.DOCUMENT_NUMBER,
        "pl.rg.utils.validator.impl.DocumentNumberValidator");
    classNameResolver.put(ValidatorCase.PASSWORD,
        "pl.rg.security.validator.PasswordValidator");
  }

  private Logger logger = LoggerImpl.getInstance();

  public Map<String, String> validateFields(Object object) {
    Map<String, String> constraints = new HashMap<>();
    Class<?> objectClass = object.getClass();
    Field[] classFields = objectClass.getDeclaredFields();
    Field[] superclassFields = objectClass.getSuperclass().getDeclaredFields();
    Field[] allFields = Stream
        .concat(Arrays.stream(classFields), Arrays.stream(superclassFields))
        .toArray(Field[]::new);
    Arrays.stream(allFields)
        .filter(field -> field.isAnnotationPresent(Validate.class))
        .filter(field -> {
          Validate annotation = field.getAnnotation(Validate.class);
          return annotation.possibleNull().equals(PossibleNull.NO);
        })
        .forEach(field -> {
          field.setAccessible(true);
          try {
            boolean isValid = isValid(object, field);
            if (!isValid) {
              constraints.put(field.getName(), field.getAnnotation(Validate.class).message());
            }

          } catch (IllegalAccessException | ClassNotFoundException | InvocationTargetException |
                   InstantiationException | NoSuchMethodException e) {
            ValidationException exception = new ValidationException(
                "Błąd podczas walidacji: " + e.getMessage(), e);
            logger.logAnException(LogLevel.ERROR, exception, exception.getMessage());
            throw exception;
          }
        });
    return constraints;
  }

  @Override
  public Map<String, String> validateWithInnerFields(Object object) {
    Map<String, String> constraints = validateFields(object);
    Field[] declaredFields = object.getClass().getDeclaredFields();
    for (Field field : declaredFields) {
      if (field.getType().getName().startsWith("pl.rg") && !field.getType().isEnum()) {
        field.setAccessible(true);
        try {
          constraints.putAll(validateWithInnerFields(field.get(object)));
        } catch (IllegalAccessException e) {
          ValidationException exception = new ValidationException(
              "Błąd podczas walidacji: " + e.getMessage(), e);
          logger.logAnException(LogLevel.DEBUG, exception, exception.getMessage());
          throw exception;
        }
      }
    }
    return constraints;
  }

  private boolean isValid(Object object, Field field)
      throws IllegalAccessException, ClassNotFoundException,
      InstantiationException, InvocationTargetException, NoSuchMethodException {
    if (field.getType().isArray()) {
      Object array = field.get(object);
      for (int i = 0; i < Array.getLength(array); i++) {
        Object arrayElement = Array.get(array, i);
        if (!isValidElement(arrayElement, field)) {
          return false;
        }
      }
      return true;
    }
    Object fieldValue = field.get(object);
    return isValidElement(fieldValue, field);
  }

  private boolean isValidElement(Object fieldValue, Field field)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    ValidatorCase validatorCase = field.getAnnotation(Validate.class).validatorCase();
    Class<?> cls = Class.forName(classNameResolver.get(validatorCase));
    Constructor<?> clsConstructor = cls.getConstructors()[0];
    Object clsObject = clsConstructor.newInstance();
    Method validMethod = clsObject.getClass().getMethod("valid", String.class, Field.class);
    return (boolean) validMethod.invoke(clsObject, fieldValue.toString(), field);
  }
}
