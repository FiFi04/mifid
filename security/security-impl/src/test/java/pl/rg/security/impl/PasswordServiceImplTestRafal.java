package pl.rg.security.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static pl.rg.security.impl.SecurityModuleImpl.ALGORITHM_TYPE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Cipher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.rg.security.repository.PublicKeyHashRepository;
import pl.rg.utils.db.DBConnector;

//@RunWith(MockitoJUnitRunner.class)
class PasswordServiceImplTestRafal {

  @Mock
  private DBConnector dbConnector;

  @Mock
  private PublicKeyHashRepository publicKeyHashRepository;

  @InjectMocks
  private SecurityModuleImpl passwordService;

  private KeyPair keyPair;

  @BeforeAll
  static void beforeAll() throws ClassNotFoundException {
//    mySQLContainer.start();
//    System.setProperty("db.url", mySQLContainer.getJdbcUrl());
//    System.setProperty("db.username", mySQLContainer.getUsername());
//    System.setProperty("db.password", mySQLContainer.getPassword());
  }

  @BeforeEach
  void setUp() throws NoSuchAlgorithmException, SQLException {
    DBConnector instance = DBConnector.getInstance();
    when(dbConnector.getConnection()).thenReturn(
        DriverManager.getConnection("jdbc:mysql://localhost:3306/mifid", "root", "admin"));

    MockitoAnnotations.openMocks(this);

//    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_TYPE);
//    keyPairGenerator.initialize(2048);
//    keyPair = keyPairGenerator.generateKeyPair();
  }

  @Test
  void shouldEncryptPasswordWhenKeysArePresent() throws Exception {
    String password = "Pasword123!";
//    when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.of(keyPair.getPublic()));

    Optional<String> encryptedPassword = passwordService.encryptPassword(password);

    assertTrue(encryptedPassword.isPresent());
    Cipher cipher = Cipher.getInstance(ALGORITHM_TYPE);
    cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
    String decryptedPassword = new String(
        cipher.doFinal(Base64.getDecoder().decode(encryptedPassword.get())));
    assertEquals(password, decryptedPassword);
  }

  @Test
  void shouldFailEncryptionWhenPublicKeyIsNotPresent() {
    String password = "Password123!";
//    when(publicKeyHashRepository.getPublicKey()).thenReturn(Optional.empty());

    Optional<String> encryptedPassword = passwordService.encryptPassword(password);

    assertFalse(encryptedPassword.isPresent());
  }

  @Test
  void shouldFailDecryptionWhenPrivateKeyIsNotPresent()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    String password = "Password123!";
    Method getPrivateKey = SecurityModuleImpl.class.getMethod("getPrivateKey");
    getPrivateKey.setAccessible(true);
    when(getPrivateKey.invoke(passwordService)).thenReturn(Optional.empty());

    Optional<String> decryptedPassword = passwordService.decryptPassword(password);

    assertFalse(decryptedPassword.isPresent());
  }
}