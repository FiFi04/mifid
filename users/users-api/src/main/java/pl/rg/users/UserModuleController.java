package pl.rg.users;

import java.util.Optional;

public interface UserModuleController {

  boolean createUser(String firstName, String lastName, String email);

  Optional<UserDto> getUser(Integer userID);

  void updateUser(UserDto userDto);

  void deleteUser(Integer userId);

  boolean logIn(String username, String password);

  void logOut();
}
