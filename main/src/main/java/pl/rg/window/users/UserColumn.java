package pl.rg.window.users;

import java.util.Arrays;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.rg.users.model.UserModel;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserColumn {

  ID("Id", UserModel.ID, false, "id"),
  LOGIN("Login", UserModel.USER_NAME, false, "userName"),
  NAME("Imię", UserModel.FIRST_NAME, true, "firstName"),
  SURNAME("Nazwisko", UserModel.LAST_NAME, true, "lastName"),
  EMAIL("Email", UserModel.EMAIL, true, "email");

  private String name;

  private String dbColum;

  private boolean visibility;

  private String javaAttribute;

  public static String[] getColumnNames() {
    return Arrays.stream(values())
        .map(u -> u.getName())
        .toArray(size -> new String[size]);
  }

  public static String getNameByJavaAttribute(String javaAttribute) {
    return Arrays.stream(values())
        .filter(v -> v.getJavaAttribute().equals(javaAttribute))
        .findFirst().get().getName();
  }

  public static Optional<String> getDbColumnByName(String name) {
    return Arrays.stream(values())
        .filter(column -> column.name.equals(name))
        .map(UserColumn::getDbColum)
        .findFirst();
  }
}
