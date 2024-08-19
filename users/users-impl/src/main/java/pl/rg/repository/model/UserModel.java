package pl.rg.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.annotation.FieldCategory;
import pl.rg.repository.MifidGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserModel extends MifidGeneral<Integer> {

    @FieldCategory(dbColumn = true)
    public final static String USER_NAME = "user_name";

    @FieldCategory(dbColumn = true)
    public final static String PASSWORD = "password";

    @FieldCategory(dbColumn = true)
    public final static String FIRST_NAME = "first_name";

    @FieldCategory(dbColumn = true)
    public final static String LAST_NAME = "last_name";

    @FieldCategory(dbColumn = true)
    public final static String EMAIL = "email";

    @FieldCategory(dbColumn = true)
    public final static String ROLE = "role";

    private final static String TABLE_NAME = "user";

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

    @FieldCategory(dbField = true)
    private String role;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }
}
