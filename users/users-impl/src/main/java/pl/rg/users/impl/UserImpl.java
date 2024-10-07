package pl.rg.users.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.users.User;
import pl.rg.utils.annotation.Validate;
import pl.rg.utils.validator.enums.ValidatorCase;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserImpl implements User {

  private Integer id;

  @Validate(validatorCase = ValidatorCase.TEXT, message = "Niepoprawny format nazwy użytkownika", format = "^\\w+$")
  private String userName;

  @Validate(validatorCase = ValidatorCase.PASSWORD, message = "Hasło powinno zawierać przynajmniej jedną wielką literę, jedną małą, cyfrę oraz znak specjalny")
  private String password;

  @Validate(validatorCase = ValidatorCase.TEXT, message = "Niepoprawne imię, powinno zawierać tylko litery")
  private String firstName;

  @Validate(validatorCase = ValidatorCase.TEXT, message = "Niepoprawne nazwisko, powinno zawierać tylko litery")
  private String lastName;

  @Validate(validatorCase = ValidatorCase.TEXT, message = "Nieprawidłowy format email", format = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
  private String email;

  public UserImpl(String userName, String password, String firstName, String lastName,
      String email) {
    this.userName = userName;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }
}
