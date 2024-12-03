package pl.rg.users.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.rg.users.impl.UserTestModel.ENCRYPTED_PASSWORD;
import static pl.rg.users.impl.UserTestModel.GENERATED_PASSWORD;
import static pl.rg.users.model.UserModel.FIRST_NAME;
import static pl.rg.users.model.UserModel.LAST_NAME;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.User;
import pl.rg.users.repository.UserRepository;
import pl.rg.utils.exception.ApplicationException;
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

  @Mock
  private SecurityModuleApi securityModuleApi;

  @Mock
  private LoggerImpl logger;

  @InjectMocks
  UserModuleImpl userModule;

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
}