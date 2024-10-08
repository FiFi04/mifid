package pl.rg.security;

import java.util.Optional;

public interface SecurityModuleApi {

  Optional<String> encryptPassword(String password);

  Optional<String> decryptPassword(String encryptedPassword);
}
