package pl.rg.users.impl;

import java.util.Optional;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.User;
import pl.rg.users.UserDto;
import pl.rg.users.UserModuleApi;
import pl.rg.users.mapper.UserMapper;
import pl.rg.users.model.UserModel;
import pl.rg.users.repository.UserRepository;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

@Service
public class UserModuleImpl implements UserModuleApi {

  @Autowire
  private SecurityModuleApi securityModuleApi;

  @Autowire
  private UserRepository userRepository;

  private Logger logger = LoggerImpl.getInstance();

  UserMapper userMapper = UserMapper.INSTANCE;

  @Override
  public void addUser(UserDto userDto) {
    User user = userMapper.dtoToDomain(userDto);
    Optional<String> encryptedPassword = securityModuleApi.encryptPassword(user.getPassword());
    user.setPassword(encryptedPassword.get());
    UserModel userModel = userMapper.domainToUserModel(user);
    userRepository.save(userModel);
  }

  @Override
  public String generateUsername(String firstName, String lastName) {
    int userIndex = 1;
    String username = firstName.substring(0, 3) + lastName.substring(0, 3);
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
