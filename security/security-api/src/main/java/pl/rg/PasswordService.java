package pl.rg;

import java.security.PrivateKey;
import java.util.Optional;

public interface PasswordService {

    Optional<String> encryptPassword(String password);

    Optional<String> decryptPassword(String encryptedPassword);

    boolean savePrivateKey(PrivateKey privateKey);

    Optional<PrivateKey> getPrivateKey();
}
