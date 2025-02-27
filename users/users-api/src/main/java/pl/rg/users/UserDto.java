package pl.rg.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.utils.annotation.Validate;
import pl.rg.utils.validator.enums.ValidatorCase;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserDto {

  private Integer id;

  private String userName;

  @Validate(validatorCase = ValidatorCase.TEXT, message = "Niepoprawne imię, powinno zawierać tylko litery")
  private String firstName;

  @Validate(validatorCase = ValidatorCase.TEXT, message = "Niepoprawne nazwisko, powinno zawierać tylko litery")
  private String lastName;

  @Validate(validatorCase = ValidatorCase.TEXT, message = "Nieprawidłowy format email", format = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
  private String email;

  private String blocked;

  public UserDto(String firstName, String lastName, String email) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }
}
