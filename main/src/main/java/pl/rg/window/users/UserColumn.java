package pl.rg.window.users;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.rg.users.model.UserModel;
import pl.rg.window.DataEnumColumn;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserColumn implements DataEnumColumn {

  ID("Id", UserModel.ID, false, true, "id"),
  LOGIN("Login", UserModel.USER_NAME, false, true, "userName"),
  NAME("Imię", UserModel.FIRST_NAME, true, true, "firstName"),
  SURNAME("Nazwisko", UserModel.LAST_NAME, true, true, "lastName"),
  EMAIL("Email", UserModel.EMAIL, true, true, "email"),
  BLOCKED_STATUS("Zablokowany", UserModel.BLOCKED_TIME, false, true, "blockedTime");

  private String name;

  private String dbColumn;

  private boolean visibility;

  private boolean activeSearch;

  private String javaAttribute;

  public static String[] getSearchColumns() {
    return Arrays.stream(values())
        .filter(u -> u.activeSearch)
        .map(u -> u.getName())
        .toArray(size -> new String[size]);
  }

  public static String[] getColumnNames() {
    return Arrays.stream(values())
        .map(u -> u.getName())
        .toArray(size -> new String[size]);
  }
}