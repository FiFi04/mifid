package pl.rg.users.model;

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
public class UserModel extends MifidGeneral<Integer> {

  @FieldCategory(dbColumn = true)
  public static final String USER_NAME = "user_name";

  @FieldCategory(dbColumn = true)
  public static final String PASSWORD = "password";

  @FieldCategory(dbColumn = true)
  public static final String FIRST_NAME = "first_name";

  @FieldCategory(dbColumn = true)
  public static final String LAST_NAME = "last_name";

  @FieldCategory(dbColumn = true)
  public static final String EMAIL = "email";

  public static final String TABLE_NAME = "user";

  @FieldCategory(dbField = true)
  private String userName;

  @FieldCategory(dbField = true)
  private String password;

  @FieldCategory(dbField = true)
  private String firstName;

  @FieldCategory(dbField = true)
  private String lastName;

  @FieldCategory(dbField = true)
  private String email;

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }
}
