package pl.rg.users.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rg.users.UserDto;
import pl.rg.users.UserModuleApi;
import pl.rg.utils.db.DBConnector;
import pl.rg.utils.exception.ValidationException;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.validator.api.ValidatorService;

public class UserModuleControllerTest {

  @Mock
  private DBConnector dbConnector;

  @Mock
  private Connection connection;

  @Mock
  private LoggerImpl logger;

  @Mock
  private ValidatorService validatorService;

  @Mock
  private UserModuleApi userModuleApi;

  @InjectMocks
  UserModuleControllerImpl userModuleController;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void whenCreateUserWithValidData_thenShouldCreateNewUser() {
    //given
    try (MockedStatic<DBConnector> dbConnectorMockedStatic = mockStatic(DBConnector.class);
        MockedStatic<DriverManager> driverManagerMockedStatic = mockStatic(DriverManager.class)) {

      dbConnectorMockedStatic.when(DBConnector::getInstance).thenReturn(dbConnector);
      driverManagerMockedStatic.when(
              () -> DriverManager.getConnection(anyString(), anyString(), anyString()))
          .thenReturn(connection);
      when(dbConnector.getConnection()).thenReturn(connection);
    }

    //when
    boolean isCreated = userModuleController.createUser("Jan", "Kowalski", "j.kowalski@email.com");

    //then
    assertTrue(isCreated);
  }

  @Test
  public void whenCreateUserWithInvalidData_thenShouldThrowException() {
    //given
    Map<String, String> validationErrors = Map.of(
        "firstName", "Niepoprawne imię, powinno zawierać tylko litery",
        "email", "Nieprawidłowy format email"
    );

    //when
    try (MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class)) {
      loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);
      when(validatorService.validateFields(any(UserDto.class))).thenReturn(validationErrors);
      when(logger.logAndThrowRuntimeException(any(), any())).thenReturn(new ValidationException());

      //then
      assertThrows(ValidationException.class,
          () -> userModuleController.createUser("Jan123", "Kowalski",
              "j.kowalski.email.com"));
      verify(logger, times(1)).logAndThrowRuntimeException(any(), any());
    }
  }
}