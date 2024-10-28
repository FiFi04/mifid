package pl.rg.users.impl;

import java.util.Optional;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.User;
import pl.rg.users.UserModuleApi;
import pl.rg.users.mapper.UserMapper;
import pl.rg.users.model.UserModel;
import pl.rg.users.repository.UserRepository;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

@Service
public class UserModuleImpl implements UserModuleApi {

  @Autowire
  private SecurityModuleApi securityModuleApi;

  @Autowire
  private UserRepository userRepository;

  private Logger logger = LoggerImpl.getInstance();

  private UserMapper userMapper = UserMapper.INSTANCE;

  @Override
  public void addUser(User user) {
    user.setUserName(generateUsername(user.getFirstName(), user.getLastName()));
    String generatePassword = securityModuleApi.generatePassword();
    Optional<String> encryptedPassword = securityModuleApi.encryptPassword(generatePassword);
    user.setPassword(encryptedPassword.get());
    UserModel userModel = userMapper.domainToUserModel(user);
    logger.log("Add new user with login {}", user.getUserName());
    userRepository.save(userModel);
  }

  @Override
  public Optional<User> find(Integer id) {
    Optional<UserModel> userModel = userRepository.findById(id);
    if (userModel.isPresent()) {
      User user = userMapper.userModelToDomain(userModel.get());
      return Optional.of(user);
    } else {
      throw logger.logAndThrowRuntimeException(
          new ApplicationException("U32GH", "Nie znaleziono użytownika o id: " + id));
    }
  }

  @Override
  public void update(User user) {
    UserModel userModel = userMapper.domainToUserModel(user);
    userRepository.save(userModel);
  }

  @Override
  public void delete(Integer userId) {
    userRepository.deleteById(userId);
  }

  @Override
  public boolean validateLogInData(String username, String password) {
    Optional<UserModel> userModel = userRepository.getByUsername(username);
    if (userModel.isEmpty()) {
      return false;
    } else {
      UserModel model = userModel.get();
      Optional<String> decryptedPassword = securityModuleApi.decryptPassword(
          model.getPassword());
      boolean validUsername = model.getUserName().equals(username);
      boolean validPassword = decryptedPassword.get().equals(password);
      return validPassword && validUsername;
    }
  }

  private String generateUsername(String firstName, String lastName) {
    int userIndex = 1;
    String username = (firstName.substring(0, 3) + lastName.substring(0, 3)).toLowerCase();
    while (userRepository.containsUsername(username)) {
      if (userIndex == 1) {
        username = username + userIndex;
      } else {
        username = username.substring(0, username.length() - 1) + userIndex;
      }
      userIndex++;
    }
    return username;
  }
}