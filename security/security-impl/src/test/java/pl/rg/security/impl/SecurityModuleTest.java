package pl.rg.security.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rg.security.exception.SecurityException;
import pl.rg.security.repository.PublicKeyHashRepository;
import pl.rg.utils.db.DBConnector;
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

  private static KeyPairTestModel keyPairTestModel;

  private static KeyPair keyPair;

  @InjectMocks
  private SecurityModuleImpl securityModule;

  @BeforeAll
  public static void beforeAll() {
    keyPairTestModel = new KeyPairTestModel();
    keyPair = keyPairTestModel.getValidKeyPair();
  }

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void whenEncryptPasswordWithEmptyKeys_thenShouldThrowSecurityException() {
    // given
    // tu wypisujemy wszystkie dane wejsciowe potrzebne do przetestowania metody.
    String password = "Password123!"; // typu proste mogę być od razu deklarowane tutaj
    KeyPair keyPair = keyPairTestModel.getEmptyKeyPar(); // typy bardziej zlozony wyrzucamy do modelu testowego
    Exception exceptionThrown = null;

    // poniżej są wszystkie mocki, powinny być one zadekladowane w takiej samej kolejnosci jak jest wywołanie w testowanej metodzie
    // jeżeli jedna metoda jest wywołana kilka razy to musi być zamockowana też 2 razy.
    MockedStatic<DBConnector> dbConnectorMockedStatic = mockStatic(DBConnector.class);
    MockedStatic<DriverManager> driverManagerMockedStatic = mockStatic(DriverManager.class);
    MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class);
    dbConnectorMockedStatic.when(DBConnector::getInstance).thenReturn(dbConnector);
    driverManagerMockedStatic.when(
            () -> DriverManager.getConnection(anyString(), anyString(), anyString()))
        .thenReturn(connection);
    when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));
    when(dbConnector.getConnection()).thenReturn(connection);
    loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);

    // when
    try {
      securityModule.encryptPassword(password);
    } catch (RuntimeException e) {
      exceptionThrown = e;
    }

    // then
    assertNotNull(exceptionThrown);
    assertInstanceOf(SecurityException.class, exceptionThrown);
    assertEquals("Błąd podczas szyfrowania hasła", exceptionThrown.getMessage());
  }

  @Test
  public void whenEncryptPasswordWithValidKeys_thenShouldReturnEncryptedPassword() {
    //given
    String password = "Password123!";

    when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));

    //when
    Optional<String> encryptedPassword = securityModule.encryptPassword(password);

    //then
    assertNotNull(encryptedPassword);
    assertTrue(encryptedPassword.isPresent());
  }

  @Test
  public void whenDecryptPasswordWithValidKeys_thenReturnOriginalPassword() {
    //given
    String password = "Password123!";
    when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));
    when(securityModule.getPrivateKey()).thenReturn(Optional.of(keyPair.getPrivate()));

    Optional<String> encryptedPassword = securityModule.encryptPassword(password);

    //when
    Optional<String> decryptedPassword = securityModule.decryptPassword(encryptedPassword.get());

    //then
    assertTrue(decryptedPassword.isPresent());
    assertEquals(password, decryptedPassword.get());
  }
}
