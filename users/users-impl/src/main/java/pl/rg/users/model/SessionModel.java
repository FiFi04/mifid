package pl.rg.users.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.utils.annotation.FieldCategory;
import pl.rg.utils.repository.MifidGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SessionModel extends MifidGeneral<Integer> {

  @FieldCategory(dbColumn = true)
  public final static String USER = "user";

  @FieldCategory(dbColumn = true)
  public final static String TOKEN = "token";

  @FieldCategory(dbColumn = true)
  public final static String LOGIN_TIME = "login_time";

  @FieldCategory(dbColumn = true)
  public final static String LOGOUT_TIME = "logout_time";

  private final static String TABLE_NAME = "session";

  @FieldCategory(dbField = true)
  private String user;

  @FieldCategory(dbField = true)
  private String token;

  @FieldCategory(dbField = true)
  private LocalDateTime loginTime;

  @FieldCategory(dbField = true)
  private LocalDateTime logoutTime;

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }
}