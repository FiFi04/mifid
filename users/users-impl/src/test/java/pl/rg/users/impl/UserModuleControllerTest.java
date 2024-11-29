package pl.rg.users.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rg.users.User;
import pl.rg.users.UserDto;
import pl.rg.users.UserModuleApi;
import pl.rg.utils.db.DBConnector;
import pl.rg.utils.exception.ValidationException;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.filter.FilterConditionType;
import pl.rg.utils.repository.filter.FilterSearchType;
import pl.rg.utils.repository.paging.Order;
import pl.rg.utils.repository.paging.OrderType;
import pl.rg.utils.repository.paging.Page;
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

  @Test
  public void whenSearchUsersWithFilters_thenShouldReturnFilteredUsers() {
    //given
    List<Filter> filters = List.of(
        new Filter("firstName", new Object[]{"Jan"}, FilterSearchType.EQUAL),
        new Filter("lastName", new Object[]{"Nowak"}, FilterSearchType.MATCH,
            FilterConditionType.OR));
    List<User> users = List.of(
        new UserImpl(1, "jankow", "password1", "Jan", "Kowalski", "jan.kowalski@example.com"),
        new UserImpl(2, "tomnow", "password2", "Tomasz", "Nowak", "tomasz.nowak@example.com"));
    List<UserDto> userDtos = List.of(
        new UserDto(1, "jankow", "Jan", "Kowalski", "jan.kowalski@example.com"),
        new UserDto(2, "tomnow", "Tomasz", "Nowak", "tomasz.nowak@example.com"));
    when(userModuleApi.getFiltered(filters)).thenReturn(users);

    //when
    List<UserDto> filteredUserDto = userModuleController.getFiltered(filters);

    //then
    verify(userModuleApi, times(1)).getFiltered(filters);
    assertEquals(userDtos, filteredUserDto);
  }

  @Test
  public void whenSearchUsersByPage_thenShouldReturnMifidUsersPage() {
    //given
    List<Filter> filters = List.of(
        new Filter("firstName", new Object[]{"Jan"}, FilterSearchType.EQUAL),
        new Filter("lastName", new Object[]{"Nowak"}, FilterSearchType.MATCH,
            FilterConditionType.OR));
    Order order = new Order("last_name", OrderType.ASC);
    Order order2 = new Order("first_name", OrderType.DESC);
    Page page = new Page(0, 2, List.of(order, order2));
    List<User> users = List.of(
        new UserImpl(1, "jankow", "password1", "Jan", "Kowalski", "jan.kowalski@example.com"),
        new UserImpl(2, "tomnow", "password2", "Tomasz", "Nowak", "tomasz.nowak@example.com"));
    List<UserDto> userDtos = List.of(
        new UserDto(1, "jankow", "Jan", "Kowalski", "jan.kowalski@example.com"),
        new UserDto(2, "tomnow", "Tomasz", "Nowak", "tomasz.nowak@example.com"));
    when(userModuleApi.getPage(filters, page)).thenReturn(new MifidPage<>(2, 1, 0, 2, users));

    //when
    MifidPage usersPage = userModuleController.getPage(filters, page);

    //then
    verify(userModuleApi, times(1)).getPage(filters, page);
    assertEquals(userDtos, usersPage.getLimitedObjects());
  }
}