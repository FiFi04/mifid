package pl.rg.users.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
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
import pl.rg.users.session.Session;
import pl.rg.utils.db.DBConnector;
import pl.rg.utils.exception.ApplicationException;
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

  @AfterEach
  public void tearDown() {
    Session.getInstance().setActiveSession(null);
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

  @Test
  public void whenUpdateUserWithValidData_thenShouldUpdateUser() {
    //given
    UserDto userDto = userTestModel.returnUserDtoList().get(0);
    when(validatorService.validateFields(userDto)).thenReturn(new HashMap<>());

    //when
    userModuleController.updateUser(userDto);

    //then
    verify(validatorService, times(1)).validateFields(userDto);
    verify(userRepository, times(1)).save(any());
  }

  @Test
  public void whenUpdateUserWithInvalidData_thenShouldThrowException() {
    //given
    UserDto userDto = userTestModel.returnUserDtoList().get(0);
    Exception exceptionThrown = null;
    Map<String, String> constraints = Map.of(
        FIRST_NAME, "Niepoprawne imię, powinno zawierać tylko litery",
        EMAIL, "Nieprawidłowy format email"
    );
    when(validatorService.validateFields(userDto)).thenReturn(constraints);
    when(logger.logAndThrowRuntimeException(any(), any(RuntimeException.class))).thenReturn(
        new ValidationException("Błędne dane podczas aktualizowania użytkownika: ", constraints));

    //when
    try {
      userModuleController.updateUser(userDto);
    } catch (ValidationException e) {
      exceptionThrown = e;
    }

    //then
    assertNotNull(exceptionThrown);
    assertInstanceOf(ValidationException.class, exceptionThrown);
    assertEquals("Błędne dane podczas aktualizowania użytkownika: " + constraints,
        exceptionThrown.getMessage());
    assertThrows(ValidationException.class,
        () -> userModuleController.updateUser(userDto));
  }

  @Test
  public void whenLoginWithValidData_thenShouldReturnTrue() {
    //given
    UserModel userModel = userTestModel.returnUserModel();
    when(userRepository.getByUsername(anyString())).thenReturn(Optional.of(userModel));
    when(securityModuleApi.decryptPassword(anyString())).thenReturn(
        Optional.of(userModel.getPassword()));

    //when
    boolean login = userModuleController.logIn(userModel.getUserName(), userModel.getPassword());

    //then
    assertTrue(login);
    assertNotNull(Session.getInstance().getActiveSession());
    assertNull(userModel.getBlockedTime());
    assertEquals(20, Session.getInstance().getActiveSession().getToken().length());
    assertEquals(userModel.getUserName(), Session.getInstance().getActiveSession().getUser());
    assertEquals(0, userModel.getLoginAttempts());
  }

  @Test
  public void whenLoginWithInvalidDataAndNonAvailableAttempts_thenShouldThrowException() {
    //given
    UserModel userModel = userTestModel.returnUserModel();
    userModel.setLoginAttempts(3);
    Exception exceptionThrown = null;
    when(userRepository.getByUsername(anyString())).thenReturn(Optional.of(userModel));
    when(securityModuleApi.decryptPassword(anyString())).thenReturn(
        Optional.of(userModel.getPassword()));
    when(logger.logAndThrowRuntimeException(any(), any(RuntimeException.class))).thenReturn(
        new ApplicationException("U34LV",
            "Wykorzystano wszystkie próby logowania. Spróbuj ponownie później."));

    // when
    try {
      userModuleController.logIn(userModel.getUserName(), anyString());
    } catch (ApplicationException e) {
      exceptionThrown = e;
    }

    //then
    assertNotNull(exceptionThrown);
    assertNull(Session.getInstance().getActiveSession());
    assertInstanceOf(ApplicationException.class, exceptionThrown);
    assertEquals("U34LV: Wykorzystano wszystkie próby logowania. Spróbuj ponownie później.",
        exceptionThrown.getMessage());
    assertThrows(ApplicationException.class,
        () -> userModuleController.logIn(userModel.getUserName(), anyString()));
  }

  @Test
  public void whenLoginWithInvalidDataAndAvailableAttempts_thenShouldThrowException() {
    //given
    UserModel userModel = userTestModel.returnUserModel();
    userModel.setLoginAttempts(0);
    Exception exceptionThrown = null;
    when(userRepository.getByUsername(anyString())).thenReturn(Optional.of(userModel));
    when(securityModuleApi.decryptPassword(anyString())).thenReturn(
        Optional.of(userModel.getPassword()));
    int availableLoginAttempts = userModuleApi.checkAvailableLoginAttempts(userModel.getUserName());
    when(logger.logAndThrowRuntimeException(any(), any(RuntimeException.class))).thenReturn(
        new ApplicationException("U35LV", "Błędne dane podczas logowania. Pozostało "
            + availableLoginAttempts + " prób logowania."));

    // when
    try {
      userModuleController.logIn(userModel.getUserName(), anyString());
    } catch (ApplicationException e) {
      exceptionThrown = e;
    }

    //then
    assertNotNull(exceptionThrown);
    assertNull(Session.getInstance().getActiveSession());
    assertInstanceOf(ApplicationException.class, exceptionThrown);
    assertEquals(2, availableLoginAttempts);
    assertEquals("U35LV: Błędne dane podczas logowania. Pozostało " + availableLoginAttempts
        + " prób logowania.", exceptionThrown.getMessage());
    assertThrows(ApplicationException.class,
        () -> userModuleController.logIn(userModel.getUserName(), anyString()));
  }

  @Test
  public void whenResetLoginPasswordForValidUsername_thenShouldSetLoginAttemptsTo0() {
    //given
    UserModel userModel = userTestModel.returnUserModel();
    userModel.setBlockedTime(LocalDateTime.now());
    userModel.setLoginAttempts(3);
    when(userRepository.getByUsername(anyString())).thenReturn(Optional.of(userModel));

    // when
    userModuleController.resetLoginAttempts(userModel.getUserName());

    //then
    assertNull(userModel.getBlockedTime());
    assertEquals(0, userModel.getLoginAttempts());
    verify(userRepository, times(1)).getByUsername(userModel.getUserName());
    verify(userRepository, times(1)).save(userModel);
    verify(logger, times(1)).log(any(), anyString());
  }
}