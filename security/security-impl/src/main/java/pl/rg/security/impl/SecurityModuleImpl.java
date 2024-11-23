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
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.crypto.Cipher;
import pl.rg.security.SecurityModuleApi;
import pl.rg.security.exception.SecurityException;
import pl.rg.security.repository.PublicKeyHashRepository;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.db.PropertiesUtils;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.validator.impl.BaseValidator;

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
      throw getLogger().logAndThrowRuntimeException(LogLevel.ERROR,
          new SecurityException("Błąd podczas szyfrowania hasła",
              e));
    }
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
      throw getLogger().logAndThrowRuntimeException(LogLevel.ERROR,
          new SecurityException("Błąd podczas odszyfrowania hasła",
              e));
    }
  }

  @Override
  public String generatePassword() {
    int passwordMaxLength = 20;
    String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
    String digits = "0123456789";
    String specialCharacters = BaseValidator.SPECIAL_CHARACTERS;
    double[] weights = getWeights();
    List<String> password = new ArrayList<>();
    SecureRandom secureRandom = new SecureRandom();
    int passwordLength = secureRandom.nextInt(8, passwordMaxLength);

    password.add(getRandomChar(upperCaseLetters, secureRandom));
    password.add(getRandomChar(lowerCaseLetters, secureRandom));
    password.add(getRandomChar(digits, secureRandom));
    password.add(getRandomChar(specialCharacters, secureRandom));

    for (int i = password.size(); i < passwordLength; i++) {
      double value = secureRandom.nextDouble();
      if (value < weights[0]) {
        password.add(getRandomChar(upperCaseLetters, secureRandom));
      } else if (value < weights[1]) {
        password.add(getRandomChar(lowerCaseLetters, secureRandom));
      } else if (value < weights[2]) {
        password.add(getRandomChar(digits, secureRandom));
      } else {
        password.add(getRandomChar(specialCharacters, secureRandom));
      }
    }
    Collections.shuffle(password);
    return String.join("", password);
  }

  private String getRandomChar(String availableChars, SecureRandom secureRandom) {
    return String.valueOf(availableChars.charAt(secureRandom.nextInt(availableChars.length())));
  }

  private double[] getWeights() {
    double upperCaseWeight = 0.35;
    double lowerCaseWeight = 0.35;
    double digitsWeight = 0.2;
    double specialCharactersWeight = 0.1;
    double[] weights = new double[4];
    weights[0] = upperCaseWeight;
    weights[1] = weights[0] + lowerCaseWeight;
    weights[2] = weights[1] + digitsWeight;
    weights[3] = weights[2] + specialCharactersWeight;
    return weights;
  }

  private boolean savePrivateKey(PrivateKey privateKey) {
    File keyFile = new File(getPrivateKeyDirectory());
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile))) {
      String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
      writer.write(privateKeyStr);
      return true;
    } catch (IOException e) {
      throw getLogger().logAndThrowRuntimeException(LogLevel.ERROR,
          new SecurityException("Błąd zapisu klucza do pliku",
              e));
    }
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
      throw getLogger().logAndThrowRuntimeException(LogLevel.ERROR,
          new SecurityException("Błąd odczytu klucza z pliku",
              e));
    } catch (GeneralSecurityException e) {
      throw getLogger().logAndThrowRuntimeException(LogLevel.ERROR,
          new SecurityException("Błąd odszyfrowania klucza z pliku",
              e));
    }
  }

  private void generateRsaKeyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM_TYPE);
      generator.initialize(2048);
      KeyPair keyPair = generator.generateKeyPair();
      publicKeyHashRepository.savePublicKey(keyPair.getPublic());
      savePrivateKey(keyPair.getPrivate());
    } catch (NoSuchAlgorithmException e) {
      throw getLogger().logAndThrowRuntimeException(LogLevel.ERROR,
          new SecurityException("Błąd podczas tworzenia kluczy",
              e));
    }
  }
}
