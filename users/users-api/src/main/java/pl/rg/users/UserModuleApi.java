package pl.rg.users;

import java.util.Optional;
import pl.rg.utils.pageAndSort.PageFilter;

public interface UserModuleApi extends PageFilter<User> {

  void addUser(User user);

  Optional<User> find(Integer userId);

  void update(User user);

  void delete(Integer userId);

  boolean validateLogInData(String username, String password);

  int checkAvailableLoginAttempts(String username);

  void resetLoginAttempts(String username);

  String getBlockedValue(User user);

  void startSession(String currentUser);

  void updateSession();

  void endSession();
}
