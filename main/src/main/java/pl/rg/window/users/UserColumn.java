package pl.rg.window.users;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserColumn {

  ID("Id", false, "id"),
  LOGIN("Login", false, "userName"),
  NAME("Imię", true, "firstName"),
  SURNAME("Nazwisko", true, "lastName"),
  EMAIL("Email", true, "email");

  private String name;

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
}
