package pl.rg.users;

import pl.rg.users.impl.UserDtoImpl;

public interface UserModuleApi {

  void addUser(UserDtoImpl userDto);
}
