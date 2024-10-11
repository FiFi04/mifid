package pl.rg.users.impl;

import java.util.Map;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.UserModuleController;
import pl.rg.users.UserDto;
import pl.rg.users.UserModuleApi;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Controller;
import pl.rg.utils.exception.ValidationException;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.validator.api.ValidatorService;

@Controller
public class UserModuleControllerImpl implements UserModuleController {

  @Autowire
  private UserModuleApi userModuleApi;

  @Autowire
  private SecurityModuleApi securityModuleApi;

  @Autowire
  private ValidatorService validatorService;

  private Logger logger = LoggerImpl.getInstance();

  @Override
  public UserDto createUser(String firsName, String lastName, String email) {
    UserDto user = new UserDtoImpl(userModuleApi.generateUsername(firsName, lastName),
        securityModuleApi.generatePassword(), firsName, lastName, email);
    Map<String, String> constraints = validatorService.validateFields(user);
    if (constraints.isEmpty()) {
      return user;
    } else {
      ValidationException exception = new ValidationException(
          "Błędne dane podczas tworzenia użytkownika: ", constraints);
      logger.logAndThrowRuntimeException(exception);
    }
    return user;
  }

  @Override
  public void addUser(UserDto userDto) {
    userModuleApi.addUser(userDto);
  }
}


