package pl.rg.users;

import java.time.LocalDateTime;

public interface User {

  Integer getId();

  void setId(Integer id);

  String getUserName();

  void setUserName(String userName);

  String getPassword();

  void setPassword(String password);

  String getFirstName();

  void setFirstName(String firstName);

  String getLastName();

  void setLastName(String lastName);

  String getEmail();

  void setEmail(String email);

  LocalDateTime getBlockedTime();

  void setBlockedTime(LocalDateTime blockedTime);
}
