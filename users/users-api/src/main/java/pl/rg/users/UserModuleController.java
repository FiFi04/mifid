package pl.rg.users;

import pl.rg.users.impl.UserDtoImpl;

public interface UserModuleController {

  UserDtoImpl createUser(String firstName, String lastName, String email);
}
