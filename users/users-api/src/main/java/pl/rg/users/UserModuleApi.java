package pl.rg.users;

import java.util.Optional;

public interface UserModuleApi {

  void addUser(User user);

  Optional<User> find(Integer userId);

  void update(User user);

  void delete(Integer userId);

  boolean validateLogInData(String username, String password);

  void startSession(String currentUser);

  void updateSession();

  void endSession();
}
