package pl.rg.users.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.rg.users.impl.UserTestModel.ENCRYPTED_PASSWORD;
import static pl.rg.users.impl.UserTestModel.GENERATED_PASSWORD;
import static pl.rg.users.model.UserModel.FIRST_NAME;
import static pl.rg.users.model.UserModel.LAST_NAME;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rg.EmailModuleApi;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.User;
import pl.rg.users.model.SessionModel;
import pl.rg.users.model.UserModel;
import pl.rg.users.repository.UserRepository;
import pl.rg.users.session.UserSessionImpl;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.filter.FilterConditionType;
import pl.rg.utils.repository.filter.FilterSearchType;
import pl.rg.utils.repository.paging.Order;
import pl.rg.utils.repository.paging.OrderType;
import pl.rg.utils.repository.paging.Page;

class UserModuleImplTest {

  @Mock
  UserRepository userRepository;

  @InjectMocks
  UserModuleImpl userModule;

  @Mock
  private SecurityModuleApi securityModuleApi;

  @Mock
  private LoggerImpl logger;

  @Mock
  private UserSessionImpl session;

  @Mock
  private EmailModuleApi emailModuleApi;

  private UserTestModel userTestModel;

  @BeforeEach
  public void setUp() {
    userTestModel = new UserTestModel();
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void whenAddingUserWithValidData_thenShouldSaveNewUser() {
    //given
    User user = userTestModel.returnUser("Kowalski");

    try (MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class)) {

      when(userRepository.containsUsername(anyString()))
          .thenReturn(true)
          .thenReturn(true)
          .thenReturn(false);
      loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);
      when(securityModuleApi.generatePassword()).thenReturn(GENERATED_PASSWORD);
      when(securityModuleApi.encryptPassword(GENERATED_PASSWORD)).thenReturn(
          Optional.of(ENCRYPTED_PASSWORD));
      doNothing().when(emailModuleApi).sendNotification(any(), any(), any());

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
    when(userRepository.findById(any())).thenReturn(Optional.of(userTestModel.returnUserModel()));

    //when
    Optional<User> user = userModule.find(1);

    //then
    assertNotNull(user);
    assertTrue(user.isPresent());
    assertEquals("jankow", user.get().getUserName());
    assertEquals(1, user.get().getId());
  }

  @Test
  public void whenSearchForNonExistingUser_ThenShouldThrowApplicationException() {
    //given
    try (MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class)) {
      loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);
      when(logger.logAndThrowRuntimeException(any(), any())).thenReturn(new ApplicationException());

      //when
      when(userRepository.findById(any())).thenReturn(Optional.empty());

