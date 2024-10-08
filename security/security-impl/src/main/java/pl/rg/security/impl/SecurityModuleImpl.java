package pl.rg.security.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Cipher;
import pl.rg.security.SecurityModuleApi;
import pl.rg.security.exception.SecurityException;
import pl.rg.security.repository.PublicKeyHashRepository;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.db.PropertiesUtils;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

@Service
public class SecurityModuleImpl implements SecurityModuleApi {

  @Autowire
  private PublicKeyHashRepository publicKeyHashRepository;

  public static final String ALGORITHM_TYPE = "RSA";

  public Logger getLogger() {
    return LoggerImpl.getInstance();
  }

  public String getPrivateKeyDirectory() {
    return PropertiesUtils.getProperty(PropertiesUtils.PRIVATE_KEY);
  }

  @Override
  public Optional<String> encryptPassword(String password) {
    if (Boolean.parseBoolean(PropertiesUtils.getProperty("application.generateKeys"))) {
      generateRsaKeyPair();
    }
    try {
      Cipher encryptCipher = Cipher.getInstance(ALGORITHM_TYPE);
      Optional<PublicKey> publicKey = publicKeyHashRepository.getPublicKey();
      encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey.orElseThrow());
      return Optional.of(
          Base64.getEncoder().encodeToString(encryptCipher.doFinal(password.getBytes())));
    } catch (GeneralSecurityException e) {
      SecurityException exception = new SecurityException("Błąd podczas szyfrowania hasła",
          e);
      getLogger().logAndThrowRuntimeException(exception);
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> decryptPassword(String encryptedPassword) {
    try {
      Cipher decryptCipher = Cipher.getInstance(ALGORITHM_TYPE);
      Optional<PrivateKey> privateKey = getPrivateKey();
      decryptCipher.init(Cipher.DECRYPT_MODE, privateKey.orElseThrow());
      return Optional.of(
          new String(decryptCipher.doFinal(Base64.getDecoder().decode(encryptedPassword))));
    } catch (GeneralSecurityException e) {
      SecurityException exception = new SecurityException("Błąd podczas odszyfrowania hasła",
          e);
      getLogger().logAndThrowRuntimeException(exception);
    }
    return Optional.empty();
  }

  private boolean savePrivateKey(PrivateKey privateKey) {
    File keyFile = new File(getPrivateKeyDirectory());
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile))) {
      String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
      writer.write(privateKeyStr);
      return true;
    } catch (IOException e) {
      SecurityException exception = new SecurityException("Błąd zapisu klucza do pliku",
          e);
      getLogger().logAndThrowRuntimeException(exception);
    }
    return false;
  }

  private Optional<PrivateKey> getPrivateKey() {
    File keyFile = new File(getPrivateKeyDirectory());
    try {
      byte[] privateKeyBytes = Files.readAllBytes(keyFile.toPath());
      byte[] decodedPrivateKey = Base64.getDecoder().decode(privateKeyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_TYPE);
      PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
      return Optional.of(keyFactory.generatePrivate(privateKeySpec));
    } catch (IOException e) {
      SecurityException exception = new SecurityException("Błąd odczytu klucza z pliku",
          e);
      getLogger().logAndThrowRuntimeException(exception);
    } catch (GeneralSecurityException e) {
      SecurityException exception = new SecurityException("Błąd odszyfrowania klucza z pliku",
          e);
      getLogger().logAndThrowRuntimeException(exception);
    }
    return Optional.empty();
  }

  private void generateRsaKeyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM_TYPE);
      generator.initialize(2048);
      KeyPair keyPair = generator.generateKeyPair();
      publicKeyHashRepository.savePublicKey(keyPair.getPublic());
      savePrivateKey(keyPair.getPrivate());
    } catch (NoSuchAlgorithmException e) {
      SecurityException exception = new SecurityException("Błąd podczas tworzenia kluczy",
          e);
      getLogger().logAndThrowRuntimeException(exception);
    }
  }
}
