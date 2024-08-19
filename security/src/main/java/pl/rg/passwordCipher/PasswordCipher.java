package pl.rg.passwordCipher;

import java.util.Optional;

public interface PasswordCipher {

    Optional<String> encryptPassword(String password);

    Optional<String> decryptPassword(String encryptedPassword);
}
