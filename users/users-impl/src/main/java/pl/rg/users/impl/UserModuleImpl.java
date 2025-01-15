package pl.rg.users.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import lombok.Data;
import pl.rg.EmailModuleApi;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.User;
import pl.rg.users.UserModuleApi;
import pl.rg.users.mapper.UserMapper;
import pl.rg.users.model.SessionModel;
import pl.rg.users.model.UserModel;
import pl.rg.users.repository.SessionRepository;
import pl.rg.users.repository.UserRepository;
import pl.rg.users.session.SessionImpl;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.db.PropertiesUtils;
import pl.rg.utils.enums.EmailTemplateType;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

@Data
@Service
public class UserModuleImpl implements UserModuleApi {

  @Autowire
  private SecurityModuleApi securityModuleApi;

  @Autowire
  private UserRepository userRepository;

  @Autowire
  private SessionRepository sessionRepository;

  @Autowire
  EmailModuleApi emailModuleApi;

  private Logger logger = LoggerImpl.getInstance();

  private UserMapper userMapper = UserMapper.INSTANCE;

  private SessionImpl session = SessionImpl.getInstance();

  @Override
  public void addUser(User user) {
    user.setUserName(generateUsername(user.getFirstName(), user.getLastName()));
    String generatedPassword = securityModuleApi.generatePassword();
    Optional<String> encryptedPassword = securityModuleApi.encryptPassword(generatedPassword);
    user.setPassword(encryptedPassword.get());
    UserModel userModel = userMapper.domainToUserModel(user);
    logger.log(LogLevel.INFO, "Add new user with login {}", user.getUserName());
    userRepository.save(userModel);
    sendEmailNotification(EmailTemplateType.NEW_ACCOUNT, user, generatedPassword);
  }

  @Override
  public Optional<User> find(Integer id) {
    Optional<UserModel> userModel = userRepository.findById(id);
    if (userModel.isPresent()) {
      User user = userMapper.userModelToDomain(userModel.get());
      return Optional.of(user);
    } else {
      throw logger.logAndThrowRuntimeException(LogLevel.DEBUG,
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
    Optional<UserModel> userModel = userRepository.findByUsername(username);
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
  public int checkAvailableLoginAttempts(String username) {
    int maxLoginAttempts = PropertiesUtils.getIntProperty(PropertiesUtils.USER_MAX_LOGIN_ATTEMPTS);
    Optional<UserModel> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      throw logger.logAndThrowRuntimeException(LogLevel.DEBUG,
          new ApplicationException("U36GH", "Nie znaleziono użytownika o podanym loginie"));
    }
    UserModel userModel = user.get();
    if (isBlockedUser(userModel)) {
      return 0;
    }
    return maxLoginAttempts - getLoginAttempts(userModel, maxLoginAttempts);
  }

  @Override
  public void resetLoginAttempts(String username) {
    UserModel userModel = userRepository.findByUsername(username).get();
    userModel.setBlockedTime(null);
    userModel.setLoginAttempts(0);
    userRepository.save(userModel);
    logger.log(LogLevel.INFO, "Oblokowano użytkownika " + username);
  }

  @Override
  public void startSession(String currentUser) {
    session.setStartTimeCounter(LocalTime.now());
    session.setActiveSession(
        new SessionModel(currentUser, generateToken(), LocalDateTime.now(), null));
    logger.log(LogLevel.INFO, "Rozpoczęto sesję " + session.getActiveSession().getToken());
  }

  @Override
  public void updateSession() {
    LocalTime currentTime = LocalTime.now();
    Duration actionDuration = Duration.between(session.getStartTimeCounter(), currentTime);
    if (actionDuration.getSeconds() < session.getSessionMaxTimeInSeconds()) {
      session.setStartTimeCounter(currentTime);
    } else {
      endSession();
      throw logger.logAndThrowRuntimeException(LogLevel.DEBUG, new ApplicationException("U33SE",
          "Wylogowano z powodu zbyt długiego czasu nieaktywności"));
    }
  }

  @Override
  public void endSession() {
    session.getActiveSession().setLogoutTime(LocalDateTime.now());
    sessionRepository.save(session.getActiveSession());
    logger.log(LogLevel.INFO, "Zakończono sesję " + session.getActiveSession().getToken());
    session.setActiveSession(null);
  }

  @Override
  public SessionImpl getCurrentUserSession() {
    return session;
  }

  @Override
  public List<User> getFiltered(List<Filter> filters) {
    return userRepository.findAll(filters).stream()
        .map(userMapper::userModelToDomain)
        .toList();
  }

  @Override
  public MifidPage<User> getPage(List<Filter> filters, Page page) {
    MifidPage<UserModel> userModelPage = userRepository.findAll(filters, page);
    return userMapper.userModelPageToUserPage(userModelPage);
  }

  private boolean isBlockedUser(UserModel userModel) {
    LocalDateTime blockedTime = userModel.getBlockedTime();
    int blockedHours = PropertiesUtils.getIntProperty(PropertiesUtils.USER_BLOCKED_HOURS);
    if (blockedTime != null) {
      Duration blockedDuration = Duration.between(blockedTime, LocalDateTime.now());
      if (blockedDuration.toHours() >= blockedHours) {
        userModel.setLoginAttempts(0);
        userModel.setBlockedTime(null);
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  private int getLoginAttempts(UserModel userModel, int maxLoginAttempts) {
    int loginAttempts = userModel.getLoginAttempts();
    if (loginAttempts < maxLoginAttempts) {
      loginAttempts++;
      if (loginAttempts == maxLoginAttempts) {
        blockUser(userModel, loginAttempts);
      } else {
        userModel.setLoginAttempts(loginAttempts);
        userRepository.save(userModel);
      }
      return loginAttempts;
    }
    return maxLoginAttempts;
  }

  private void blockUser(UserModel userModel, int loginAttempts) {
    userModel.setLoginAttempts(loginAttempts);
    userModel.setBlockedTime(LocalDateTime.now());
    userRepository.save(userModel);
  }

  private void sendEmailNotification(EmailTemplateType emailTemplateType, User user,
      String generatedPassword) {
    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("$firstName", user.getFirstName());
    placeholders.put("$lastName", user.getLastName());
    placeholders.put("$username", user.getUserName());
    placeholders.put("$password", generatedPassword);
    emailModuleApi.sendNotification(emailTemplateType.getWindowColumnName(), user.getEmail(),
        placeholders);
    logger.log(LogLevel.INFO,
        "Wiadomość o utworzeniu użytkownika została wysłana do " + user.getEmail());
  }

  private String generateUsername(String firstName, String lastName) {
    int userIndex = 1;
    String firstPart = firstName.length() > 3 ? firstName.substring(0, 3) : firstName;
    String secondPart = lastName.length() > 3 ? lastName.substring(0, 3) : lastName;
    String username = (firstPart + secondPart).toLowerCase();
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