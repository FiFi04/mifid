package pl.rg.security;

import java.util.Optional;

public interface PasswordService {

  Optional<String> encryptPassword(String password);

  Optional<String> decryptPassword(String encryptedPassword);
}
