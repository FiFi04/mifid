package pl.rg.window.users;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserColumn {

  ID("Id", false),
  LOGIN("Login", false),
  NAME("Imię", true),
  SURNAME("Nazwisko", true),
  EMAIL("Email", true);

  private String name;

  private boolean visibility;

  public static String[] getColumnNames() {
    return Arrays.stream(values()).map(u -> u.getName()).toArray(size -> new String[size]);
  }
}
