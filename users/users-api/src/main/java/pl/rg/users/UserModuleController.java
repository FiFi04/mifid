package pl.rg.users;

public interface UserModuleController {

  UserDto createUser(String firstName, String lastName, String email);

  void addUser(UserDto userDto);
}
