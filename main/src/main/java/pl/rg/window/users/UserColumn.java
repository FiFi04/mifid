package pl.rg.window.users;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.rg.users.model.UserModel;
import pl.rg.window.DataEnumColumn;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserColumn implements DataEnumColumn {

  ID("Id", UserModel.ID, false, "id"),
  LOGIN("Login", UserModel.USER_NAME, false, "userName"),
  NAME("Imię", UserModel.FIRST_NAME, true, "firstName"),
  SURNAME("Nazwisko", UserModel.LAST_NAME, true, "lastName"),
  EMAIL("Email", UserModel.EMAIL, true, "email"),
  BLOCKED_STATUS("Zablokowany", UserModel.BLOCKED_TIME, false, "blockedTime");

  private String name;

  private String dbColumn;

  private boolean visibility;

  private String javaAttribute;
}