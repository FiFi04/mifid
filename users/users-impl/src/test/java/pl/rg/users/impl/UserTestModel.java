package pl.rg.users.impl;

import java.util.List;
import pl.rg.users.User;
import pl.rg.users.UserDto;
import pl.rg.users.model.UserModel;

public class UserTestModel {

  public static final String GENERATED_PASSWORD = "Password123!";

  public static final String ENCRYPTED_PASSWORD = "EncryptedPassword123!";

  public List<UserModel> returnUserModelList() {
    List<UserModel> userModels = List.of(
        new UserModel("jankow", "password1", "Jan", "Kowalski", "jan.kowalski@example.com"),
        new UserModel("tomnow", "password2", "Tomasz", "Nowak", "tomasz.nowak@example.com"));
    userModels.get(0).setId(1);
    userModels.get(1).setId(2);

    return userModels;
  }

  public List<User> returnUserList() {
    List<User> users = List.of(
        new UserImpl(1, "jankow", "password1", "Jan", "Kowalski", "jan.kowalski@example.com", null),
        new UserImpl(2, "tomnow", "password2", "Tomasz", "Nowak", "tomasz.nowak@example.com", null));

    return users;
  }

  public List<UserDto> returnUserDtoList() {
    List<UserDto> userDtos = List.of(
        new UserDto(1, "jankow", "Jan", "Kowalski", "jan.kowalski@example.com", null),
        new UserDto(2, "tomnow", "Tomasz", "Nowak", "tomasz.nowak@example.com", null));

    return userDtos;
  }

  public UserModel returnUserModel() {
    UserModel userModel = new UserModel("jankow", ENCRYPTED_PASSWORD, "Jan",
        "Kowalski", "jan.kowalski@email.com");
    userModel.setId(1);

    return userModel;
  }

  public User returnUser(String lastName) {
    User user = new UserImpl();
    user.setFirstName("Jan");
    user.setLastName(lastName);
    user.setEmail("j.kowalski@email.com");

    return user;
  }
}
