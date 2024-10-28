package pl.rg.users.impl;

import java.util.Map;
import java.util.Optional;
import pl.rg.users.Session;
import pl.rg.users.User;
import pl.rg.users.UserDto;
import pl.rg.users.UserModuleApi;
import pl.rg.users.UserModuleController;
import pl.rg.users.mapper.UserMapper;
import pl.rg.users.session.SessionImpl;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Controller;
import pl.rg.utils.exception.ApplicationException;
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

  private UserMapper userMapper = UserMapper.INSTANCE;

  private Session session = SessionImpl.getInstance();

  private Logger logger = LoggerImpl.getInstance();

  @Override
  public boolean createUser(String firsName, String lastName, String email) {
    session.updateSession();
    UserDto userDto = new UserDto(firsName, lastName, email);
    Map<String, String> constraints = validatorService.validateFields(userDto);
    if (constraints.isEmpty()) {
      User user = userMapper.dtoToDomain(userDto);
      userModuleApi.addUser(user);
      return true;
    } else {
      throw logger.logAndThrowRuntimeException(new ValidationException(
          "Błędne dane podczas tworzenia użytkownika: ", constraints));
    }
  }

  @Override
  public Optional<UserDto> getUser(Integer userID) {
    session.updateSession();
    Optional<User> userDomain = userModuleApi.find(userID);
    return userDomain.map(user -> userMapper.domainToDto(user));
  }

  @Override
  public void updateUser(UserDto userDto) {
    session.updateSession();
    User user = userMapper.dtoToDomain(userDto);
    userModuleApi.update(user);
  }

  @Override
  public void deleteUser(Integer userId) {
    session.updateSession();
    userModuleApi.delete(userId);
  }

  @Override
  public boolean logIn(String username, String password) {
    boolean validLogInData = userModuleApi.validateLogInData(username, password);
    if (validLogInData) {
      session.startSession(username);
      return true;
    } else {
      throw logger.logAndThrowRuntimeException(
          new ApplicationException("U34LV", "Błędne dane podczas logowania"));
    }
  }

  @Override
  public void logOut() {
    session.endSession();
  }
}


