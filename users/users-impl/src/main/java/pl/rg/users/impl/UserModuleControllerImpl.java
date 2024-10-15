package pl.rg.users.impl;

import java.util.Map;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.UserModuleController;
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
  private ValidatorService validatorService;

  private Logger logger = LoggerImpl.getInstance();

  @Override
  public UserDtoImpl createUser(String firsName, String lastName, String email) {
    UserDtoImpl user = new UserDtoImpl(firsName, lastName, email);
    Map<String, String> constraints = validatorService.validateFields(user);
    if (constraints.isEmpty()) {
      userModuleApi.addUser(user);
      return user;
    } else {
      ValidationException exception = new ValidationException(
          "Błędne dane podczas tworzenia użytkownika: ", constraints);
      logger.logAndThrowRuntimeException(exception);
    }
    return user;
  }
}


