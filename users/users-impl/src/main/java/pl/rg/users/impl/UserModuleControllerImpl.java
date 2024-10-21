package pl.rg.users.impl;

import java.util.Map;
import pl.rg.users.User;
import pl.rg.users.UserDto;
import pl.rg.users.UserModuleController;
import pl.rg.users.UserModuleApi;
import pl.rg.users.mapper.UserMapper;
import pl.rg.users.window.UserForm;
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

  UserMapper userMapper = UserMapper.INSTANCE;

  @Override
  public boolean createUser(String firsName, String lastName, String email) {
    UserDto userDto = new UserDto(firsName, lastName, email);
    Map<String, String> constraints = validatorService.validateFields(userDto);
    if (constraints.isEmpty()) {
      User user = userMapper.dtoToDomain(userDto);
      userModuleApi.addUser(user);
      return true;
    } else {
      ValidationException exception = new ValidationException(
          "Błędne dane podczas tworzenia użytkownika: ", constraints);
      logger.logAndThrowRuntimeException(exception);
    }
    return false;
  }

  @Override
  public void showUserForm(){
    UserForm userForm = new UserForm(this);
    userForm.setVisible(true);
  }
}


