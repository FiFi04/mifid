package pl.rg.users;

public interface UserModuleApi {

  User createUser(String userName, String firsName, String lastName, String email);
}
