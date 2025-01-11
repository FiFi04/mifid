package pl.rg.users.impl;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.users.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserImpl implements User {

  private Integer id;

  private String userName;

  private String password;

  private String firstName;

  private String lastName;

  private String email;

  private LocalDateTime blockedTime;

  public UserImpl(String userName, String password, String firstName, String lastName,
      String email) {
    this.userName = userName;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }
}
