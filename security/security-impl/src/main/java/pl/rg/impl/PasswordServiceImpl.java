package pl.rg.impl;

import pl.rg.PasswordService;
import pl.rg.annotation.Autowire;
import pl.rg.annotation.Service;
import pl.rg.logger.Logger;
import pl.rg.logger.LoggerImpl;
import pl.rg.repository.PasswordRepository;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordServiceImpl implements PasswordService {

    @Autowire
    private PasswordRepository passwordRepository;

    private static PasswordServiceImpl passwordService;

    private Logger logger = LoggerImpl.getInstance();

    private PasswordServiceImpl() {
        initializePasswordServiceImpl();
    }

    private void initializePasswordServiceImpl() {
        generateRsaKeyPair();
    }

    public static PasswordServiceImpl getInstance() {
        if (passwordService == null) {
            passwordService = new PasswordServiceImpl();
        }
        return passwordService;
    }

    public Optional<String> encryptPassword(String password) {
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            Optional<PublicKey> publicKey = passwordRepository.getPublicKey();
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey.orElseThrow());
            return Optional.of(Base64.getEncoder().encodeToString(encryptCipher.doFinal(password.getBytes())));
        } catch (GeneralSecurityException e) {
            logger.logAndThrowException("Błąd podczas szyfrowania hasła: ", e);
            return Optional.empty();
        }
    }

    public Optional<String> decryptPassword(String encryptedPassword) {
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            Optional<PrivateKey> privateKey = passwordRepository.getPrivateKey();
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey.orElseThrow());
            return Optional.of(new String(decryptCipher.doFinal(Base64.getDecoder().decode(encryptedPassword))));
        } catch (GeneralSecurityException e) {
            logger.logAndThrowException("Błąd podczas odszyfrowania hasła: ", e);
            return Optional.empty();
        }
    }

    private void generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            passwordRepository.savePublicKey(keyPair.getPublic());
            passwordRepository.savePrivateKey(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e) {
            logger.logAndThrowException("Błąd podczas tworzenia kluczy: ", e);
        }
    }
}
