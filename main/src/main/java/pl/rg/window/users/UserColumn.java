package pl.rg.window.users;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.rg.users.model.UserModel;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserColumn {

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

  static String[] getSearchColumns() {
    return Arrays.stream(values())
        .filter(u -> u.activeSearch)
        .map(UserColumn::getName)
        .toArray(String[]::new);
  }

  static String[] getColumnNames() {
    return Arrays.stream(values())
        .map(UserColumn::getName)
        .toArray(String[]::new);
  }

  static String getNameByJavaAttribute(String javaAttribute) {
    return Arrays.stream(values())
        .filter(v -> v.getJavaAttribute().equals(javaAttribute))
        .findFirst().get().getName();
  }

  static String getDbColumnByName(String name) {
    return Arrays.stream(values())
        .filter(column -> column.getName().equals(name))
        .map(UserColumn::getDbColumn)
        .findFirst().get();
  }
}