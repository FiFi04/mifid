package pl.rg.impl;

import pl.rg.PasswordService;
import pl.rg.annotation.Autowire;
import pl.rg.annotation.Service;
import pl.rg.db.PropertiesUtils;
import pl.rg.logger.Logger;
import pl.rg.logger.LoggerImpl;
import pl.rg.repository.PublicKeyHashRepository;

import javax.crypto.Cipher;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordServiceImpl implements PasswordService {

    @Autowire
    private PublicKeyHashRepository keyHashRepository;

    private final String privateKeyDirectory = PropertiesUtils.getProperty("privateKey.directory");

    private Logger logger = LoggerImpl.getInstance();

    public PasswordServiceImpl() {
        initializePasswordServiceImpl();
    }

    private void initializePasswordServiceImpl() {
        generateRsaKeyPair();
    }

    public Optional<String> encryptPassword(String password) {
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            Optional<PublicKey> publicKey = keyHashRepository.getPublicKey();
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
            Optional<PrivateKey> privateKey = getPrivateKey();
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey.orElseThrow());
            return Optional.of(new String(decryptCipher.doFinal(Base64.getDecoder().decode(encryptedPassword))));
        } catch (GeneralSecurityException e) {
            logger.logAndThrowException("Błąd podczas odszyfrowania hasła: ", e);
            return Optional.empty();
        }
    }

    public boolean savePrivateKey(PrivateKey privateKey) {
        File keyFile = new File(privateKeyDirectory);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile))) {
            String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            writer.write(privateKeyStr);
            return true;
        } catch (IOException e) {
            logger.logAndThrowException("Błąd zapisu klucza do pliku: ", e);
            return false;
        }
    }

    public Optional<PrivateKey> getPrivateKey() {
        File keyFile = new File(privateKeyDirectory);
        try {
            byte[] privateKeyBytes = Files.readAllBytes(keyFile.toPath());
            byte[] decodedPrivateKey = Base64.getDecoder().decode(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
            return Optional.of(keyFactory.generatePrivate(privateKeySpec));
        } catch (IOException e) {
            logger.logAndThrowException("Błąd odczytu klucza z pliku: ", e);
        } catch (GeneralSecurityException e) {
            logger.logAndThrowException("Błąd odszyfrowania klucza z pliku: ", e);
        }
        return Optional.empty();
    }

    private void generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            keyHashRepository.savePublicKey(keyPair.getPublic());
            savePrivateKey(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e) {
            logger.logAndThrowException("Błąd podczas tworzenia kluczy: ", e);
        }
    }
}
