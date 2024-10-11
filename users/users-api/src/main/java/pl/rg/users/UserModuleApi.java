package pl.rg.users;

public interface UserModuleApi {

  void addUser(UserDto userDto);

  String generateUsername(String firstName, String lastName);
}
