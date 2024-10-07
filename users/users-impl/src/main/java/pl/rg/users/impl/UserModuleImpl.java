package pl.rg.users.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import pl.rg.users.User;
import pl.rg.users.UserModuleApi;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.exception.ValidationException;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.validator.api.ValidatorService;
import pl.rg.utils.validator.impl.BaseValidator;
import pl.rg.utils.validator.impl.ValidatorServiceImpl;

@Service
public class UserModuleImpl implements UserModuleApi {

  private ValidatorService validatorService = new ValidatorServiceImpl();

  private Logger logger = LoggerImpl.getInstance();

  private Map<String, String> constraints;

  @Override
  public User createUser(String userName, String firsName, String lastName, String email) {
    User user = new UserImpl(userName, generatePassword(), firsName, lastName, email);
    constraints = validatorService.validateFields(user);
    if (constraints.isEmpty()) {
      return user;
    } else {
      ValidationException exception = new ValidationException(
          "Błędne dane podczas tworzenia użytkownika: ", constraints);
      logger.logAndThrowRuntimeException(exception);
    }
    return user;
  }

  private String generatePassword() {
    int passwordLength = 8;
    String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
    String digits = "0123456789";
    String specialCharacters = BaseValidator.SPECIAL_CHARACTERS;
    String allCharacters = upperCaseLetters + lowerCaseLetters + digits + specialCharacters;
    List<String> password = new ArrayList<>();
    SecureRandom secureRandom = new SecureRandom();

    password.add(getRandomChar(upperCaseLetters, secureRandom));
    password.add(getRandomChar(lowerCaseLetters, secureRandom));
    password.add(getRandomChar(digits, secureRandom));
    password.add(getRandomChar(specialCharacters, secureRandom));

    for (int i = password.size(); i < passwordLength; i++) {
      password.add(
          getRandomChar(allCharacters, secureRandom));
    }
    Collections.shuffle(password);
    return String.join("", password);
  }

  private String getRandomChar(String availableChars, SecureRandom secureRandom) {
    return String.valueOf(availableChars.charAt(secureRandom.nextInt(availableChars.length())));
  }
}
