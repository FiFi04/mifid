package pl.rg.users.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.User;
import pl.rg.users.UserModuleApi;
import pl.rg.users.mapper.UserMapper;
import pl.rg.users.model.SessionModel;
import pl.rg.users.model.UserModel;
import pl.rg.users.repository.SessionRepository;
import pl.rg.users.repository.UserRepository;
import pl.rg.users.session.Session;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

@Service
public class UserModuleImpl implements UserModuleApi {

  @Autowire
  private SecurityModuleApi securityModuleApi;

  @Autowire
  private UserRepository userRepository;

  @Autowire
  private SessionRepository sessionRepository;

  private Logger logger = LoggerImpl.getInstance();

  private UserMapper userMapper = UserMapper.INSTANCE;

  private Session session = Session.getInstance();

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
      return decryptedPassword.get().equals(password);
    }
  }

  @Override
  public void startSession(String currentUser) {
    session.setStartTimeCounter(LocalTime.now());
    session.setActiveSession(
        new SessionModel(currentUser, generateToken(), LocalDateTime.now(), null));
    logger.log("Rozpoczęto sesję " + session.getActiveSession().getToken());
  }

  @Override
  public void updateSession() {
    LocalTime currentTime = LocalTime.now();
    Duration actionDuration = Duration.between(session.getStartTimeCounter(), currentTime);
    if (actionDuration.getSeconds() < session.getSessionMaxTimeInSeconds()) {
      session.setStartTimeCounter(currentTime);
    } else {
      endSession();
      throw logger.logAndThrowRuntimeException(new ApplicationException("U33SE",
          "Wylogowano z powodu zbyt długiego czasu nieaktywności"));
    }
  }

  @Override
  public void endSession() {
    session.getActiveSession().setLogoutTime(LocalDateTime.now());
    sessionRepository.save(session.getActiveSession());
    logger.log("Zakończono sesję " + session.getActiveSession().getToken());
    session.setActiveSession(null);
  }

  @Override
  public List<User> getFiltered(List<Filter> filters) {
    List<UserModel> filteredUserModels = userRepository.findAll(filters);
    List<User> filteredUsers = new ArrayList<>();
    for (UserModel filteredUserModel : filteredUserModels) {
      filteredUsers.add(userMapper.userModelToDomain(filteredUserModel));
    }
    return filteredUsers;
  }

  @Override
  public MifidPage<User> getPage(List<Filter> filters, Page page) {
    MifidPage<UserModel> userModelPage = userRepository.findAll(filters, page);
    MifidPage<User> userPage = new MifidPage<>(userModelPage.getTotalObjects(),
        userModelPage.getTotalPage(), userModelPage.getObjectFrom(), userModelPage.getObjectTo(),
        new ArrayList<>());
    List<User> limitedUsersPage = new ArrayList<>();
    for (UserModel userModel : userModelPage.getLimitedObjects()) {
      limitedUsersPage.add(userMapper.userModelToDomain(userModel));
    }
    userPage.setLimitedObjects(limitedUsersPage);
    return userPage;
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

  private String generateToken() {
    int tokenLength = 20;
    String tokenChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    StringBuilder token = new StringBuilder();
    Random random = new Random();
    for (int i = 0; i < tokenLength; i++) {
      token.append(tokenChars.charAt(random.nextInt(tokenChars.length())));
    }
    return token.toString();
  }
}