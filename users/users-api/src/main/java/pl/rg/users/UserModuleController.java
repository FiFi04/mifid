package pl.rg.users;

public interface UserModuleController {

  boolean createUser(String firstName, String lastName, String email);

  void showUserForm();
}
