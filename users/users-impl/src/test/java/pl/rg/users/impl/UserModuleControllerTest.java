package pl.rg.users.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.rg.users.impl.UserTestModel.ENCRYPTED_PASSWORD;
import static pl.rg.users.impl.UserTestModel.GENERATED_PASSWORD;
import static pl.rg.users.model.UserModel.EMAIL;
import static pl.rg.users.model.UserModel.FIRST_NAME;
import static pl.rg.users.model.UserModel.LAST_NAME;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.UserDto;
import pl.rg.users.model.UserModel;
import pl.rg.users.repository.UserRepository;
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
  private UserRepository userRepository;

  @Mock
  private SecurityModuleApi securityModuleApi;

  @InjectMocks
  private UserModuleControllerImpl userModuleController;

  @InjectMocks
  private UserModuleImpl userModuleApi;

  private UserTestModel userTestModel;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userTestModel = new UserTestModel();
    userModuleApi.getSession().setStartTimeCounter(LocalTime.now());
    userModuleController.setUserModuleApi(userModuleApi);

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
      when(securityModuleApi.generatePassword()).thenReturn(GENERATED_PASSWORD);
      when(securityModuleApi.encryptPassword(anyString())).thenReturn(
          Optional.of(ENCRYPTED_PASSWORD));
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
        FIRST_NAME, "Niepoprawne imię, powinno zawierać tylko litery",
        EMAIL, "Nieprawidłowy format email"
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
        new Filter(FIRST_NAME, new Object[]{"Jan"}, FilterSearchType.EQUAL),
        new Filter(LAST_NAME, new Object[]{"Nowak"}, FilterSearchType.MATCH,
            FilterConditionType.OR));
    when(userRepository.findAll(filters)).thenReturn(userTestModel.returnUserModelList());

    //when
    List<UserDto> filteredUserDto = userModuleController.getFiltered(filters);

    //then
    assertNotNull(filteredUserDto);
    assertEquals("jankow", filteredUserDto.get(0).getUserName());
    assertEquals("tomnow", filteredUserDto.get(1).getUserName());
    assertEquals(2, filteredUserDto.size());
    assertEquals(userTestModel.returnUserDtoList(), filteredUserDto);
  }

  @Test
  public void whenSearchUsersByPage_thenShouldReturnMifidUsersPage() {
    //given
    List<Filter> filters = List.of(
        new Filter(FIRST_NAME, new Object[]{"Jan"}, FilterSearchType.EQUAL),
        new Filter(LAST_NAME, new Object[]{"Nowak"}, FilterSearchType.MATCH,
            FilterConditionType.OR));
    Order order = new Order(LAST_NAME, OrderType.ASC);
    Order order2 = new Order(FIRST_NAME, OrderType.DESC);
    Page page = new Page(0, 2, List.of(order, order2));
    when(userRepository.findAll(filters, page)).thenReturn(
        new MifidPage<UserModel>(2, 1, 0, 2, userTestModel.returnUserModelList()));

    //when
    MifidPage usersPage = userModuleController.getPage(filters, page);

    //then
    assertNotNull(usersPage);
    assertEquals(1, usersPage.getTotalPage());
    assertEquals(2, usersPage.getTotalObjects());
    assertEquals(page.getTo() - page.getFrom(), usersPage.getLimitedObjects().size());
    assertEquals(userTestModel.returnUserDtoList(), usersPage.getLimitedObjects());
  }
}