      //then
      assertThrows(ApplicationException.class, () -> userModule.find(1));
      verify(logger, times(1)).logAndThrowRuntimeException(any(), any());
    }
  }

  @Test
  public void whenUpdateUser_ThenShouldSaveUpdatedUser() {
    //given
    User user = userTestModel.returnUser("Nowak");

    //when
    userModule.update(user);

    //then
    verify(userRepository, times(1)).save(any());
    assertEquals("Nowak", user.getLastName());
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
    List<User> filteredUsers = userModule.getFiltered(filters);

    //then
    assertNotNull(filteredUsers);
    verify(userRepository, times(1)).findAll(filters);
    verify(userRepository).findAll(argThat(argument ->
        argument.size() == filters.size() && argument.containsAll(filters)));
    assertEquals("jankow", filteredUsers.get(0).getUserName());
    assertEquals("tomnow", filteredUsers.get(1).getUserName());
    assertEquals(2, filteredUsers.size());
    assertEquals(userTestModel.returnUserList(), filteredUsers);
  }

  @Test
  public void whenSearchUsersByPage_thenShouldReturnMifidUsersPage() {
    //given
    List<Filter> filters = List.of(
        new Filter(FIRST_NAME, new Object[]{"Jan"}, FilterSearchType.EQUAL));
    new Filter(LAST_NAME, new Object[]{"Nowak"}, FilterSearchType.MATCH, FilterConditionType.OR);
    Order order = new Order(LAST_NAME, OrderType.ASC);
    Order order2 = new Order(FIRST_NAME, OrderType.DESC);
    Page page = new Page(0, 2, List.of(order, order2));
    when(userRepository.findAll(filters, page)).thenReturn(
        new MifidPage<>(2, 1, 0, 2, userTestModel.returnUserModelList()));

    //when
    MifidPage usersPage = userModule.getPage(filters, page);

    //then
    assertNotNull(usersPage);
    verify(userRepository, times(1)).findAll(filters, page);
    verify(userRepository).findAll(filters, page);
    assertEquals(1, usersPage.getTotalPage());
    assertEquals(2, usersPage.getTotalObjects());
    assertEquals(page.getTo() - page.getFrom(), usersPage.getLimitedObjects().size());
    assertEquals(userTestModel.returnUserList(), usersPage.getLimitedObjects());
  }

  @Test
  public void whenLoginWithValidCredentials_thenShouldReturnTrue() {
    //given
    UserModel userModel = userTestModel.returnUserModel();
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userModel));
    when(securityModuleApi.decryptPassword(anyString())).thenReturn(
        Optional.of(userModel.getPassword()));

    //when
    boolean loginStatus = userModule.validateLogInData(userModel.getUserName(),
        userModel.getPassword());

    //then
    assertTrue(loginStatus);
    verify(userRepository, times(1)).findByUsername(userModel.getUserName());
    verify(securityModuleApi, times(1)).decryptPassword(anyString());
  }

  @Test
  public void whenLoginWithInvalidUsername_thenShouldReturnFalse() {
    //given
    UserModel userModel = userTestModel.returnUserModel();
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    //when
    boolean loginStatus = userModule.validateLogInData(anyString(), userModel.getPassword());

    //then
    assertFalse(loginStatus);
    verify(userRepository, times(1)).findByUsername(anyString());
    verify(securityModuleApi, times(0)).decryptPassword(anyString());
  }

  @Test
  public void whenLoginWithInvalidUsername_thenShouldThrowApplicationException() {
    //given
    Exception exceptionThrown = null;
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
    when(logger.logAndThrowRuntimeException(any(), any(RuntimeException.class))).thenReturn(
        new ApplicationException("U36GH", "Nie znaleziono użytownika o podanym loginie"));

    // when
    try {
      userModule.checkAvailableLoginAttempts(anyString());
    } catch (ApplicationException e) {
      exceptionThrown = e;
    }

    // then
    assertNotNull(exceptionThrown);
    assertInstanceOf(ApplicationException.class, exceptionThrown);
    assertEquals("U36GH: Nie znaleziono użytownika o podanym loginie",
        exceptionThrown.getMessage());
    assertThrows(ApplicationException.class,
        () -> userModule.checkAvailableLoginAttempts(anyString()));
  }

  @Test
  public void whenLoginWithAvailableAttempts_thenShouldIncreaseAttempts() {
    //given
    UserModel userModel = userTestModel.returnUserModel(0, null);
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userModel));

    //when
    int availableLoginAttempts = userModule.checkAvailableLoginAttempts(userModel.getUserName());

    //then
    assertEquals(2, availableLoginAttempts);
    assertEquals(1, userModel.getLoginAttempts());
    assertNull(userModel.getBlockedTime());
    verify(userRepository, times(1)).findByUsername(anyString());
    verify(userRepository, times(1)).save(any());
  }

  @Test
  public void whenLoginWithInvalidCredentialsAndLastLoginAttempt_thenShouldBlockUser() {
    //given
    UserModel userModel = userTestModel.returnUserModel(2, null);
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userModel));

    //when
    int availableLoginAttempts = userModule.checkAvailableLoginAttempts(userModel.getUserName());

    //then
    assertEquals(0, availableLoginAttempts);
    assertEquals(3, userModel.getLoginAttempts());
    assertNotNull(userModel.getBlockedTime());
    verify(userRepository, times(1)).findByUsername(anyString());
    verify(userRepository, times(1)).save(any());
  }

  @Test
  public void whenLoginWithBlockedUserAfter1Hour_thenShouldUnblockUser() {
    //given
    UserModel userModel = userTestModel.returnUserModel(0, LocalDateTime.now().minusHours(2));
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userModel));

    //when
    int availableLoginAttempts = userModule.checkAvailableLoginAttempts(userModel.getUserName());

    //then
    assertEquals(2, availableLoginAttempts);
    assertEquals(1, userModel.getLoginAttempts());
    assertNull(userModel.getBlockedTime());
    verify(userRepository, times(1)).findByUsername(anyString());
    verify(userRepository, times(1)).save(any());
  }

  @Test
  public void whenResetLoginPasswordForValidUsername_thenShouldSetLoginAttemptsTo0() {
    //given
    UserModel userModel = userTestModel.returnUserModel();
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userModel));

    //when
    userModule.resetLoginAttempts(userModel.getUserName());

    //then
    assertEquals(0, userModel.getLoginAttempts());
    assertNull(userModel.getBlockedTime());
    verify(userRepository, times(1)).findByUsername(anyString());
    verify(userRepository, times(1)).save(any());
    verify(logger, times(1)).log(LogLevel.INFO,
        "Oblokowano użytkownika " + userModel.getUserName());
  }

  @Test
  public void whenLoginWithValidData_thenShouldStartSession() {
    //given
    UserModel userModel = userTestModel.returnUserModel();
    try (MockedStatic<UserSessionImpl> sessionMockedStatic = mockStatic(UserSessionImpl.class)) {
      sessionMockedStatic.when(UserSessionImpl::getInstance).thenReturn(session);

      doAnswer(invocation -> {
        LocalTime startTime = invocation.getArgument(0);
        when(session.getStartTimeCounter()).thenReturn(startTime);
        return null;
      }).when(session).setStartTimeCounter(any(LocalTime.class));

      doAnswer(invocation -> {
        SessionModel sessionModel = invocation.getArgument(0);
        when(session.getActiveSession()).thenReturn(sessionModel);
        return null;
      }).when(session).setActiveSession(any(SessionModel.class));

      //when
      userModule.startSession(userModel.getUserName());

      //then
      assertNotNull(session.getActiveSession());
      assertNotNull(session.getStartTimeCounter());
      assertNull(session.getActiveSession().getLogoutTime());
      assertEquals(userModel.getUserName(), session.getActiveSession().getUser());
      assertEquals(20, session.getActiveSession().getToken().length());
      verify(logger, times(1)).log(any(), anyString());
    }
  }
}