package pl.rg.window.emails;

import java.util.Arrays;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.rg.emails.model.EmailModel;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EmailColumn {

  ID("Id", EmailModel.ID, false, true, "id"),
  SUBJECT("Temat", EmailModel.SUBJECT, true, true, "subject"),
  BODY("Treść", EmailModel.BODY, true, true, "body"),
  SENDER("Użytkownik", EmailModel.SENDER, false, true, "sender"),
  RECIPIENT("Do", EmailModel.RECIPIENT, true, true, "recipient"),
  RECIPIENT_CC("Do (kopia)", EmailModel.RECIPIENT_CC, true, true, "recipientCc"),
  STATUS("Status", EmailModel.STATUS, false, true, "status"),
  ERROR_MESSAGE("Błąd", EmailModel.ERROR_MESSAGE, false, false, "errorMessage"),
  SENT_ATTEMPTS("Ilość prób wysyłki", EmailModel.SENT_ATTEMPTS, false, false, "sentAttempts");

  private String name;

  private String dbColumn;

  private boolean visibility;

  private boolean activeSearch;

  private String javaAttribute;

  public static String[] getSearchColumns() {
    return Arrays.stream(values())
        .filter(e -> e.activeSearch)
        .map(e -> e.getName())
        .toArray(size -> new String[size]);
  }

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
        .map(EmailColumn::getDbColumn)
        .findFirst();
  }
}