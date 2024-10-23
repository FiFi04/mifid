package pl.rg.users;

import java.util.Optional;

public interface UserModuleApi {

  void addUser(User user);

  Optional<User> find(Integer userId);

  void update(User user);

  void delete (Integer userId);
}
