package pl.rg.securityimpl.impl;

import pl.rg.securityapi.PasswordService;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.db.PropertiesUtils;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.securityimpl.repository.PublicKeyHashRepository;

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
    private PublicKeyHashRepository publicKeyHashRepository;

    private final String privateKeyDirectory = PropertiesUtils.getProperty("privateKey.directory");

    private final boolean shouldGenerateKeys = Boolean.parseBoolean(PropertiesUtils.getProperty("application.generateKeys"));

    private Logger logger = LoggerImpl.getInstance();

    public Optional<String> encryptPassword(String password) {
        if (shouldGenerateKeys) {
            generateRsaKeyPair();
        }
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            Optional<PublicKey> publicKey = publicKeyHashRepository.getPublicKey();
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

    private Optional<PrivateKey> getPrivateKey() {
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
            publicKeyHashRepository.savePublicKey(keyPair.getPublic());
            savePrivateKey(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e) {
            logger.logAndThrowException("Błąd podczas tworzenia kluczy: ", e);
        }
    }
}
