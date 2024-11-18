package pl.rg.users.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import pl.rg.users.User;
import pl.rg.users.UserDto;
import pl.rg.users.UserModuleApi;
import pl.rg.users.UserModuleController;
import pl.rg.users.mapper.UserMapper;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Controller;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.exception.ValidationException;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;
import pl.rg.utils.validator.api.ValidatorService;

@Controller
public class UserModuleControllerImpl implements UserModuleController {

  @Autowire
  private UserModuleApi userModuleApi;

  @Autowire
  private ValidatorService validatorService;

  private UserMapper userMapper = UserMapper.INSTANCE;

  private Logger logger = LoggerImpl.getInstance();

  @Override
  public boolean createUser(String firsName, String lastName, String email) {
    userModuleApi.updateSession();
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
    userModuleApi.updateSession();
    Optional<User> userDomain = userModuleApi.find(userID);
    return userDomain.map(user -> userMapper.domainToDto(user));
  }

  @Override
  public void updateUser(UserDto userDto) {
    userModuleApi.updateSession();
    User user = userMapper.dtoToDomain(userDto);
    userModuleApi.update(user);
  }

  @Override
  public void deleteUser(Integer userId) {
    userModuleApi.updateSession();
    userModuleApi.delete(userId);
  }

  @Override
  public boolean logIn(String username, String password) {
    boolean validLogInData = userModuleApi.validateLogInData(username, password);
    if (validLogInData) {
      userModuleApi.startSession(username);
      return true;
    } else {
      throw logger.logAndThrowRuntimeException(
          new ApplicationException("U34LV", "Błędne dane podczas logowania"));
    }
  }

  @Override
  public void logOut() {
    userModuleApi.endSession();
  }

  @Override
  public List<UserDto> getFiltered(List<Filter> filters) {
    return userModuleApi.getFiltered(filters).stream()
        .map(userMapper::domainToDto)
        .toList();
  }

  @Override
  public MifidPage getPage(List<Filter> filters, Page page) {
    MifidPage<User> userPage = userModuleApi.getPage(filters, page);
    return userMapper.userPageToUserDtoPage(userPage);
  }
}


