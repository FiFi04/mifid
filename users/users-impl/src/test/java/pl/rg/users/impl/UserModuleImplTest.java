package pl.rg.users.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.User;
import pl.rg.users.model.UserModel;
import pl.rg.users.repository.UserRepository;
import pl.rg.utils.db.DBConnector;
import pl.rg.utils.logger.LoggerImpl;

class UserModuleImplTest {

  protected final static String GENERATED_PASSWORD = "Password123!";

  protected final static String ENCRYPTED_PASSWORD = "EncryptedPassword123!";

  @Mock
  UserRepository userRepository;

  @Mock
  private DBConnector dbConnector;

  @Mock
  private Connection connection;

  @Mock
  private SecurityModuleApi securityModuleApi;

  @Mock
  private LoggerImpl logger;

  @InjectMocks
  UserModuleImpl userModule;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void whenAddingUserWithValidData_thenShouldSaveNewUser() {
    //given
    User user = new UserImpl();
    user.setFirstName("Jan");
    user.setLastName("Kowalski");
    user.setEmail("j.kowalski@email.com");

    try (MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class)) {

      when(userRepository.containsUsername(anyString()))
          .thenReturn(true)
          .thenReturn(true)
          .thenReturn(false);
      loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);
      when(securityModuleApi.generatePassword()).thenReturn(GENERATED_PASSWORD);
      when(securityModuleApi.encryptPassword(GENERATED_PASSWORD)).thenReturn(
          Optional.of(ENCRYPTED_PASSWORD));

      //when
      userModule.addUser(user);

      //then
      assertEquals("jankow2", user.getUserName());
      verify(userRepository, times(1)).save(any());
    }
  }

  @Test
  public void whenFoundExistingUser_ThenShouldReturnUser() {
    //given
    UserModel userModel = new UserModel("jankow", UserModuleImplTest.ENCRYPTED_PASSWORD, "Jan",
        "Kowalski", "jan.kowalski@email.com");
    when(userRepository.findById(any())).thenReturn(Optional.of(userModel));
    when(securityModuleApi.decryptPassword(anyString())).thenReturn(
        Optional.of(UserModuleImplTest.GENERATED_PASSWORD));

    //when
    Optional<User> user = userModule.find(1);

    //then
    assertTrue(user.isPresent());
    assertEquals("jankow", user.get().getUserName());
    assertEquals(1, user.get().getId());
  }

  @Test
  public void whenSearchForNonExistingUser_ThenShouldReturnEmptyOptional() {
    //given
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    //when
    Optional<User> user = userModule.find(1);

    //then
    assertTrue(user.isEmpty());
  }

  @Test
  public void whenUpdateUser_ThenShouldSaveUpdatedUser() {
    //given
    User user = new UserImpl("jankow", UserModuleImplTest.ENCRYPTED_PASSWORD, "Jan",
        "Kowalski", "jan.kowalski@email.com");
    user.setLastName("Nowak");
    when(securityModuleApi.encryptPassword(any())).thenReturn(
        Optional.of(UserModuleImplTest.ENCRYPTED_PASSWORD));

    //when
    userModule.update(user);

    //then
    verify(userRepository, times(1)).save(any());
  }
}