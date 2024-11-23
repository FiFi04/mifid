package pl.rg.security.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rg.security.exception.SecurityException;
import pl.rg.security.repository.PublicKeyHashRepository;
import pl.rg.utils.db.DBConnector;
import pl.rg.utils.db.PropertiesUtils;
import pl.rg.utils.logger.LoggerImpl;

public class SecurityModuleTest {

  @Mock
  private PublicKeyHashRepository publicKeyHashRepository;

  @Mock
  private DBConnector dbConnector;

  @Mock
  private LoggerImpl logger;

  @Mock
  private Connection connection;

  private KeyPairTestModel keyPairTestModel = new KeyPairTestModel();

  private KeyPair keyPair = keyPairTestModel.getValidKeyPair();

  @InjectMocks
  private SecurityModuleImpl securityModule;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void whenEncryptPasswordWithEmptyKeys_thenShouldThrowSecurityException() {
    // given
    KeyPair keyPair = keyPairTestModel.getEmptyKeyPar();
    Exception exceptionThrown = null;

    try (MockedStatic<DBConnector> dbConnectorMockedStatic = mockStatic(DBConnector.class);
        MockedStatic<DriverManager> driverManagerMockedStatic = mockStatic(DriverManager.class);
        MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class)) {

      dbConnectorMockedStatic.when(DBConnector::getInstance).thenReturn(dbConnector);
      driverManagerMockedStatic.when(
              () -> DriverManager.getConnection(anyString(), anyString(), anyString()))
          .thenReturn(connection);
      when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));
      when(dbConnector.getConnection()).thenReturn(connection);
      loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);
      doThrow(new SecurityException("Błąd podczas szyfrowania hasła")).when(logger)
          .logAndThrowRuntimeException(any(), any(RuntimeException.class));

      // when
      try {
        securityModule.encryptPassword(KeyPairTestModel.PASSWORD);
      } catch (SecurityException e) {
        exceptionThrown = e;
      }

      // then
      assertNotNull(exceptionThrown);
      assertInstanceOf(SecurityException.class, exceptionThrown);
      assertEquals("Błąd podczas szyfrowania hasła", exceptionThrown.getMessage());
    }
  }

  @Test
  public void whenEncryptPasswordWithValidKeys_thenShouldReturnEncryptedPassword() {
    //given
    when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));

    //when
    Optional<String> encryptedPassword = securityModule.encryptPassword(KeyPairTestModel.PASSWORD);

    //then
    assertNotNull(encryptedPassword);
    assertTrue(encryptedPassword.isPresent());
  }

  @Test
  public void whenDecryptPasswordWithValidKeys_thenReturnOriginalPassword() {
    //given
    try (MockedStatic<PropertiesUtils> propertiesMockedStatic = mockStatic(PropertiesUtils.class)) {

      propertiesMockedStatic.when(() -> PropertiesUtils.getProperty(PropertiesUtils.PRIVATE_KEY))
          .thenReturn("src/test/resources/private.key");
      when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));

      Optional<String> encryptedPassword = securityModule.encryptPassword(
          KeyPairTestModel.PASSWORD);

      //when
      Optional<String> decryptedPassword = securityModule.decryptPassword(encryptedPassword.get());

      //then
      assertTrue(decryptedPassword.isPresent());
      assertEquals(KeyPairTestModel.PASSWORD, decryptedPassword.get());
    }
  }

  @Test
  public void whenDecryptPasswordWithEmptyKeyFile_thenShouldThrowSecurityException(
      @TempDir Path tempDir) {
    //given
    Exception exceptionThrown = null;
    Path tempEmptyKeyFilePath = tempDir.resolve("tempEmptyKeyFile");

    try (MockedStatic<PropertiesUtils> propertiesMockedStatic = mockStatic(PropertiesUtils.class);
        MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class)) {

      propertiesMockedStatic.when(() -> PropertiesUtils.getProperty(PropertiesUtils.PRIVATE_KEY))
          .thenReturn(tempEmptyKeyFilePath.toString());
      when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));
      loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);

      Optional<String> encryptedPassword = securityModule.encryptPassword(
          KeyPairTestModel.PASSWORD);
      doThrow(new SecurityException("Błąd odczytu klucza z pliku")).when(logger)
          .logAndThrowRuntimeException(any(), any(RuntimeException.class));

      //when
      try {
        securityModule.decryptPassword(encryptedPassword.get());
      } catch (RuntimeException e) {
        exceptionThrown = e;
      }

      //then
      assertNotNull(exceptionThrown);
      assertInstanceOf(SecurityException.class, exceptionThrown);
      assertEquals("Błąd odczytu klucza z pliku", exceptionThrown.getMessage());
    }
  }

  @Test
  public void whenDecryptPasswordWithIncorrectPasswordHash_thenShouldThrowSecurityException() {
    //given
    Exception exceptionThrown = null;
    String invalidPasswordHash = "InvalidPasswordHash";

    try (MockedStatic<PropertiesUtils> propertiesMockedStatic = mockStatic(PropertiesUtils.class);
        MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class)) {

      propertiesMockedStatic.when(() -> PropertiesUtils.getProperty(PropertiesUtils.PRIVATE_KEY))
          .thenReturn("src/test/resources/private.key");
      loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);
      doThrow(new SecurityException("Błąd podczas odszyfrowania hasła")).when(logger)
          .logAndThrowRuntimeException(any(), any(RuntimeException.class));

      //when
      try {
        securityModule.decryptPassword(invalidPasswordHash);
      } catch (SecurityException e) {
        exceptionThrown = e;
      }

      //then
      assertNotNull(exceptionThrown);
      assertInstanceOf(SecurityException.class, exceptionThrown);
      assertEquals("Błąd podczas odszyfrowania hasła", exceptionThrown.getMessage());
    }
  }

  @Test
  public void whenGenerateKeysPropertyIsTrue_thenShouldGenerateKeyPair(@TempDir Path tempDir) {
    //given
    Path privateKeyPath = tempDir.resolve("private.key");

    try (MockedStatic<PropertiesUtils> propertiesMockedStatic = mockStatic(PropertiesUtils.class)) {
      propertiesMockedStatic.when(() -> PropertiesUtils.getProperty("application.generateKeys"))
          .thenReturn("true");
      propertiesMockedStatic.when(() -> PropertiesUtils.getProperty(PropertiesUtils.PRIVATE_KEY))
          .thenReturn(privateKeyPath.toString());
      when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));
      Optional<PublicKey> publicKey = publicKeyHashRepository.getPublicKey();

      //when
      securityModule.encryptPassword(KeyPairTestModel.PASSWORD);

      //then
      assertNotEquals(0, privateKeyPath.toFile().length());
      assertTrue(publicKey.isPresent());
    }
  }
}
